import json
import mysql.connector
import traceback
import re

# ---------------- DB CONNECTION ----------------
conn = mysql.connector.connect(
    host="localhost",
    user="pathway_user",
    password="pathway_pass",
    database="pathway_planner",
)
cursor = conn.cursor()


# ---------------- NORMALIZATION ----------------
def normalize(req):
    if not req:
        return ""
    req = req.upper()

    # 1. Remove Oxford variants entirely BEFORE tokenizing
    req = re.sub(r"[A-Z]{2,5}_OX\s*\d{3}[A-Z]?", "", req)

    # 2. Clean up resulting mess left by removals
    req = re.sub(r"\(\s*OR\s+", "(", req)
    req = re.sub(r"\s+OR\s+\)", ")", req)
    req = re.sub(r"\b(AND|OR)\s+(AND|OR)\b", r"\1", req)

    # 3. Strip equivalent and noise
    req = re.sub(r"OR EQUIVALENT.*", "", req)
    req = re.sub(r"THIS COURSE REQUIRES", "", req)
    req = req.replace("[", "(").replace("]", ")")

    return req.strip()


TOKEN_REGEX = re.compile(r"\(|\)|\bAND\b|\bOR\b|[A-Z]{2,5}\s*\d{3}[A-Z]?")


def tokenize(req):
    return TOKEN_REGEX.findall(req)


# ---------------- PARSER ----------------



# ---------------- DB HELPERS ----------------
def get_course_id(course_str):
    m = re.match(r"([A-Z]{2,5})\s*(\d{3}[A-Z]?)", course_str)
    if not m: return None
    code, num = m.group(1), m.group(2)
    cursor.execute("SELECT id FROM course WHERE code=%s AND course_num=%s", (code, num))
    row = cursor.fetchone()
    return row[0] if row else None


def clear_existing_prereqs(course_id):
    cursor.execute("""
        DELETE pi FROM prerequisite_item pi
        INNER JOIN prerequisite p ON pi.prerequisite_id = p.id
        WHERE p.course_id = %s
    """, (course_id,))
    cursor.execute("DELETE FROM prerequisite WHERE course_id = %s", (course_id,))


# ---------------- REFINED PARSER WITH PRUNING ----------------
def parse(tokens):
    if not tokens:
        return None

    def get_precedence(op):
        return 1 if op == "OR" else 2

    def parse_expr(i):
        output_queue = []
        operator_stack = []

        while i < len(tokens):
            tok = tokens[i]
            if tok == "(":
                node, i = parse_expr(i + 1)
                if node: output_queue.append(node)
            elif tok == ")":
                break
            elif tok in ("AND", "OR"):
                while (operator_stack and operator_stack[-1] != "(" and
                       get_precedence(operator_stack[-1]) >= get_precedence(tok)):
                    output_queue.append(operator_stack.pop())
                operator_stack.append(tok)
            else:
                output_queue.append({"type": "COURSE", "value": tok, "children": []})
            i += 1

        while operator_stack:
            op = operator_stack.pop()
            if op != "(": output_queue.append(op)

        stack = []
        for item in output_queue:
            if isinstance(item, str):
                if len(stack) < 2:
                    # If an operator is left with only one side (due to Oxford removal),
                    # just keep that one side and discard the operator.
                    continue
                right = stack.pop()
                left = stack.pop()
                stack.append({"type": item, "children": [left, right]})
            else:
                stack.append(item)

        if not stack: return None, i

        # --- NEW: RECURSIVE PRUNING ---
        # If this logic node only has one child, return the child directly.
        # This turns "(MATH 111 OR [Empty])" into "MATH 111"
        curr = stack[0]
        while curr["type"] in ("AND", "OR") and len(curr.get("children", [])) == 1:
            curr = curr["children"][0]

        return curr, i

    tree, _ = parse_expr(0)
    return tree


