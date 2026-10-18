import re
from typing import Dict, Any, List
import httpx
from app.config import settings
from app.schemas import ChatResponse, SourceCitation, WarningCallout

def generate_retrieval_only_answer(query: str, retrieval: Dict[str, Any], institute: str) -> str:
    chunks = retrieval["chunks"]
    confidence = retrieval["confidence"]
    q_lower = query.lower()

    # Out of domain or zero evidence fallback
    if confidence < 0.25 or not chunks:
        inst_label = f" for {institute}" if institute != "ALL" else ""
        return (
            f"### Official Inquiry Guidance{inst_label}\n\n"
            f"I could not locate verified official documentation regarding **\"{query}\"** "
            f"in the current approved JSPM Tathawade Campus knowledge base or official portals.\n\n"
            f"**Recommended Steps:**\n"
            f"- For Engineering (RSCOE) admissions, fees, and cutoffs: Visit [https://jspmrscoe.edu.in/](https://jspmrscoe.edu.in/) or call Dr. Amol Patil at `+91-9172390964`.\n"
            f"- For Pharmacy (RSCOPR): Visit [https://jspmrscopr.edu.in/](https://jspmrscopr.edu.in/) or call Prof. Anil Tankar at `+91-9822729314`.\n"
            f"- For Management (JIMS): Visit [https://jspmjims.edu.in/](https://jspmjims.edu.in/) or email `director_jims@jspm.edu.in`.\n"
            f"- For College of Education: Visit [https://jspmrscoed.edu.in/](https://jspmrscoed.edu.in/) or email `principal@jspmrscoed.edu.in`.\n"
            f"- For State CET Cell CAP round admissions and merit rules, refer to [https://cetcell.mahacet.org/](https://cetcell.mahacet.org/).\n\n"
            f"> *Note: Academic intake, cutoffs, and regulatory fees are subject to revision each academic year by the competent government authorities.*"
        )

    # Intent-specific synthesized responses
    if any(k in q_lower for k in ["highest package", "highest salary", "maximum package", "1 cr", "33.8"]):
        return (
            "### RSCOE Placement Package Highlights\n\n"
            "According to the official **RSCOE Student Progression and Industry Relations (SPIR) Cell** portal:\n\n"
            "- **Academic Year 2025–26:** Highest recorded package is **₹33.8 LPA** (with 550+ companies visited, 800+ students placed, 1,100+ offers).\n"
            "- **Academic Year 2024–25:** Highest package was **₹30 LPA** (780+ placed across 500+ companies).\n"
            "- **Academic Year 2023–24:** Published year-wise table records the maximum salary verbatim as **\"1 Cr. LPA\"**.\n"
            "- **Summary Overview:** The homepage highlight text separately reports packages up to **₹42 LPA (Amazon)**.\n\n"
            "**Major Corporate Recruiters:**\n"
            "TCS, Capgemini, Cognizant, Infosys, Accenture, Nutanix, Data Axle, Sulzer India, KPIT, LTIMindtree, Bosch, and Tata Motors.\n\n"
            "> **Official Verification Advisory:** Published package figures differ between the summary overview and historical year-wise tables. "
            "Students and recruiters are advised to verify audited placement statistics with the SPIR Placement Cell (Dr. Saurabh Tayde: `+91-9552046715`)."
        )

    if any(k in q_lower for k in ["fee", "tuition", "development fee", "b.tech fee", "cost"]):
        return (
            "### Approved 2026–27 Fee Schedule (RSCOE)\n\n"
            "The following figures are transcribed directly from the official **Fees Structure for Academic Year 2026–27** notification:\n\n"
            "- **First-Year B.Tech (Open / OMS / Income > 8 Lakh):**\n"
            "  - Tuition Fee: **₹1,26,087** | Development Fee: **₹18,913** | University Fees: **₹1,000** | **Total: ₹1,46,000**\n"
            "- **First-Year B.Tech (OBC / EWS / EBC Boys):**\n"
            "  - Tuition Fee: **₹63,044** | Development Fee: **₹18,913** | University Fees: **₹1,000** | **Total: ₹82,957**\n"
            "- **First-Year B.Tech (NT / SBC / TFWS & Eligible Girls):**\n"
            "  - Tuition Fee: **₹0** | Development Fee: **₹18,913** | University Fees: **₹1,000** | **Total: ₹19,913**\n"
            "- **First-Year B.Tech (SC / ST):**\n"
            "  - Tuition Fee: **₹0** | Development Fee: **₹0** | University Fees: **₹1,000** | **Total: ₹1,000**\n\n"
            "**Direct Second Year (DSY) Engineering (Open):**\n"
            "- Tuition Fee: **₹1,15,652** | Development Fee: **₹17,348** | Total: **₹1,34,000**\n\n"
            "**Other Programmes (Open Category Total):**\n"
            "- **MCA:** ₹1,43,500 | **MBA:** ₹136,500 | **M.Tech:** ₹137,000 | **BCA:** ₹51,000\n\n"
            "> *Important: Category fee concessions require valid caste/income documentation submitted via the MahaDBT portal. "
            "Please confirm payable totals with the accounts counter.*"
        )

    if any(k in q_lower for k in ["cutoff", "cut off", "cet", "jee", "percentile"]):
        return (
            "### MHT-CET & JEE Main Cutoff Guidance\n\n"
            "- **Institutional Policy:** The chatbot does not generate estimated cutoff scores because cutoffs fluctuate "
            "every academic year across CAP Round 1, Round 2, Round 3, categories (Open, OBC, SC, ST, TFWS, EWS), and home university quotas.\n"
            "- **Official Source:** Branch-wise closing percentiles and ranks are published exclusively by the "
            "**Maharashtra State CET Cell**.\n"
            "- **Recommended Process:** Consult the official State CET Cell CAP cutoff repository at [https://cetcell.mahacet.org/](https://cetcell.mahacet.org/) "
            "and RSCOE's admission notice board at [https://jspmrscoe.edu.in/](https://jspmrscoe.edu.in/).\n\n"
            "> *For current CAP vacancy lists and institute-level round schedules, contact the Admissions Cell: Dr. Amol Patil (`+91-9172390964`).*"
        )

    if any(k in q_lower for k in ["direct second year", "dsy", "dse", "diploma to degree"]):
        return (
            "### Direct Second Year (DSY / DSE) Engineering Admissions\n\n"
            "- **Eligibility:** Diploma holders and B.Sc. degree graduates as approved by Maharashtra State CET Cell / DTE regulations.\n"
            "- **2026–27 Approved Fees (Open Category):** Tuition ₹1,15,652 + Development ₹17,348 + University ₹1,000 = **₹1,34,000**.\n"
            "- **Reserved Concessions (OBC/EWS/EBC Boys):** **₹76,174** | **NT/SBC/TFWS/Girls:** **₹18,348** | **SC/ST:** **₹1,000**.\n"
            "- **Official Contacts for DSY:**\n"
            "  - **Prof. Ashish Malgave:** `+91-9921901045` / `+91-8459157893`\n"
            "  - **Dr. P. S. Patil:** `+91-9049300947`\n"
            "- **Source:** [https://www.jspmrscoe.edu.in/admissionenquiry/contact-no](https://www.jspmrscoe.edu.in/admissionenquiry/contact-no)"
        )

    if any(k in q_lower for k in ["hostel", "bus", "transport", "mess", "living"]):
        return (
            "### Campus Facilities: Hostel & Transportation Contacts\n\n"
            "Official campus facility contacts published on the college enquiry board:\n\n"
            "- **Boys & Girls Hostel Facility:**\n"
            "  - In-Charge / Rector: **Mr. Shivaji Surve**\n"
            "  - Contact: `+91-9604275533`\n"
            "  - *Note: Room allotment, mess fees, and security deposit details must be confirmed directly with the hostel office.*\n\n"
            "- **College Bus & Transportation Service:**\n"
            "  - In-Charge: **Mr. Umesh Ubale**\n"
            "  - Contacts: `+91-9763722020` / `+91-9881389182`\n"
            "  - *Covers designated routes across Pune, PCMC, and surrounding corridors.*\n\n"
            "- **Source:** [https://www.jspmrscoe.edu.in/admissionenquiry/contact-no](https://www.jspmrscoe.edu.in/admissionenquiry/contact-no)"
        )

    # Polytechnic E&TC Department & Staff
    if any(k in q_lower for k in ["e&tc", "electronic", "tyej", "madhuri patil", "amita dubey", "aparna deshpande", "bhise", "bajal", "pallavi shah"]):
        return (
            "### JSPM RSCOE (Polytechnic) — Electronics & Telecommunication Department\n\n"
            "- **Course:** Diploma in Electronics & Telecommunication Engineering\n"
            "- **Divisions:** 1 Division\n"
            "- **Class Designation:** TYEJ (Third Year E&TC)\n"
            "- **Class Strength:** **61 students**\n\n"
            "**Department Faculty & Staff Members:**\n"
            "• **Mrs. Madhuri Patil-Maske**\n"
            "• **Mrs. Amita Dubey**\n"
            "• **Mrs. Aparna Deshpande**\n"
            "• **Mrs. Pournima Bhise**\n"
            "• **Mrs. Savitha Bajal**\n"
            "• **Mrs. Pallavi Shah**\n\n"
            "**Reference Websites:**\n"
            "- Official Polytechnic Portal: [https://polytechnic.jspmrscoe.edu.in/](https://polytechnic.jspmrscoe.edu.in/)\n"
            "- Diploma Courses Directory: [https://www.jspmrscoe.edu.in/Diploma](https://www.jspmrscoe.edu.in/Diploma)\n"
            "- MSBTE Official: [https://msbte.ac.in/](https://msbte.ac.in/)\n\n"
            "> *All diploma courses are affiliated to MSBTE and approved by AICTE & DTE Maharashtra.*"
        )

    # Timetable for TYCO-A / Fifth Term
    if any(k in q_lower for k in ["timetable", "time table", "schedule", "osy", "ste", "acn", "ede", "co5k", "fifth term", "lecture"]):
        return (
            "### Official Academic Timetable — TYCO-A (Fifth Term CO5K, 2026–27)\n\n"
            "**JSPM's Rajshree Shahu College of Engineering, Polytechnic**\n"
            "*Computer Engineering Department • Term: Fifth (CO5K) • w.e.f. 10/08/2026*\n\n"
            "**Curriculum Subjects & Faculty:**\n"
            "- **OSY (Operating System):** Msr. P. V. Patil (PVP)\n"
            "- **STE (Software Engineering):** Mrs. A. R. Sonawane (ARS)\n"
            "- **ACN (Advanced Computer Network):** Mr. S. S. Ghosh (SSG)\n"
            "- **EDE (Entrepreneurship Dev. & Startups):** Ms. T. R. Patil (TRP) / Mrs. M. S. Patil (MSP)\n\n"
            "**Daily Lecture & Practical Schedule:**\n"
            "- **Monday:**\n"
            "  - 10:30–11:30: STE (ARS) [F-111] | 11:30–12:30: OSY (PVP) [F-111]\n"
            "  - *12:30–1:15: Long Break*\n"
            "  - 1:15–2:15: EDE (TRP) [F-205] | 2:15–3:15: ACN (SSG) [F-205]\n"
            "  - *3:15–3:30: Short Break*\n"
            "  - 3:30–5:30 Practicals: T1: OSY (PVP) Lab L3 | T2: STE (ARS) Lab L2 | T3: ACN (SSG) Lab L1\n\n"
            "- **Tuesday:**\n"
            "  - 10:30–11:30: STE (ARS) [F-111] | 11:30–12:30: ACN (SSG) [F-111]\n"
            "  - *12:30–1:15: Long Break*\n"
            "  - 1:15–2:15: OSY (PVP) [F-205] | 2:15–3:15: STE (ARS) [F-205]\n"
            "  - *3:15–3:30: Short Break*\n"
            "  - 3:30–5:30 Practicals: T1: ACN (SSG) Lab L1 | T2: OSY (PVP) Lab L3 | T3: STE (ARS) Lab L2\n\n"
            "- **Wednesday:**\n"
            "  - 10:30–11:30: ACN (SSG) [F-111] | 11:30–12:30: STE (ARS) [F-111]\n"
            "  - *12:30–1:15: Long Break*\n"
            "  - 1:15–3:15: OSY (PVP) [F-205]\n"
            "  - *3:15–3:30: Short Break*\n"
            "  - 3:30–5:30 Practicals / Tutorials: T1: STE (ARS) [Room 206] | T3: EDE (TRP) [Room 111]\n\n"
            "- **Thursday:**\n"
            "  - 10:30–12:30 Morning Practicals: T3: STE (ARS) Lab L3 | T2: EDE (MSP) [Room 205]\n"
            "  - *12:30–1:15: Long Break*\n"
            "  - 1:15–2:15: ACN (SSG) [F-205] | 2:15–3:15: OSY (PVP) [F-205]\n"
            "  - *3:15–3:30: Short Break*\n"
            "  - 3:30–5:30 Practicals: T1: STE (ARS) Lab L2 | T2: ACN (SSG) Lab L1 | T3: OSY (PVP) Lab L3\n\n"
            "- **Friday:**\n"
            "  - 10:30–12:30 Morning Practicals: T2: STE (ARS) [Room 212] | T1: EDE (TRP) [Room 212]\n"
            "  - *12:30–1:15: Long Break*\n"
            "  - 1:15–2:15: ACN (SSG) [F-205] | 2:15–3:15: OSY (PVP) [F-205]\n"
            "  - *3:15–3:30: Short Break*\n"
            "  - 3:30–4:30: STE (ARS) [F-111]\n\n"
            "**Laboratory Allocations:** Lab L1: Room F-05 | Lab L2: Room F-06 | Lab L3: Room F-104\n"
            "**Signatories:** Mrs. H. N. Bhandare (Timetable In-charge), Mrs. V. I. Thakare (HOD), Mrs. S. U. Puri (DAC), Dr. S. S. Gaikwad (Principal)."
        )

    # Polytechnic Computer Engineering Department & Divisions
    if any(k in q_lower for k in ["computer division", "division a", "division b", "tyco-a", "tyco-b", "tyco strength", "computer strength", "tyco"]):
        return (
            "### JSPM RSCOE (Polytechnic) — Computer Engineering Department\n\n"
            "- **Course:** Diploma in Computer Engineering\n"
            "- **Total Divisions:** **2 Divisions** (Division A and Division B)\n"
            "- **Division A (TYCO-A):** Class Strength = **61 students**\n"
            "- **Division B (TYCO-B):** Class Strength = **58 students**\n"
            "- **Total Third Year Enrollment:** **119 students**\n"
            "- **HOD (Head of Department):** Mrs. V. I. Thakare\n"
            "- **Department Academic Coordinator (DAC):** Mrs. S. U. Puri\n"
            "- **Timetable In-charge:** Mrs. H. N. Bhandare\n"
            "- **Principal:** Dr. S. S. Gaikwad\n\n"
            "**Reference Websites:**\n"
            "- Official Polytechnic Portal: [https://polytechnic.jspmrscoe.edu.in/](https://polytechnic.jspmrscoe.edu.in/)\n"
            "- Diploma Courses Directory: [https://www.jspmrscoe.edu.in/Diploma](https://www.jspmrscoe.edu.in/Diploma)\n"
            "- MSBTE Official: [https://msbte.ac.in/](https://msbte.ac.in/)"
        )

    # General Polytechnic Overview & Departments
    if any(k in q_lower for k in ["polytechnic", "diploma college", "diploma institute", "diploma department"]):
        return (
            "### JSPM's Rajarshi Shahu College of Engineering (Polytechnic)\n\n"
            "- **College Name:** JSPM's Rajarshi Shahu College of Engineering (Polytechnic)\n"
            "- **Location:** Tathawade, Pimpri-Chinchwad, Pune, Maharashtra – 411033\n"
            "- **Institute Type:** Diploma Engineering Institute (Affiliated to MSBTE, approved by AICTE & DTE Maharashtra)\n"
            "- **Principal:** Dr. S. S. Gaikwad\n\n"
            "**Offered Diploma Engineering Departments:**\n"
            "1. **Civil Engineering**\n"
            "2. **Computer Engineering** (2 Divisions: TYCO-A [61 students], TYCO-B [58 students])\n"
            "3. **Electronics & Telecommunication Engineering (E&TC)** (1 Division: TYEJ [61 students])\n"
            "4. **Mechanical Engineering**\n\n"
            "**Official Reference Portals:**\n"
            "- Official Polytechnic Website: [https://polytechnic.jspmrscoe.edu.in/](https://polytechnic.jspmrscoe.edu.in/)\n"
            "- Official Diploma Courses Directory: [https://www.jspmrscoe.edu.in/Diploma](https://www.jspmrscoe.edu.in/Diploma)\n"
            "- MSBTE Official Website: [https://msbte.ac.in/](https://msbte.ac.in/)"
        )

    # General evidence extraction
    primary_chunk = chunks[0]
    sec_title = primary_chunk["section_title"]
    sec_text = primary_chunk["text"]
    src = primary_chunk["source_url"]

    # Format into concise, structured response
    clean_lines = [l.strip() for l in sec_text.splitlines() if l.strip() and not l.strip().startswith("#")]
    body = "\n".join(clean_lines[:25])

    return (
        f"### {sec_title}\n\n"
        f"{body}\n\n"
        f"**Official Source:** [{src}]({src})\n\n"
        f"> *Information retrieved from verified institutional records. Rules and notices are subject to periodic revision.*"
    )

