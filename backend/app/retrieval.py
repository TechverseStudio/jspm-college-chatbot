import re
import difflib
from typing import List, Dict, Any, Optional, Tuple
from app.database import get_connection
from app.schemas import (
    PlacementStat, FeeItem, ContactCard, SourceCitation, WarningCallout
)

# Canonical 2026-27 Fee Structure from Official Notice Image
FEE_SCHEDULE = [
    FeeItem(
        programme="B.Tech (First Year)",
        category="Open / OMS (Income > 8 Lakh)",
        tuition_fee="₹1,26,087",
        development_fee="₹18,913",
        university_fee="₹1,000",
        total_fee="₹1,46,000"
    ),
    FeeItem(
        programme="B.Tech (First Year)",
        category="OBC / EWS / EBC (Boys)",
        tuition_fee="₹63,044",
        development_fee="₹18,913",
        university_fee="₹1,000",
        total_fee="₹82,957"
    ),
    FeeItem(
        programme="B.Tech (First Year)",
        category="NT / SBC / TFWS & OBC/EWS/EBC (Girls)",
        tuition_fee="₹0",
        development_fee="₹18,913",
        university_fee="₹1,000",
        total_fee="₹19,913"
    ),
    FeeItem(
        programme="B.Tech (First Year)",
        category="SC / ST",
        tuition_fee="₹0",
        development_fee="₹0",
        university_fee="₹1,000",
        total_fee="₹1,000"
    ),
    FeeItem(
        programme="B.Tech Direct Second Year (DSY)",
        category="Open / OMS (Income > 8 Lakh)",
        tuition_fee="₹1,15,652",
        development_fee="₹17,348",
        university_fee="₹1,000",
        total_fee="₹1,34,000"
    ),
    FeeItem(
        programme="B.Tech Direct Second Year (DSY)",
        category="OBC / EWS / EBC (Boys)",
        tuition_fee="₹57,826",
        development_fee="₹17,348",
        university_fee="₹1,000",
        total_fee="₹76,174"
    ),
    FeeItem(
        programme="B.Tech Direct Second Year (DSY)",
        category="NT / SBC / TFWS & Girls",
        tuition_fee="₹0",
        development_fee="₹17,348",
        university_fee="₹1,000",
        total_fee="₹18,348"
    ),
    FeeItem(
        programme="B.Tech Direct Second Year (DSY)",
        category="SC / ST",
        tuition_fee="₹0",
        development_fee="₹0",
        university_fee="₹1,000",
        total_fee="₹1,000"
    ),
    FeeItem(
        programme="BCA (First Year)",
        category="Open / OMS",
        tuition_fee="₹45,000",
        development_fee="₹5,000",
        university_fee="₹1,000",
        total_fee="₹51,000"
    ),
    FeeItem(
        programme="MCA (First Year)",
        category="Open / OMS",
        tuition_fee="₹1,24,000",
        development_fee="₹18,500",
        university_fee="₹1,000",
        total_fee="₹1,43,500"
    ),
    FeeItem(
        programme="MBA (First Year)",
        category="Open / OMS",
        tuition_fee="₹1,18,000",
        development_fee="₹17,500",
        university_fee="₹1,000",
        total_fee="₹1,36,500"
    ),
    FeeItem(
        programme="M.Tech (First Year)",
        category="Open / OMS",
        tuition_fee="₹1,18,500",
        development_fee="₹17,500",
        university_fee="₹1,000",
        total_fee="₹1,37,000"
    )
]

# Canonical Year-wise Placement Statistics from Official RSCOE SPIR Page
PLACEMENT_STATISTICS = [
    PlacementStat(
        academic_year="2025-26",
        companies_visited="550+",
        students_placed="800+",
        offers="1,100+",
        max_salary="₹33.8 LPA"
    ),
    PlacementStat(
        academic_year="2024-25",
        companies_visited="500+",
        students_placed="780+",
        offers="1,000+",
        max_salary="₹30 LPA"
    ),
    PlacementStat(
        academic_year="2023-24",
        companies_visited="500+",
        students_placed="765",
        offers="1,000+",
        max_salary="1 Cr. LPA (As published)"
    ),
    PlacementStat(
        academic_year="2022-23",
        companies_visited="200+",
        students_placed="524",
        offers="800+",
        max_salary="₹47 LPA"
    ),
    PlacementStat(
        academic_year="2021-22",
        companies_visited="250+",
        students_placed="647",
        offers="1,100+",
        max_salary="₹42 LPA"
    )
]

