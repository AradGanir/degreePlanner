# transcript_scraper.py
import pdfplumber
import re
import json
import mysql.connector

# Database connection
conn = mysql.connector.connect(
    host="localhost",
    user="pathway_user",
    password="pathway_pass",
    database="pathway_planner",
)
cursor = conn.cursor()

# Grade mapping from transcript to database enum
GRADE_MAP = {
    "A": "A", "A-": "A_MINUS",
    "B+": "B_PLUS", "B": "B", "B-": "B_MINUS",
    "C+": "C_PLUS", "C": "C", "C-": "C_MINUS",
    "D+": "D_PLUS", "D": "D", "D-": "D_MINUS",
    "F": "F", "W": "W", "IP": "IP",
    "S": None, "U": None, "T": None  # Pass/Fail and Transfer - no grade equivalent
}


def parse_transcript(pdf_path):
    """Parse Emory transcript PDF and extract course data."""

    result = {
        "student_name": None,
        "student_id": None,
        "ap_credits": [],
        "semesters": []
    }

    with pdfplumber.open(pdf_path) as pdf:
        full_text = ""
        for page in pdf.pages:
            full_text += page.extract_text() + "\n"

            # Extract header info
    name_match = re.search(r"Name:\s*(.+)", full_text)
    id_match = re.search(r"Student ID:\s*(\d+)", full_text)

    if name_match:
        result["student_name"] = name_match.group(1).strip()
    if id_match:
        result["student_id"] = id_match.group(1).strip()

        # Extract AP/Transfer credits
    # Pattern: MATH_OX 111 Calculus I 3.000 T
    ap_pattern = re.compile(
        r"([A-Z_]+)\s+(\d+[A-Z]?)\s+(.+?)\s+(\d+\.\d+)\s+T"
    )
    for match in ap_pattern.finditer(full_text):
        result["ap_credits"].append({
            "code": match.group(1),
            "num": match.group(2),
            "title": match.group(3).strip(),
            "credits": float(match.group(4)),
            "status": "TRANSFER"
        })

        # Split by semester headers
    semester_pattern = re.compile(
        r"(Fall|Spring|Summer)\s+(\d{4})\s*\n"
        r"Program:.*?\n"
        r"Plan:.*?\n"
    )

    # Course line pattern:
    # MATH_OX 221 Linear Algebra 4.000 4.000 A 16.000
    course_pattern = re.compile(
        r"^([A-Z_]+)\s+(\d+[A-Z]*)\s+(.+?)\s+"  # code, num, title                                                                                                                                                                                          
        r"(\d+\.\d+)\s+"  # attempted                                                                                                                                                                                                
        r"(\d+\.\d+)\s+"  # earned                                                                                                                                                                                                   
        r"([A-Z][+-]?|S|U|W|IP)\s+"  # grade                                                                                                                                                                                                    
        r"(\d+\.\d+)",
        # points
        re.MULTILINE
    )

    # Find semester boundaries
    semesters = list(semester_pattern.finditer(full_text))

    for i, sem_match in enumerate(semesters):
        semester_name = f"{sem_match.group(1)} {sem_match.group(2)}"

        # Get text until next semester or end
        start = sem_match.end()
        end = semesters[i + 1].start() if i + 1 < len(semesters) else len(full_text)
        semester_text = full_text[start:end]

        courses = []
        for course_match in course_pattern.finditer(semester_text):
            courses.append({
                "code": course_match.group(1),
                "num": course_match.group(2),
                "title": course_match.group(3).strip(),
                "attempted": float(course_match.group(4)),
                "earned": float(course_match.group(5)),
                "grade": course_match.group(6),
                "points": float(course_match.group(7))
            })

            # Extract term GPA
        gpa_match = re.search(r"Term GPA\s+(\d+\.\d+)", semester_text)

        result["semesters"].append({
            "semester": semester_name,
            "courses": courses,
            "term_gpa": float(gpa_match.group(1)) if gpa_match else None
        })

    return result


def to_enrollments(parsed):
    """Convert parsed transcript to enrollment records for API."""
    enrollments = []

    # AP credits as completed
    for ap in parsed["ap_credits"]:
        enrollments.append({
            "courseCode": ap["code"],
            "courseNum": ap["num"],
            "semester": "Transfer",
            "grade": "T",
            "status": "COMPLETED"
        })

        # Semester courses
    for sem in parsed["semesters"]:
        for course in sem["courses"]:
            # Determine status based on earned credits
            if course["earned"] > 0:
                status = "COMPLETED"
            elif course["grade"] in ("W",):
                status = "WITHDRAWN"
            else:
                status = "IN_PROGRESS"

            enrollments.append({
                "courseCode": course["code"],
                "courseNum": course["num"],
                "semester": sem["semester"],
                "grade": course["grade"],
                "status": status
            })

    return enrollments


def insert_enrollments(student_id, parsed):
    """Insert parsed transcript enrollments into database."""

    # Get course IDs by code+num
    cursor.execute("SELECT id, code, course_num FROM course")
    courses = {(row[1], row[2]): row[0] for row in cursor.fetchall()}

    inserted = 0
    skipped = 0

    # Insert AP/Transfer credits
    for ap in parsed["ap_credits"]:
        course_key = (ap["code"], ap["num"])
        if course_key not in courses:
            print(f"Course not found: {ap['code']} {ap['num']}")
            skipped += 1
            continue

        cursor.execute("""
            INSERT IGNORE INTO enrollment (student_id, course_id, grade, semester, enrollment_status)
            VALUES (%s, %s, %s, %s, %s)
        """, (student_id, courses[course_key], None, "Transfer", "COMPLETED"))
        inserted += 1

    # Insert semester courses
    for sem in parsed["semesters"]:
        for course in sem["courses"]:
            course_key = (course["code"], course["num"])
            if course_key not in courses:
                print(f"Course not found: {course['code']} {course['num']}")
                skipped += 1
                continue

            grade = GRADE_MAP.get(course["grade"])

            if course["earned"] > 0:
                status = "COMPLETED"
            elif course["grade"] == "W":
                status = "WITHDRAWN"
            else:
                status = "IN_PROGRESS"

            cursor.execute("""
                INSERT IGNORE INTO enrollment (student_id, course_id, grade, semester, enrollment_status)
                VALUES (%s, %s, %s, %s, %s)
            """, (student_id, courses[course_key], grade, sem["semester"], status))
            inserted += 1

    conn.commit()
    return inserted, skipped


if __name__ == "__main__":
    import sys

    if len(sys.argv) < 2:
        print("Usage: python transcript_scraper.py <student_id> [pdf_path]")
        sys.exit(1)

    student_id = int(sys.argv[1])
    pdf_path = sys.argv[2] if len(sys.argv) > 2 else "SSR_TSRPT-2.pdf"

    parsed = parse_transcript(pdf_path)
    enrollments = to_enrollments(parsed)

    output = {
        "parsed_transcript": parsed,
        "enrollments": enrollments
    }

    with open("student_transcript.json", "w") as f:
        json.dump(output, f, indent=2)
    print("Parsed transcript saved to student_transcript.json")

    # Insert into database
    inserted, skipped = insert_enrollments(student_id, parsed)
    print(f"Inserted {inserted} enrollments, skipped {skipped}")

    cursor.close()
    conn.close()