async def generate_ai_answer(query: str, retrieval: Dict[str, Any], institute: str) -> str:
    """Optional AI answer generation when AI_PROVIDER is configured."""
    if settings.AI_PROVIDER == "none" or not settings.AI_API_KEY:
        return generate_retrieval_only_answer(query, retrieval, institute)

    evidence_passages = "\n\n---\n\n".join([
        f"Source: {c['section_title']} ({c['source_url']})\n{c['text']}"
        for c in retrieval["chunks"]
    ])

    system_prompt = (
        "You are the official JSPM Tathawade Campus Chatbot assistant. "
        "Your duty is to provide strictly accurate, professional, and well-structured answers "
        "derived exclusively from the supplied retrieved college documents. "
        "Guidelines:\n"
        "1. Never fabricate or extrapolate facts, fees, cutoffs, or phone numbers.\n"
        "2. If retrieved evidence is insufficient, clearly state that the official information is unverified and provide the relevant office contact.\n"
        "3. Highlight discrepancies or warnings prominently.\n"
        "4. Format answers using markdown headings, bullet points, and concise language.\n"
        "5. Include citations to the official source URLs provided."
    )

    user_prompt = (
        f"User Query: {query}\n"
        f"Selected Institute Filter: {institute}\n\n"
        f"Retrieved Official Evidence:\n{evidence_passages}\n\n"
        f"Please provide an authoritative and helpful answer according to the system instructions."
    )

    try:
        if settings.AI_PROVIDER in ("openai", "custom"):
            headers = {
                "Authorization": f"Bearer {settings.AI_API_KEY}",
                "Content-Type": "application/json"
            }
            payload = {
                "model": settings.AI_MODEL,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                "temperature": 0.2
            }
            async with httpx.AsyncClient(timeout=20.0) as client:
                res = await client.post(f"{settings.AI_BASE_URL}/chat/completions", json=payload, headers=headers)
                if res.status_code == 200:
                    data = res.json()
                    return data["choices"][0]["message"]["content"]
                else:
                    return generate_retrieval_only_answer(query, retrieval, institute)
        elif settings.AI_PROVIDER == "gemini":
            # Gemini REST API
            gemini_url = f"https://generativelanguage.googleapis.com/v1beta/models/{settings.AI_MODEL}:generateContent?key={settings.AI_API_KEY}"
            payload = {
                "contents": [
                    {"parts": [{"text": f"{system_prompt}\n\n{user_prompt}"}]}
                ],
                "generationConfig": {"temperature": 0.2}
            }
            async with httpx.AsyncClient(timeout=20.0) as client:
                res = await client.post(gemini_url, json=payload)
                if res.status_code == 200:
                    data = res.json()
                    return data["candidates"][0]["content"]["parts"][0]["text"]
                else:
                    return generate_retrieval_only_answer(query, retrieval, institute)
    except Exception:
        # Graceful fallback to deterministic retrieval-only mode
        return generate_retrieval_only_answer(query, retrieval, institute)

    return generate_retrieval_only_answer(query, retrieval, institute)
