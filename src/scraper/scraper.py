import requests
from bs4 import BeautifulSoup
import json
import re
import time


def scrape_dept(url, prefix):
    print(f"Scraping {prefix} from: {url}")
    headers = {"User-Agent": "Mozilla/5.0"}

    try:
        res = requests.get(url, headers=headers)
        res.raise_for_status()
        soup = BeautifulSoup(res.text, "html.parser")

        # Pattern matches "AAS 100:" or "AAS 100W:" at start of text
        pattern = re.compile(rf'^{prefix}\s+\d+[A-Z]?\s*:', re.IGNORECASE)

        courses = []
        seen = set()  # Track (dept, num) to avoid duplicates

        # Courses are in accordion buttons with class "accordion__toggle"
        buttons = soup.find_all('button', class_='accordion__toggle')

        for btn in buttons:
            text = btn.get_text(strip=True)

            if pattern.match(text):
                # Extract code and title from "AAS 100: Intro To..."
                header_parts = text.split(':', 1)
                full_code = header_parts[0].strip()
                course_num = full_code.replace(prefix, "").strip()
                course_title = header_parts[1].strip() if len(header_parts) > 1 else ""

                # Skip duplicates
                key = (prefix, course_num)
                if key in seen:
                    continue
                seen.add(key)

                course_entry = {
                    "dept": prefix,
                    "num": course_num,
                    "title": course_title,
                    "description": "",
                    "credits": None,      # max credits (or fixed)
                    "min_credits": None,  # only if variable (e.g., 1-4)
                    "ger": "",
                    "requisites": "",
                    "cross_listed": ""
                }

                # Find the collapse div that contains course details
                # The button's data-target points to "#collapse-AAS-003720"
                target_id = btn.get('data-target', '').replace('#', '')
                if target_id:
                    collapse_div = soup.find(id=target_id)
                    if collapse_div:
                        collapse_text = collapse_div.get_text(separator='\n', strip=True)

                        # Extract credits - handle ranges like "1 - 4"
                        credit_match = re.search(r'Credit Hours[:\s]*(\d+)\s*(?:-\s*(\d+))?', collapse_text)
                        if credit_match:
                            first = int(credit_match.group(1))
                            second = credit_match.group(2)
                            if second:
                                # Range: min_credits=first, credits=second (max)
                                course_entry["min_credits"] = first
                                course_entry["credits"] = int(second)
                            else:
                                # Fixed credits
                                course_entry["credits"] = first

                        ger_match = re.search(r'GER[:\s]*([A-Z,\s]+?)(?:\n|Requisites|$)', collapse_text)
                        if ger_match:
                            course_entry["ger"] = ger_match.group(1).strip()

                        req_match = re.search(r'Requisites[:\s]*(.+?)(?:\n|Cross-Listed|$)', collapse_text, re.DOTALL)
                        if req_match:
                            course_entry["requisites"] = req_match.group(1).strip()

                        cross_match = re.search(r'Cross-Listed[:\s]*(.+?)(?:\n|$)', collapse_text)
                        if cross_match:
                            course_entry["cross_listed"] = cross_match.group(1).strip()

                        # Description is usually the first paragraph
                        desc_p = collapse_div.find('p')
                        if desc_p:
                            course_entry["description"] = desc_p.get_text(strip=True)

                courses.append(course_entry)

        print(f"  Found {len(courses)} courses for {prefix}")
        return courses

    except Exception as e:
        print(f"  [!] Error scraping {url}: {e}")
        return []