# ---------------- REFINED PERSISTENCE ----------------
def persist_node(node, target_course_id, is_root=False):
    if not node: return None

    # If the logic has collapsed down to a single course
    if node["type"] == "COURSE":
        c_id = get_course_id(node["value"])
        if not c_id or c_id == target_course_id: return None

        # Even if it's just one course, the DB root must be an 'AND' or 'OR'
        # to match your Spring Boot entity structure.
        cursor.execute(
            "INSERT INTO prerequisite (course_id, type) VALUES (%s, 'AND')",
            (target_course_id if is_root else None,)
        )
        prereq_id = cursor.lastrowid
        cursor.execute(
            "INSERT INTO prerequisite_item (prerequisite_id, required_course_id) VALUES (%s, %s)",
            (prereq_id, c_id)
        )
        return prereq_id

    # Handle Logic Nodes with multiple children
    # First, collect all valid sub-items to see if this node is still necessary
    valid_children = []
    for child in node["children"]:
        if child["type"] == "COURSE":
            c_id = get_course_id(child["value"])
            if c_id and c_id != target_course_id:
                valid_children.append(('COURSE', c_id))
        else:
            # Recursively check nested logic
            valid_children.append(('NESTED', child))

    if not valid_children: return None

    # If removal of Oxford/Self-references left only ONE child, promote it
    if len(valid_children) == 1:
        child_type, child_val = valid_children[0]
        if child_type == 'COURSE':
            # Create the root record as an AND with one item
            cursor.execute("INSERT INTO prerequisite (course_id, type) VALUES (%s, 'AND')",
                           (target_course_id if is_root else None,))
            p_id = cursor.lastrowid
            cursor.execute("INSERT INTO prerequisite_item (prerequisite_id, required_course_id) VALUES (%s, %s)",
                           (p_id, child_val))
            return p_id
        else:
            # If it's a nested logic node, keep drilling down
            return persist_node(child_val, target_course_id, is_root=is_root)

    # Standard Multi-child logic
    cursor.execute(
        "INSERT INTO prerequisite (course_id, type) VALUES (%s, %s)",
        (target_course_id if is_root else None, node["type"])
    )
    prereq_id = cursor.lastrowid

    for c_type, c_val in valid_children:
        if c_type == 'COURSE':
            cursor.execute("INSERT INTO prerequisite_item (prerequisite_id, required_course_id) VALUES (%s, %s)",
                           (prereq_id, c_val))
        else:
            nested_id = persist_node(c_val, target_course_id, is_root=False)
            if nested_id:
                cursor.execute(
                    "INSERT INTO prerequisite_item (prerequisite_id, nested_prerequisite_id) VALUES (%s, %s)",
                    (prereq_id, nested_id))

    return prereq_id

# ---------------- MAIN ----------------
def main():
    try:
        with open("all_emory_courses.json") as f:
            data = json.load(f)
    except FileNotFoundError:
        print("all_emory_courses.json not found")
        return

    processed = 0
    skipped = 0

    for entry in data:
        code, num = entry.get("dept"), entry.get("num")
        requisites = entry.get("requisites")

        cursor.execute("SELECT id FROM course WHERE code=%s AND course_num=%s", (code, num))
        row = cursor.fetchone()
        if not row: continue
        course_id = row[0]

        clear_existing_prereqs(course_id)

        if not requisites or requisites.strip().upper() == "NONE":
            skipped += 1
            continue

        try:
            req_str = normalize(requisites)
            tokens = tokenize(req_str)
            if not tokens:
                skipped += 1
                continue

            tree = parse(tokens)
            if tree:
                persist_node(tree, course_id, is_root=True)
                conn.commit()
                processed += 1
            else:
                skipped += 1

        except Exception:
            conn.rollback()
            print(f"Error on {code} {num}")
            traceback.print_exc()

    print(f"\nTotal Processed: {processed}")
    print(f"Courses Skipped/Empty: {skipped}")
    cursor.close()
    conn.close()


if __name__ == "__main__":
    main()