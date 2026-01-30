import json
import mysql.connector

conn = mysql.connector.connect(
        host="localhost",
    user="pathway_user",
    password="pathway_pass",
    database="pathway_planner",
)

cursor = conn.cursor()

files = ["all_emory_courses.json", "oxford_courses.json"]
for file in files:

    with open(file, "r") as f:
        courses = json.load(f)

    insert_sql = """
    INSERT IGNORE INTO course (code, course_num, title, description, credits)
    VALUES (%s, %s, %s, %s, %s)
    """


    inserted = 0
    skipped = 0
    i=0

    for c in courses:
        try:
            print(f"Printing course {i}")
            i+=1
            code = c["dept"]
            course_num = c["num"]
            title = c["title"]
            description = c.get("description")

            credits = c.get("min_credits") or c.get("credits")
            if credits is None:
                skipped += 1
                continue

            cursor.execute(
                insert_sql,
                (code, course_num, title, description, credits)
            )
            inserted += 1

        except Exception as e:
            print(f"Skipping {c.get('dept')} {c.get('num')}: {e}")
            skipped += 1


conn.commit()
cursor.close()
conn.close()

print(f"Inserted: {inserted}")
print(f"Skipped: {skipped}")