# Key Institutional Contacts Directory
OFFICIAL_CONTACTS = [
    ContactCard(
        name="Dr. Avinash Badadhe",
        role="Principal",
        institute="RSCOE",
        phone="+91-9850557177",
        email="principal@jspmrscoe.edu.in",
        source_url="https://www.jspmrscoe.edu.in/admissionenquiry/contact-no"
    ),
    ContactCard(
        name="Campus Director Desk",
        role="Director Desk",
        institute="RSCOE",
        phone="+91-8149094248",
        email="principal@jspmrscoe.edu.in",
        source_url="https://www.jspmrscoe.edu.in/admissionenquiry/contact-no"
    ),
    ContactCard(
        name="Dr. Saurabh Tayde",
        role="Training & Placement Head (SPIR)",
        institute="RSCOE",
        phone="+91-9552046715",
        email="principal@jspmrscoe.edu.in",
        source_url="https://www.jspmrscoe.edu.in/admissionenquiry/contact-no"
    ),
    ContactCard(
        name="Dr. Amol Patil / Dr. Satyajit Kasar",
        role="First Year B.Tech Admissions",
        institute="RSCOE",
        phone="+91-9172390964",
        email="principal@jspmrscoe.edu.in",
        source_url="https://www.jspmrscoe.edu.in/admissionenquiry/contact-no"
    ),
    ContactCard(
        name="Prof. Ashish Malgave / Dr. P. S. Patil",
        role="Direct Second Year (DSY) Engineering Admissions",
        institute="RSCOE",
        phone="+91-9921901045",
        email="principal@jspmrscoe.edu.in",
        source_url="https://www.jspmrscoe.edu.in/admissionenquiry/contact-no"
    ),
    ContactCard(
        name="Dr. Seema V. Kedar",
        role="HOD, Computer Engineering",
        institute="RSCOE",
        phone="020-22933423",
        email="hod_comp@jspmrscoe.edu.in",
        department="Computer Engineering",
        source_url="https://www.jspmrscoe.edu.in/ComputerEngineering/contact-us"
    ),
    ContactCard(
        name="Prof. Dr. K. P. Moholkar",
        role="HOD, Computer Science & Business Systems (CSBS)",
        institute="RSCOE",
        phone="+91-9960092262",
        email="hod_csbs@jspmrscoe.edu.in",
        department="CSBS",
        source_url="https://www.jspmrscoe.edu.in/computerscience-and-businesssystems/contact-us"
    ),
    ContactCard(
        name="Dr. S. C. Wagaj",
        role="HOD, Electronics & Telecommunication (E&TC)",
        institute="RSCOE",
        phone="+91-9970063553",
        email="scwagaj@gmail.com",
        department="E&TC",
        source_url="https://www.jspmrscoe.edu.in/eandtc-engineering/contact-us"
    ),
    ContactCard(
        name="Dr. S. N. Khan",
        role="HOD, Mechanical Engineering",
        institute="RSCOE",
        phone="+91-9373087321",
        email="hod_mech@jspmrscoe.edu.in",
        department="Mechanical Engineering",
        source_url="https://www.jspmrscoe.edu.in/mechanical-engineering/contact-us"
    ),
    ContactCard(
        name="Dr. Rinku Dulloo",
        role="HOD, MCA & BCA",
        institute="RSCOE",
        phone="+91-9552565101",
        email="hod_mca@jspmrscoe.edu.in",
        department="MCA & BCA",
        source_url="https://www.jspmrscoe.edu.in/dca/contact-us"
    ),
    ContactCard(
        name="Mr. Shivaji Surve",
        role="Hostel Rector / In-Charge",
        institute="RSCOE",
        phone="+91-9604275533",
        source_url="https://www.jspmrscoe.edu.in/admissionenquiry/contact-no"
    ),
    ContactCard(
        name="Mr. Umesh Ubale",
        role="College Bus / Transport In-Charge",
        institute="RSCOE",
        phone="+91-9763722020",
        source_url="https://www.jspmrscoe.edu.in/admissionenquiry/contact-no"
    ),
    ContactCard(
        name="Prof. Anil Tankar",
        role="B.Pharm Admissions & T&P Head",
        institute="RSCOPR",
        phone="+91-9822729314",
        email="anil.tankar@gmail.com",
        department="Pharmacy",
        source_url="https://jspmrscopr.edu.in/ContactUs/20"
    ),
    ContactCard(
        name="Dr. Prafulla Chaudhari",
        role="M.Pharm Admissions In-Charge",
        institute="RSCOPR",
        phone="+91-9890626393",
        email="pchaudhari@jspmrscopr.edu.in",
        department="Pharmacy",
        source_url="https://jspmrscopr.edu.in/Admission/96"
    ),
    ContactCard(
        name="Dr. S. S. Gaikwad",
        role="Principal, Polytechnic",
        institute="POLYTECHNIC",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. V. I. Thakare",
        role="HOD, Computer Engineering (Polytechnic)",
        institute="POLYTECHNIC",
        department="Computer Engineering",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. S. U. Puri",
        role="Department Academic Coordinator (DAC)",
        institute="POLYTECHNIC",
        department="Computer Engineering",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. H. N. Bhandare",
        role="Timetable In-charge",
        institute="POLYTECHNIC",
        department="Computer Engineering",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. Madhuri Patil-Maske",
        role="Faculty, Electronics & Telecommunication (E&TC)",
        institute="POLYTECHNIC",
        department="E&TC",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. Amita Dubey",
        role="Faculty, Electronics & Telecommunication (E&TC)",
        institute="POLYTECHNIC",
        department="E&TC",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. Aparna Deshpande",
        role="Faculty, Electronics & Telecommunication (E&TC)",
        institute="POLYTECHNIC",
        department="E&TC",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. Pournima Bhise",
        role="Faculty, Electronics & Telecommunication (E&TC)",
        institute="POLYTECHNIC",
        department="E&TC",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. Savitha Bajal",
        role="Faculty, Electronics & Telecommunication (E&TC)",
        institute="POLYTECHNIC",
        department="E&TC",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    ),
    ContactCard(
        name="Mrs. Pallavi Shah",
        role="Faculty, Electronics & Telecommunication (E&TC)",
        institute="POLYTECHNIC",
        department="E&TC",
        source_url="https://polytechnic.jspmrscoe.edu.in/"
    )
]

