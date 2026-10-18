import re
import hashlib
from pathlib import Path
from typing import List, Dict, Any, Tuple
from app.config import settings
from app.database import get_connection, init_db

def compute_hash(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()

SEED_SOURCES = [
    {
        "url": "https://jspmrscoe.edu.in/",
        "institute": "RSCOE",
        "topic": "Official Portal & Overview",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://www.jspmrscoe.edu.in/contact-us",
        "institute": "RSCOE",
        "topic": "General Contacts",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://www.jspmrscoe.edu.in/admissionenquiry/contact-no",
        "institute": "RSCOE",
        "topic": "Admission & Section Contacts",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://jspmrscoe.edu.in/placements/placementprocessstatics",
        "institute": "RSCOE",
        "topic": "Placements & Internships",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://jspmrscoe.edu.in/20-July-2026/fee-structure-2026-27.jpeg",
        "institute": "RSCOE",
        "topic": "2026-27 Approved Fee Schedule",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://jspmrscopr.edu.in/",
        "institute": "RSCOPR",
        "topic": "Pharmacy College Portal",
        "retrieved_date": "2026-10-18",
        "scraping_status": "ACCESS_RESTRICTED",
        "status_code": 403,
        "approved": 1
    },
    {
        "url": "https://jspmrscopr.edu.in/ContactUs/20",
        "institute": "RSCOPR",
        "topic": "Pharmacy Contacts & Admissions",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://jspmrscopr.edu.in/NACC-Table/163",
        "institute": "RSCOPR",
        "topic": "Pharmacy FRA Fee Proposals",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://jspmrscoed.edu.in/",
        "institute": "RSCOED",
        "topic": "College of Education Portal",
        "retrieved_date": "2026-10-18",
        "scraping_status": "ACCESS_RESTRICTED",
        "status_code": 403,
        "approved": 1
    },
    {
        "url": "https://jspmrscoed.edu.in/pdf/par_ncte/Mandatory%20Disclosure.pdf",
        "institute": "RSCOED",
        "topic": "B.Ed Mandatory Disclosure",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://jspmjims.edu.in/",
        "institute": "JIMS",
        "topic": "Management Institute Portal",
        "retrieved_date": "2026-10-18",
        "scraping_status": "ACCESS_RESTRICTED",
        "status_code": 403,
        "approved": 1
    },
    {
        "url": "https://jspmjims.edu.in/storage/NAAC/C6/6.2.3ERPDocuments.pdf",
        "institute": "JIMS",
        "topic": "JIMS Programme Details & Intake",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://polytechnic.jspmrscoe.edu.in/",
        "institute": "POLYTECHNIC",
        "topic": "Official Polytechnic Portal",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://www.jspmrscoe.edu.in/Diploma",
        "institute": "POLYTECHNIC",
        "topic": "Official Diploma Courses Page",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://msbte.ac.in/",
        "institute": "POLYTECHNIC",
        "topic": "MSBTE Official Website",
        "retrieved_date": "2026-10-18",
        "scraping_status": "SEED_PRESERVED",
        "status_code": 200,
        "approved": 1
    },
    {
        "url": "https://jspmbps.edu.in/",
        "institute": "POLYTECHNIC",
        "topic": "Polytechnic Institute Secondary Portal",
        "retrieved_date": "2026-10-18",
        "scraping_status": "ACCESS_RESTRICTED",
        "status_code": 403,
        "approved": 1
    }
]

KNOWN_CONFLICTS = [
    {
        "topic": "RSCOE Highest Package Statistics",
        "institute": "RSCOE",
        "statement_a": "Year-wise table records 2025-26 maximum salary as ₹33.8 LPA, 2024-25 as ₹30 LPA, and 2023-24 as '1 Cr. LPA'.",
        "source_a": "https://jspmrscoe.edu.in/placements/placementprocessstatics (Year-wise Table)",
        "statement_b": "Summary paragraph on the same page highlights packages up to ₹42 LPA (Amazon).",
        "source_b": "https://jspmrscoe.edu.in/placements/placementprocessstatics (Summary Overview)",
        "explanation": "Published college figures differ between the summary paragraph (₹42 LPA Amazon) and the year-wise placement table (2025-26: ₹33.8 LPA, 2023-24: 1 Cr. LPA).",
        "resolution_guidance": "Do not reconcile or pick a single figure. State the exact academic year and figure from the official table, note that published figures differ across sections, and direct the user to SPIR (Student Progression & Industry Relations) Cell."
    },
    {
        "topic": "Polytechnic Domain & Verification",
        "institute": "POLYTECHNIC",
        "statement_a": "Supplied polytechnic domain is https://jspmbps.edu.in/.",
        "source_a": "https://jspmbps.edu.in/",
        "statement_b": "RSCOE website links to a separate section at https://polytechnic.jspmrscoe.edu.in/.",
        "source_b": "https://polytechnic.jspmrscoe.edu.in/",
        "explanation": "It is not officially confirmed whether jspmbps.edu.in and polytechnic.jspmrscoe.edu.in refer to the same polytechnic or separate entities.",
        "resolution_guidance": "Do not merge polytechnic figures with engineering or assume intake without official verification."
    }
]

def parse_markdown_sections(md_text: str) -> List[Dict[str, Any]]:
    """Splits markdown into logical knowledge chunks by H2/H3 headers."""
    chunks = []
    lines = md_text.splitlines()
    current_h2 = "General"
    current_h3 = ""
    current_content = []

    def flush_chunk():
        if current_content:
            text = "\n".join(current_content).strip()
            if len(text) > 40:
                title = f"{current_h2} - {current_h3}" if current_h3 else current_h2
                chunks.append({
                    "h2": current_h2,
                    "h3": current_h3,
                    "title": title,
                    "text": text
                })
            current_content.clear()

    for line in lines:
        if line.startswith("## "):
            flush_chunk()
            current_h2 = line.replace("## ", "").strip()
            current_h3 = ""
        elif line.startswith("### "):
            flush_chunk()
            current_h3 = line.replace("### ", "").strip()
        else:
            current_content.append(line)

    flush_chunk()
    return chunks

def extract_institute(title: str, text: str) -> str:
    combined = (title + " " + text).lower()
    if "pharmacy" in combined or "rscopr" in combined:
        return "RSCOPR"
    if "education" in combined or "rscoed" in combined or "b.ed" in combined:
        return "RSCOED"
    if "management" in combined or "jims" in combined or "mba" in combined and "jims" in combined:
        return "JIMS"
    if "polytechnic" in combined or "diploma" in combined or "tyej" in combined or "tyco" in combined or "msbte" in combined:
        return "POLYTECHNIC"
    if "rscoe" in combined or "engineering" in combined or "b.tech" in combined:
        return "RSCOE"
    return "ALL"

def extract_category(title: str, text: str) -> str:
    combined = (title + " " + text).lower()
    if "timetable" in combined or "time table" in combined or "schedule" in combined:
        return "timetable"
    if "staff" in combined or "faculty" in combined:
        return "faculty"
    if "fee" in combined or "tuition" in combined:
        return "fees"
    if "placement" in combined or "recruiter" in combined or "package" in combined or "salary" in combined or "internship" in combined:
        return "placements"
    if "cutoff" in combined or "mht-cet" in combined or "jee" in combined:
        return "cutoffs"
    if "dsy" in combined or "direct second year" in combined or "dse" in combined:
        return "dsy_admissions"
    if "contact" in combined or "phone" in combined or "email" in combined or "hod" in combined:
        return "contacts"
    if "hostel" in combined or "bus" in combined or "transport" in combined:
        return "campus_facilities"
    if "scholarship" in combined or "mahadbt" in combined:
        return "scholarships"
    if "admission" in combined or "eligibility" in combined or "course" in combined:
        return "admissions"
    return "general"

def extract_keywords(title: str, text: str) -> str:
    words = re.findall(r'\b[A-Za-z0-9_-]{3,}\b', (title + " " + text).lower())
    common_stops = {"the", "and", "for", "that", "this", "with", "from", "are", "was", "has", "have", "not", "but"}
    unique = []
    for w in words:
        if w not in common_stops and w not in unique:
            unique.append(w)
            if len(unique) >= 40:
                break
    return " ".join(unique)

def extract_source_url(text: str) -> str:
    urls = re.findall(r'https?://[^\s\)\]]+', text)
    if urls:
        # filter out image extensions if possible, or return first
        return urls[0].rstrip('.,;')
    return "https://jspmrscoe.edu.in/"

def ingest_seed_data(force: bool = False):
    init_db()
    conn = get_connection()
    cursor = conn.cursor()

    # Check if seed is already ingested
    cursor.execute("SELECT COUNT(*) as cnt FROM knowledge_chunks WHERE is_seed = 1")
    count = cursor.fetchone()["cnt"]
    if count > 0 and not force:
        print(f"Seed data already ingested ({count} chunks). Skipping.")
        conn.close()
        return

    if force:
        print("Clearing prior seed data chunks for refresh...")
        cursor.execute("DELETE FROM knowledge_chunks WHERE is_seed = 1")
        cursor.execute("DELETE FROM documents WHERE url = 'local://seed_kb.md'")

    print("Ingesting initial JSPM seed knowledge base...")

    # Insert known seed sources
    for src in SEED_SOURCES:
        cursor.execute("""
        INSERT OR REPLACE INTO sources (
            url, institute, topic, retrieved_date, last_successful_fetch, content_hash,
            scraping_status, status_code, approved_for_answering, error_message
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            src["url"], src["institute"], src["topic"], src["retrieved_date"],
            src["retrieved_date"] if src["status_code"] == 200 else None,
            compute_hash(src["url"]), src["scraping_status"], src["status_code"],
            src["approved"], None if src["status_code"] == 200 else f"HTTP {src['status_code']} access restriction"
        ))

    # Insert known conflicts
    for c in KNOWN_CONFLICTS:
        cursor.execute("""
        INSERT OR REPLACE INTO conflicts (
            topic, institute, statement_a, source_a, statement_b, source_b, explanation, resolution_guidance
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            c["topic"], c["institute"], c["statement_a"], c["source_a"],
            c["statement_b"], c["source_b"], c["explanation"], c["resolution_guidance"]
        ))

    # Read markdown seed file
    seed_path = settings.SEED_DOCUMENT_PATH
    if not seed_path.exists():
        fallback_path = Path(__file__).resolve().parent.parent.parent / "JSPM_RSCOE_College_Chatbot_Knowledge_Base.md"
        if fallback_path.exists():
            seed_path = fallback_path

    if not seed_path.exists():
        raise FileNotFoundError(f"Seed knowledge base document not found at {seed_path}")

    with open(seed_path, "r", encoding="utf-8") as f:
        md_text = f.read()

    # Insert main seed document record
    doc_hash = compute_hash(md_text)
    cursor.execute("""
    INSERT INTO documents (source_id, institute, category, title, content, url, academic_year)
    VALUES (1, 'ALL', 'seed_document', 'JSPM Tathawade Campus Knowledge Base', ?, 'local://seed_kb.md', '2026-27')
    """, (md_text,))
    doc_id = cursor.lastrowid

    # Parse sections
    parsed_sections = parse_markdown_sections(md_text)

    # Insert chunks
    for sec in parsed_sections:
        inst = extract_institute(sec["title"], sec["text"])
        cat = extract_category(sec["title"], sec["text"])
        src_url = extract_source_url(sec["text"])
        kw = extract_keywords(sec["title"], sec["text"])
        has_conf = 1 if ("conflict" in sec["title"].lower() or "warning" in sec["title"].lower() or "package" in sec["title"].lower()) else 0
        acad_yr = "2026-27" if "2026" in sec["text"] else ("2025-26" if "2025" in sec["text"] else "2024-25")

        cursor.execute("""
        INSERT INTO knowledge_chunks (
            document_id, institute, category, section_title, chunk_text,
            source_url, academic_year, is_seed, has_conflict, keywords
        ) VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?)
        """, (
            doc_id, inst, cat, sec["title"], sec["text"], src_url, acad_yr, has_conf, kw
        ))

    conn.commit()
    conn.close()
    print(f"Successfully ingested {len(parsed_sections)} knowledge chunks from seed document.")

if __name__ == "__main__":
    ingest_seed_data(force=True)
