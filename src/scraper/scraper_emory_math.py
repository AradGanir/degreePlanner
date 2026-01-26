import requests
from bs4 import BeautifulSoup
import json
import re

URL = "https://catalog.college.emory.edu/academics/departments/mathematics.html"


def scrape():
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    print(f"Loading {URL}...")
    res = requests.get(URL, headers=headers)
    res.raise_for_status()

    soup = BeautifulSoup(res.text, "html.parser")

    courses = []

    # In the Emory catalog, courses are usually contained within a specific main div
    # We look for the patterns "MATH XXX" inside heading tags or bold tags
    # Based on the catalog structure, they often use <h3> or <strong> for titles

    course_elements = soup.find_all(['h3', 'h4', 'p', 'div'], string=re.compile(r'MATH\s+\d+'))

    for element in course_elements:
        text = element.get_text(strip=True)

        # Regex to catch "MATH 111: Calculus I" or "MATH 111 - Calculus I"
        match = re.match(r"(MATH)\s+([0-9A-Z]+)[:\-\s]+(.*)", text)

        if match:
            dept, number, title = match.groups()

            description = ""
            credits = ""

            # Now we look at the elements immediately following this header
            curr = element.find_next_sibling()

            # Keep looking at siblings until we hit the next course or run out of elements
            while curr and not re.search(r'MATH\s+\d+', curr.get_text()):
                content = curr.get_text(strip=True)

                if "Credit Hours" in content:
                    # Extracts numbers or ranges like "3" or "1-4"
                    cr_match = re.search(r"(\d+(?:\s*-\s*\d+)?)", content)
                    if cr_match:
                        credits = cr_match.group(1)
                elif content:
                    # This is likely the description text
                    description += content + " "

                curr = curr.find_next_sibling()

            courses.append({
                "code": dept,
                "courseNum": number,
                "title": title.strip(),
                "description": description.strip(),
                "credits": credits
            })

    return courses


if __name__ == "__main__":
    all_courses = scrape()

    # If the list is empty, it means the site is purely dynamic.
    # But if the text is in the HTML, this will find it.
    print(f"Found {len(all_courses)} courses")

    if all_courses:
        with open("emory_math_courses.json", "w") as f:
            json.dump(all_courses, f, indent=2)
        print("Saved to emory_math_courses.json")