STOP_WORDS = {
    "what", "is", "the", "and", "for", "that", "this", "with", "from", "are", "was",
    "has", "have", "not", "but", "how", "can", "who", "all", "any", "some", "where",
    "when", "which", "about", "tell", "give", "show", "does", "did", "will", "much"
}

KNOWN_EXTERNAL_INSTITUTES = {
    "oxford", "cambridge", "harvard", "stanford", "mit", "coep", "pict", "vit",
    "vjti", "iit", "nit", "bits", "symbiosis", "dy patil", "bharati vidyapeeth"
}

def sanitize_query(query: str) -> str:
    cleaned = re.sub(r'[^\w\s]', ' ', query)
    tokens = [t.strip() for t in cleaned.split() if len(t.strip()) > 1 and t.lower() not in STOP_WORDS]
    return " OR ".join(tokens) if tokens else "*"

def check_conflicts(query: str, institute: str) -> List[WarningCallout]:
    warnings = []
    q_lower = query.lower()
    
    # Highest package / placement package conflict check
    if any(k in q_lower for k in ["highest", "package", "salary", "1 cr", "33.8", "42 lpa", "maximum salary"]):
        warnings.append(WarningCallout(
            title="Institutional Data Discrepancy: Highest Salary Package",
            message=(
                "Please note an official discrepancy on RSCOE's published placement portal: "
                "The year-wise placement table reports 2025-26 maximum package as ₹33.8 LPA, "
                "2024-25 as ₹30 LPA, and 2023-24 verbatim as '1 Cr. LPA'. "
                "However, the summary text on the same page highlights packages up to ₹42 LPA (Amazon). "
                "These figures reflect different reporting standards across page sections. "
                "Always verify current confirmed compensation figures directly with the SPIR Placement Cell."
            ),
            severity="conflict",
            sources=[
                "https://jspmrscoe.edu.in/placements/placementprocessstatics"
            ]
        ))

    # Cutoffs warning
    if any(k in q_lower for k in ["cutoff", "cut off", "mht-cet", "jee", "percentile", "rank"]):
        warnings.append(WarningCallout(
            title="Annual Cutoff Revision Notice",
            message=(
                "Cutoffs fluctuate every academic year based on CAP round seat availability, exam difficulty, "
                "and candidate merit. Chatbot estimates are not guarantees of admission. "
                "Refer exclusively to the latest Maharashtra State CET Cell CAP round matrices."
            ),
            severity="warning",
            sources=[
                "https://cetcell.mahacet.org/",
                "https://jspmrscoe.edu.in/"
            ]
        ))

    # Fee warning
    if any(k in q_lower for k in ["fee", "fees", "tuition", "cost", "scholarship", "ebc", "tfws"]):
        warnings.append(WarningCallout(
            title="Regulatory Fee Notice (2026-27)",
            message=(
                "Fee amounts are interim approved schedules regulated by the Maharashtra Fee Regulating Authority (FRA). "
                "Category concessions (OBC/EBC/TFWS/SC/ST) depend on valid income certificates, CAP allotment, "
                "and MahaDBT approval. Verify final payable dues at the institute accounts section."
            ),
            severity="info",
            sources=[
                "https://jspmrscoe.edu.in/20-July-2026/fee-structure-2026-27.jpeg"
            ]
        ))

    # Polytechnic notice
    if any(k in q_lower for k in ["polytechnic", "diploma", "tyej", "tyco"]):
        warnings.append(WarningCallout(
            title="Official Polytechnic (Diploma Engineering) Guidance",
            message=(
                "JSPM's Rajarshi Shahu College of Engineering (Polytechnic) is located at Tathawade, Pune – 411033. "
                "Affiliated with MSBTE (https://msbte.ac.in/). Official portals: "
                "https://polytechnic.jspmrscoe.edu.in/ and https://www.jspmrscoe.edu.in/Diploma."
            ),
            severity="info",
            sources=[
                "https://polytechnic.jspmrscoe.edu.in/",
                "https://www.jspmrscoe.edu.in/Diploma",
                "https://msbte.ac.in/"
            ]
        ))

    return warnings

