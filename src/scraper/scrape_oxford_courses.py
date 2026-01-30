# scrape_ox_classes.py                                                                                                                                                                                                                                      
import requests
from bs4 import BeautifulSoup
import re
import json
import time

BASE_URL = "https://oxford.emory.edu/catalog/course-descriptions/"


def get_department_links():
    """Get all department page links from the index."""
    resp = requests.get(BASE_URL + "index.html")
    soup = BeautifulSoup(resp.text, 'html.parser')

    links = []
    for a in soup.find_all('a', href=True):
        href = a['href']
        # Match department pages like "mathematics.html"
        if (href.endswith('.html') and
                href != 'index.html' and
                not href.startswith('http') and
                not href.startswith('../') and
                not href.startswith('//')):
            links.append(href)

    return list(set(links))


def parse_department_page(dept_url):
    resp = requests.get(dept_url)
    # Force UTF-8 to prevent the 'â€”' garbled text
    resp.encoding = 'utf-8'

    soup = BeautifulSoup(resp.text, 'html.parser')
    courses = []

    sections = soup.find_all('div', class_='section-content')

    for section in sections:
        h2 = section.find('h2')
        if not h2: continue

        # Clean up whitespace and non-breaking spaces
        raw_title = h2.get_text(" ", strip=True)
        clean_header = " ".join(raw_title.split())

        # NEW REGEX STRATEGY:
        # Group 1: Dept Name (All caps, spaces, & allowed)
        # Group 2: Course Num (3 digits + optional R/suffix)
        # Separator: We use [^A-Z0-9\s\(]+ to match ANY "junk" or dashes
        # Group 3: Title
        # Group 4: Optional GER
        pattern = r'^([A-Z\s&]+?)\s+(\d{3}[A-Z]*)[^A-Z0-9\s\(]+\s*(.*?)(?:\s*\(([A-Z]{2,})\))?$'

        match = re.match(pattern, clean_header)

        if not match:
            # Fallback for headers without a title separator (unlikely but safe)
            pattern_fallback = r'^([A-Z\s&]+?)\s+(\d{3}[A-Z]*)\s+(.*?)(?:\s*\(([A-Z]{2,})\))?$'
            match = re.match(pattern_fallback, clean_header)

        if not match:
            print(f"  DEBUG: Still failed: '{clean_header}'")
            continue

        dept_name, course_num, title, designation = match.groups()

        # ... (rest of your description and credit logic) ...
        description = ""
        p_tag = section.find('p')
        if p_tag:
            description = p_tag.get_text(" ", strip=True)

        courses.append({
            "dept": dept_name_to_code(dept_name.strip()),
            "num": course_num,
            "title": title.strip() if title else "",
            "description": description,
            "credits": 3,  # Use your existing credit parsing logic here
            "ger": designation
        })

    return courses


def dept_name_to_code(name):
    """Convert department name to Oxford course code."""
    mappings = {
        # Exact matches from h2 tags to official codes
        "ACCOUNTING": "ACT_OX",
        "AFRICAN AMERICAN STUDIES": "AAS_OX",
        "AFRICAN STUDIES": "AFS_OX",
        "AMERICAN STUDIES": "AMST_OX",
        "ANTHROPOLOGY": "ANTH_OX",
        "ARABIC": "ARAB_OX",
        "ART": "ART_OX",
        "ART HISTORY": "ART_OX",
        "ASTRONOMY": "ASTR_OX",
        "BIOLOGY": "BIOL_OX",
        "CHEMISTRY": "CHEM_OX",
        "CHINESE": "CHN_OX",
        "CLASSICS": "CL_OX",
        "COMPUTER SCIENCE": "CS_OX",
        "CREATIVE WRITING": "ENGCW_OX",
        "DANCE": "DANC_OX",
        "DISCOVERY SEMINAR": "DSC_OX",
        "ECONOMICS": "ECON_OX",
        "ENGLISH": "ENG_OX",
        "ENVIRONMENTAL SCIENCES": "ENVS_OX",
        "ENVIRONMENTAL SCIENCE": "ENVS_OX",
        "FILM AND MEDIA": "FILM_OX",
        "FILM": "FILM_OX",
        "FRENCH": "FREN_OX",
        "GEOLOGY": "GEOL_OX",
        "GERMAN": "GER_OX",
        "GRADUATE": "GRAD_OX",
        "HISTORY": "HIST_OX",
        "HUMAN HEALTH": "HLTH_OX",
        "HEALTH": "HLTH_OX",
        "INTERDISCIPLINARY STUDIES": "IDS_OX",
        "LATIN AMERICAN AND CARIBBEAN STUDIES": "LACS_OX",
        "LATIN": "LAT_OX",
        "LINGUISTICS": "LING_OX",
        "MATHEMATICS": "MATH_OX",
        "MIDDLE EASTERN AND SOUTH ASIAN STUDIES": "MESAS_OX",
        "MIDDLE EASTERN SOUTH ASIAN STUDIES": "MESAS_OX",
        "MUSIC": "MUS_OX",
        "INTERNSHIP": "INTER_OX",
        "OXFORD STUDIES": "OXST_OX",
        "PHILOSOPHY": "PHIL_OX",
        "PHYSICAL EDUCATION": "PE_OX",
        "PHYSICS": "PHYS_OX",
        "POLITICAL SCIENCE": "POLS_OX",
        "PSYCHOLOGY": "PSYC_OX",
        "RELIGION": "REL_OX",
        "SPANISH": "SPAN_OX",
        "THEATER": "THEA_OX",
        "THEATER STUDIES": "THEA_OX",
        "THEATRE": "THEA_OX",
        "WOMEN'S GENDER AND SEXUALITY STUDIES": "WGS_OX",
        "WOMEN GENDER AND SEXUALITY STUDIES": "WGS_OX",
        "SOCIOLOGY": "SOC_OX",
        "NEUROSCIENCE": "NBB_OX",
        "NURSING": "NRSG_OX",
        "QUANTITATIVE THEORY AND METHODS": "QTM_OX",
    }

    # Try exact match
    if name in mappings:
        return mappings[name]

        # Try with common variations
    name_clean = name.replace("'", "").replace("-", " ")
    if name_clean in mappings:
        return mappings[name_clean]

        # Fallback - shouldn't happen if mappings are complete
    print(f"  WARNING: Unknown department '{name}'")
    words = name.split()
    code = ''.join(w[0] for w in words).upper() if len(words) > 1 else name[:4].upper()
    return code + "_OX"


def scrape_all_courses():
    """Scrape all Oxford courses."""
    all_courses = []

    print("Getting department links...")
    dept_links = get_department_links()
    print(f"Found {len(dept_links)} departments: {dept_links[:5]}...")

    for i, link in enumerate(dept_links):
        url = BASE_URL + link
        print(f"[{i + 1}/{len(dept_links)}] Scraping {link}...", end=" ")

        try:
            courses = parse_department_page(url)
            all_courses.extend(courses)
            print(f"Found {len(courses)} courses")
        except Exception as e:
            print(f"Error: {e}")

        time.sleep(0.3)

    return all_courses


if __name__ == "__main__":
    courses = scrape_all_courses()
    print(f"\nTotal: {len(courses)} courses")

    with open("oxford_courses.json", "w") as f:
        json.dump(courses, f, indent=2)
    print("Saved to oxford_courses.json")