if __name__ == "__main__":
    # --- STEP 1: COMPILE YOUR LINKS AND PREFIXES HERE ---
    # Add pairs as (URL, Prefix)
    scraping_queue = [
        ("https://catalog.college.emory.edu/academics/departments/aas.html", "AAS"),
        ("https://catalog.college.emory.edu/academics/departments/african-studies.html", "AFS"),
        ("https://catalog.college.emory.edu/academics/departments/american-studies.html", "AMST"),
        ("https://catalog.college.emory.edu/academics/departments/ancient-mediterranean-studies.html", "ANCMED"),
        ("https://catalog.college.emory.edu/academics/departments/anthropology.html", "ANT"),
        ("https://catalog.college.emory.edu/academics/departments/art-history.html", "ARTHIST"),
        ("https://catalog.college.emory.edu/academics/departments/athletics-and-recreation.html", "PE"),
        ("https://catalog.college.emory.edu/academics/departments/biology.html", "BIOL"),
        ("https://catalog.college.emory.edu/academics/departments/catholic-studies.html", "CATH"),
        ("https://catalog.college.emory.edu/academics/departments/chemistry.html", "CHEM"),
        ("https://catalog.college.emory.edu/academics/departments/comparative-literature.html", "CPLT"),
        ("https://catalog.college.emory.edu/academics/departments/computer-science.html", "CS"),
        ("https://catalog.college.emory.edu/academics/departments/creative-writing.html", "ENGCW"),
        ("https://catalog.college.emory.edu/academics/departments/dance.html", "DANC"),
        ("https://catalog.college.emory.edu/academics/departments/qtm.html", "QTM"),
        ("https://catalog.college.emory.edu/academics/departments/eas.html", "EAS"),
        ("https://catalog.college.emory.edu/academics/departments/economics.html", "ECON"),
        ("https://catalog.college.emory.edu/academics/departments/emory-college-seminars.html", "ECS"),
        ("https://catalog.college.emory.edu/academics/departments/english.html", "ENG"),
        ("https://catalog.college.emory.edu/academics/departments/environmental-sciences.html", "ENVS"),
        ("https://catalog.college.emory.edu/academics/departments/film-and-media.html", "FILM"),
        ("https://catalog.college.emory.edu/academics/departments/film-and-media.html", "ARTVIS"),
        ("https://catalog.college.emory.edu/academics/departments/french-and-italian-studies.html", "FREN"),
        ("https://catalog.college.emory.edu/academics/departments/german-studies.html", "GER"),
        ("https://catalog.college.emory.edu/academics/departments/german-studies.html", "YDD"),
        ("https://catalog.college.emory.edu/academics/departments/history.html", "HIST"),
        ("https://catalog.college.emory.edu/academics/departments/human-health.html", "HLTH"),
        ("https://catalog.college.emory.edu/academics/departments/ila.html", "IDS"),
        ("https://catalog.college.emory.edu/academics/departments/italian-studies.html", "ITAL"),
        ("https://catalog.college.emory.edu/academics/departments/jewish-studies.html", "JS"),
        ("https://catalog.college.emory.edu/academics/departments/lacs.html", "LACS"),
        ("https://catalog.college.emory.edu/academics/departments/linguistics.html", "LING"),
        ("https://catalog.college.emory.edu/academics/departments/mathematics.html", "MATH"),
        ("https://catalog.college.emory.edu/academics/departments/middle-eastern-and-south-asian-studies-.html",
         "ARAB"),

        ("https://catalog.college.emory.edu/academics/departments/middle-eastern-and-south-asian-studies-.html",
         "HEBR"),
        ("https://catalog.college.emory.edu/academics/departments/middle-eastern-and-south-asian-studies-.html",
         "HNDI"),
        ("https://catalog.college.emory.edu/academics/departments/middle-eastern-and-south-asian-studies-.html",
         "MESAS"),
        ("https://catalog.college.emory.edu/academics/departments/middle-eastern-and-south-asian-studies-.html",
         "PERS"),
        ("https://catalog.college.emory.edu/academics/departments/middle-eastern-and-south-asian-studies-.html", "TBT"),
        ("https://catalog.college.emory.edu/academics/departments/music.html", "MUS"),
        ("https://catalog.college.emory.edu/academics/departments/nbb.html", "NBB"),
        ("https://catalog.college.emory.edu/academics/departments/nbb.html", "BIOL"),
        ("https://catalog.college.emory.edu/academics/departments/nbb.html", "ECON"),
        ("https://catalog.college.emory.edu/academics/departments/nbb.html", "PHIL"),
        ("https://catalog.college.emory.edu/academics/departments/nbb.html", "PHYS"),
        ("https://catalog.college.emory.edu/academics/departments/nbb.html", "PSYC"),
        ("https://catalog.college.emory.edu/academics/departments/nbb.html", "REL"),
        ("https://catalog.college.emory.edu/academics/departments/philosophy.html", "PHIL"),
        ("https://catalog.college.emory.edu/academics/departments/physics.html", "PHYS"),
        ("https://catalog.college.emory.edu/academics/departments/political-science.html", "POLS"),
        ("https://catalog.college.emory.edu/academics/departments/psychology.html", "PSYC"),
        ("https://catalog.college.emory.edu/academics/departments/religion.html", "REL"),
        ("https://catalog.college.emory.edu/academics/departments/realc.html", "CHN"),
        ("https://catalog.college.emory.edu/academics/departments/realc.html", "EAS"),
        ("https://catalog.college.emory.edu/academics/departments/realc.html", "JPN"),
        ("https://catalog.college.emory.edu/academics/departments/realc.html", "KRN"),
        ("https://catalog.college.emory.edu/academics/departments/realc.html", "REES"),
        ("https://catalog.college.emory.edu/academics/departments/realc.html", "RUSS"),
        ("https://catalog.college.emory.edu/academics/departments/sociology.html", "SOC"),
        ("https://catalog.college.emory.edu/academics/departments/spanish-and-portuguese.html", "SPAN"),
        ("https://catalog.college.emory.edu/academics/departments/spanish-and-portuguese.html", "PORT"),
        ("https://catalog.college.emory.edu/academics/departments/sustainability.html", "SUST"),
        ("https://catalog.college.emory.edu/academics/departments/theater-and-dance.html", "THEA"),
        ("https://catalog.college.emory.edu/academics/departments/wgss.html", "WGS")
        # Paste more here as you find them

        # Paste more here as you find them
    ]

    master_list = []

    # --- STEP 2: RUN THE SCRIPT ---
    for url, prefix in scraping_queue:
        dept_data = scrape_dept(url, prefix)
        master_list.extend(dept_data)
        time.sleep(1)  # Be nice to the server

    # --- STEP 3: SAVE THE RESULTS ---
    output_file = "all_emory_courses.json"
    with open(output_file, "w") as f:
        json.dump(master_list, f, indent=2)

    print(f"\nFINISHED: Saved {len(master_list)} courses to {output_file}")