def retrieve_relevant_evidence(query: str, institute: str = "ALL", limit: int = 4) -> Dict[str, Any]:
    conn = get_connection()
    cursor = conn.cursor()

    fts_query = sanitize_query(query)
    params = [fts_query]

    sql = """
    SELECT
        kc.id,
        kc.institute,
        kc.category,
        kc.section_title,
        kc.chunk_text,
        kc.source_url,
        kc.academic_year,
        kc.is_seed,
        kc.has_conflict,
        bm25(knowledge_fts) as rank_score
    FROM knowledge_fts kf
    JOIN knowledge_chunks kc ON kf.rowid = kc.id
    JOIN documents d ON kc.document_id = d.id
    LEFT JOIN sources s ON d.source_id = s.id
    WHERE knowledge_fts MATCH ?
      AND (s.approved_for_answering = 1 OR s.approved_for_answering IS NULL)
    """

    if institute and institute != "ALL":
        sql += " AND (kc.institute = ? OR kc.institute = 'ALL')"
        params.append(institute)

    sql += " ORDER BY rank_score ASC LIMIT ?"
    params.append(limit)

    cursor.execute(sql, params)
    rows = cursor.fetchall()

    chunks = []
    citations = []
    seen_urls = set()

    for r in rows:
        chunks.append({
            "id": r["id"],
            "institute": r["institute"],
            "category": r["category"],
            "section_title": r["section_title"],
            "text": r["chunk_text"],
            "source_url": r["source_url"],
            "academic_year": r["academic_year"],
            "score": r["rank_score"]
        })
        if r["source_url"] and r["source_url"] not in seen_urls:
            seen_urls.add(r["source_url"])
            citations.append(SourceCitation(
                title=r["section_title"],
                url=r["source_url"],
                institute=r["institute"],
                retrieved_date="2026-10-18",
                status="VERIFIED"
            ))

    conn.close()

    # Calculate token coverage confidence excluding stop words
    q_tokens = [t for t in re.findall(r'\b[a-zA-Z0-9]{3,}\b', query.lower()) if t not in STOP_WORDS]
    evidence_text = " ".join([c["text"] for c in chunks]).lower()
    matched_tokens = [t for t in q_tokens if t in evidence_text]
    confidence = len(matched_tokens) / max(len(q_tokens), 1) if q_tokens else 0.0

    # If query specifically mentions an external or rival institution, mark out-of-domain
    is_out_of_domain = any(ext in query.lower() for ext in KNOWN_EXTERNAL_INSTITUTES)
    if is_out_of_domain:
        confidence = 0.0
        chunks = []
        citations = []

    # Attach cards based on intent ONLY if within domain
    q_lower = query.lower()
    placement_cards = None
    fee_cards = None
    contact_cards = None

    if confidence >= 0.3:
        if any(k in q_lower for k in ["placement", "package", "salary", "recruit", "placed", "offer", "spir"]):
            placement_cards = PLACEMENT_STATISTICS

        if any(k in q_lower for k in ["fee", "tuition", "cost", "development fee", "b.tech fee", "mba fee", "mca fee"]):
            if "bca" in q_lower:
                fee_cards = [f for f in FEE_SCHEDULE if "bca" in f.programme.lower()]
            elif "mca" in q_lower:
                fee_cards = [f for f in FEE_SCHEDULE if "mca" in f.programme.lower()]
            elif "mba" in q_lower:
                fee_cards = [f for f in FEE_SCHEDULE if "mba" in f.programme.lower()]
            elif "dsy" in q_lower or "direct second" in q_lower:
                fee_cards = [f for f in FEE_SCHEDULE if "direct second" in f.programme.lower()]
            else:
                fee_cards = FEE_SCHEDULE

        if any(k in q_lower for k in ["contact", "phone", "email", "hod", "principal", "director", "hostel", "bus", "transport", "reach"]):
            matched = []
            for c in OFFICIAL_CONTACTS:
                if institute != "ALL" and c.institute != institute and c.institute != "ALL":
                    continue
                if "hod" in q_lower and "hod" not in c.role.lower():
                    continue
                if "hostel" in q_lower and "hostel" not in c.role.lower():
                    continue
                if "bus" in q_lower and "bus" not in c.role.lower():
                    continue
                matched.append(c)
            contact_cards = matched if matched else OFFICIAL_CONTACTS[:6]

    warnings = check_conflicts(query, institute)

    return {
        "chunks": chunks,
        "confidence": round(confidence, 2),
        "citations": citations,
        "warnings": warnings,
        "placement_stats": placement_cards,
        "fee_breakdown": fee_cards,
        "contact_cards": contact_cards
    }

if __name__ == "__main__":
    res = retrieve_relevant_evidence("What is the highest package in RSCOE?")
    print(f"Chunks found: {len(res['chunks'])}, Confidence: {res['confidence']}")
    print(f"Warnings: {len(res['warnings'])}")
    if res['placement_stats']:
        print(f"Placement stats attached: {len(res['placement_stats'])}")
