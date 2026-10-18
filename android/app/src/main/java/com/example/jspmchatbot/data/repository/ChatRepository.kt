package com.example.jspmchatbot.data.repository

import android.content.Context
import com.example.jspmchatbot.data.model.*
import com.example.jspmchatbot.data.remote.ApiClient
import org.json.JSONArray
import org.json.JSONObject

class ChatRepository(private val context: Context) {

    // Default to PC Wi-Fi IP and port 8088 for phone connectivity
    var currentBaseUrl: String = "http://192.168.1.4:8088"

    suspend fun checkBackendHealth(): Boolean {
        return ApiClient.checkHealth(currentBaseUrl)
    }

    suspend fun getQuickQuestions(institute: String): List<QuickQuestion> {
        val remote = ApiClient.fetchQuickQuestions(currentBaseUrl, institute)
        if (remote.isNotEmpty()) {
            return remote
        }
        // Fallback from bundled asset
        return loadOfflineQuickQuestions(institute)
    }

    suspend fun sendMessage(query: String, institute: String): ChatMessage {
        return try {
            ApiClient.sendChat(currentBaseUrl, query, institute)
        } catch (e: Exception) {
            // Offline fallback
            generateOfflineResponse(query, institute)
        }
    }

    private fun loadOfflineQuickQuestions(institute: String): List<QuickQuestion> {
        val json = ApiClient.loadOfflineBundle(context) ?: return emptyList()
        val array = json.optJSONArray("quick_questions") ?: return emptyList()
        val list = mutableListOf<QuickQuestion>()
        for (i in 0 until array.length()) {
            val q = array.getJSONObject(i)
            val inst = q.optString("institute", "ALL")
            if (institute == "ALL" || inst == "ALL" || inst == institute) {
                list.add(
                    QuickQuestion(
                        id = q.optString("id", "q$i"),
                        category = q.optString("category", "General"),
                        question = q.optString("question", ""),
                        institute = inst
                    )
                )
            }
        }
        return list
    }

    private fun generateOfflineResponse(query: String, institute: String): ChatMessage {
        val qLower = query.lower()
        val json = ApiClient.loadOfflineBundle(context)

        val offlineWarning = WarningModel(
            title = "Offline Fallback Mode",
            message = "Backend server is currently offline or unreachable at $currentBaseUrl. Answering from pre-bundled verified campus knowledge records.",
            severity = "info"
        )
        // Timetable Break Timing (from reference image)
        if (qLower.contains("break") || qLower.contains("timing of long break") || qLower.contains("lunch")) {
            return ChatMessage(
                text = "### Timetable Break Schedule ⏰\n\n" +
                        "- **Long Break:** **12:30 PM to 1:15 PM** (applicable to both TYCO-A and TYCO-B).\n" +
                        "- **Short Break:** **3:15 PM to 3:30 PM**.\n\n" +
                        "> *Verified from Fifth Term (C05K) 2026–27 Academic Schedule.*",
                role = MessageRole.ASSISTANT,
                isOffline = true
            )
        }

        // Division & Class Strength (from reference image)
        if (qLower.contains("students are there") || qLower.contains("students in tyco") || qLower.contains("strength")) {
            return ChatMessage(
                text = "### Class Strength (Computer Engineering)\n\n" +
                        "- **TYCO-A (3rd Year):** **61 students** (Class Teacher: Mrs. A.R. Sonawane)\n" +
                        "- **TYCO-B (3rd Year):** **58 students**\n" +
                        "- **Classroom:** F-111 & F-205",
                role = MessageRole.ASSISTANT,
                isOffline = true
            )
        }

        // Fees
        if (qLower.contains("fee") || qLower.contains("tuition") || qLower.contains("cost")) {
            val fees = parseOfflineFees(json)
            val text = "### Approved 2026–27 Fee Schedule (Offline Cache)\n\n" +
                    "- **First Year B.Tech (Open):** Tuition ₹1,26,087 | Dev Fee ₹18,913 | Total: **₹1,46,000**\n" +
                    "- **OBC / EWS / EBC (Boys):** Tuition ₹63,044 | Dev Fee ₹18,913 | Total: **₹82,957**\n" +
                    "- **NT / SBC / TFWS & Girls:** Tuition ₹0 | Dev Fee ₹18,913 | Total: **₹19,913**\n" +
                    "- **SC / ST:** Tuition ₹0 | Dev Fee ₹0 | University ₹1,000 | Total: **₹1,000**\n\n" +
                    "> Verified from official RSCOE fee notice image (20-July-2026)."

            return ChatMessage(
                text = text,
                role = MessageRole.ASSISTANT,
                citations = listOf(
                    CitationModel(
                        title = "RSCOE Approved Fee Schedule 2026-27",
                        url = "https://jspmrscoe.edu.in/20-July-2026/fee-structure-2026-27.jpeg",
                        institute = "RSCOE"
                    )
                ),
                warnings = listOf(offlineWarning),
                feeBreakdown = fees,
                isOffline = true
            )
        }

        // Placements / Highest package
        if (qLower.contains("placement") || qLower.contains("package") || qLower.contains("salary") || qLower.contains("highest")) {
            val stats = parseOfflinePlacements(json)
            val text = "### RSCOE Placement & Package Records (Offline Cache)\n\n" +
                    "- **2025–26:** Highest recorded: **₹33.8 LPA** (550+ companies, 800+ placed, 1,100+ offers)\n" +
                    "- **2024–25:** Highest package: **₹30 LPA** (780+ placed)\n" +
                    "- **2023–24:** Year-wise table lists **\"1 Cr. LPA\"**\n" +
                    "- **Summary text:** Lists packages up to **₹42 LPA (Amazon)**\n\n" +
                    "**Major Recruiters:** TCS, Capgemini, Cognizant, Infosys, Nutanix, KPIT, Bosch, LTIMindtree."

            val conflictWarning = WarningModel(
                title = "Institutional Data Discrepancy (Highest Package)",
                message = "The year-wise placement table reports 2025-26 as ₹33.8 LPA and 2023-24 as '1 Cr. LPA', while the summary paragraph highlights ₹42 LPA (Amazon). Confirm audited figures directly with SPIR Cell.",
                severity = "conflict",
                sources = listOf("https://jspmrscoe.edu.in/placements/placementprocessstatics")
            )

            return ChatMessage(
                text = text,
                role = MessageRole.ASSISTANT,
                citations = listOf(
                    CitationModel(
                        title = "RSCOE SPIR Placement Statistics",
                        url = "https://jspmrscoe.edu.in/placements/placementprocessstatics",
                        institute = "RSCOE"
                    )
                ),
                warnings = listOf(offlineWarning, conflictWarning),
                placementStats = stats,
                isOffline = true
            )
        }

        // Contacts / Hostel / Bus
        if (qLower.contains("contact") || qLower.contains("phone") || qLower.contains("hostel") || qLower.contains("bus") || qLower.contains("hod")) {
            val contacts = parseOfflineContacts(json)
            val text = "### Key Institutional & Campus Contacts (Offline Cache)\n\n" +
                    "- **Principal (RSCOE):** Dr. Avinash Badadhe (`+91-9850557177`)\n" +
                    "- **First Year Admissions:** Dr. Amol Patil (`+91-9172390964`)\n" +
                    "- **Direct Second Year (DSY):** Prof. Ashish Malgave (`+91-9921901045`)\n" +
                    "- **Hostel In-Charge:** Mr. Shivaji Surve (`+91-9604275533`)\n" +
                    "- **Bus Transport In-Charge:** Mr. Umesh Ubale (`+91-9763722020`)"

            return ChatMessage(
                text = text,
                role = MessageRole.ASSISTANT,
                citations = listOf(
                    CitationModel(
                        title = "RSCOE Official Admission Enquiry Contacts",
                        url = "https://www.jspmrscoe.edu.in/admissionenquiry/contact-no",
                        institute = "RSCOE"
                    )
                ),
                warnings = listOf(offlineWarning),
                contactCards = contacts,
                isOffline = true
            )
        }

        // Polytechnic E&TC Department & Staff
        if (qLower.contains("e&tc") || qLower.contains("electronic") || qLower.contains("tyej") || qLower.contains("madhuri") || qLower.contains("amita") || qLower.contains("aparna") || qLower.contains("bhise") || qLower.contains("bajal") || qLower.contains("pallavi")) {
            val text = "### JSPM RSCOE (Polytechnic) — Electronics & Telecommunication (Offline Cache)\n\n" +
                    "- **Course:** Diploma in Electronics & Telecommunication Engineering\n" +
                    "- **Divisions:** 1 Division (TYEJ)\n" +
                    "- **Class Strength:** **61 students**\n\n" +
                    "**Staff & Faculty Members:**\n" +
                    "• Mrs. Madhuri Patil-Maske\n" +
                    "• Mrs. Amita Dubey\n" +
                    "• Mrs. Aparna Deshpande\n" +
                    "• Mrs. Pournima Bhise\n" +
                    "• Mrs. Savitha Bajal\n" +
                    "• Mrs. Pallavi Shah\n\n" +
                    "**Portals:** [https://polytechnic.jspmrscoe.edu.in/](https://polytechnic.jspmrscoe.edu.in/) • [https://msbte.ac.in/](https://msbte.ac.in/)"

            return ChatMessage(
                text = text,
                role = MessageRole.ASSISTANT,
                citations = listOf(
                    CitationModel(
                        title = "Polytechnic E&TC Department",
                        url = "https://polytechnic.jspmrscoe.edu.in/",
                        institute = "POLYTECHNIC"
                    )
                ),
                warnings = listOf(offlineWarning),
                isOffline = true
            )
        }

        // Polytechnic Timetable (TYCO-A)
        if (qLower.contains("timetable") || qLower.contains("schedule") || qLower.contains("osy") || qLower.contains("ste") || qLower.contains("acn") || qLower.contains("ede") || qLower.contains("co5k")) {
            val text = "### Official Academic Timetable — TYCO-A (Offline Cache)\n\n" +
                    "**JSPM's Rajshree Shahu College of Engineering, Polytechnic**\n" +
                    "*Computer Engineering Department • Term: Fifth (CO5K) • w.e.f. 10/08/2026*\n\n" +
                    "**Curriculum Subjects & Faculty:**\n" +
                    "- **OSY (Operating System):** Msr. P. V. Patil (PVP)\n" +
                    "- **STE (Software Engineering):** Mrs. A. R. Sonawane (ARS)\n" +
                    "- **ACN (Advanced Computer Network):** Mr. S. S. Ghosh (SSG)\n" +
                    "- **EDE (Entrepreneurship Dev. & Startups):** Ms. T. R. Patil (TRP) / Mrs. M. S. Patil (MSP)\n\n" +
                    "**Daily Schedule Overview:**\n" +
                    "- Mon/Tue 10:30–11:30: STE (ARS) [F-111] | Mon 11:30–12:30: OSY (PVP) [F-111]\n" +
                    "- Tue 11:30–12:30: ACN (SSG) [F-111] | Wed 10:30–11:30: ACN (SSG) [F-111]\n" +
                    "- Breaks: Long Break (12:30–1:15 PM) | Short Break (3:15–3:30 PM)\n" +
                    "- Practicals: Batch T1 (L3/L1/L2), Batch T2 (L2/L3/L1), Batch T3 (L1/L2/L3)\n\n" +
                    "**Signatories:** Mrs. H. N. Bhandare (Timetable) • Mrs. V. I. Thakare (HOD) • Mrs. S. U. Puri (DAC) • Dr. S. S. Gaikwad (Principal)"

            return ChatMessage(
                text = text,
                role = MessageRole.ASSISTANT,
                citations = listOf(
                    CitationModel(
                        title = "Polytechnic Computer Engineering Timetable",
                        url = "https://polytechnic.jspmrscoe.edu.in/",
                        institute = "POLYTECHNIC"
                    )
                ),
                warnings = listOf(offlineWarning),
                isOffline = true
            )
        }

        // Polytechnic Computer Engineering Divisions & Strengths
        if (qLower.contains("computer division") || qLower.contains("tyco") || qLower.contains("division a") || qLower.contains("division b")) {
            val text = "### Computer Engineering Department (Polytechnic) (Offline Cache)\n\n" +
                    "- **Course:** Diploma in Computer Engineering\n" +
                    "- **Total Divisions:** **2 Divisions**\n" +
                    "- **Division A (TYCO-A):** **61 students**\n" +
                    "- **Division B (TYCO-B):** **58 students**\n" +
                    "- **Total Third Year Enrollment:** **119 students**\n" +
                    "- **HOD:** Mrs. V. I. Thakare | **Principal:** Dr. S. S. Gaikwad\n\n" +
                    "**Portals:** [https://polytechnic.jspmrscoe.edu.in/](https://polytechnic.jspmrscoe.edu.in/) • [https://msbte.ac.in/](https://msbte.ac.in/)"

            return ChatMessage(
                text = text,
                role = MessageRole.ASSISTANT,
                citations = listOf(
                    CitationModel(
                        title = "Polytechnic Computer Engineering Department",
                        url = "https://polytechnic.jspmrscoe.edu.in/",
                        institute = "POLYTECHNIC"
                    )
                ),
                warnings = listOf(offlineWarning),
                isOffline = true
            )
        }

        // General Polytechnic Overview
        if (qLower.contains("polytechnic") || qLower.contains("diploma")) {
            val text = "### JSPM's Rajarshi Shahu College of Engineering (Polytechnic) (Offline Cache)\n\n" +
                    "- **Location:** Tathawade, Pimpri-Chinchwad, Pune, Maharashtra – 411033\n" +
                    "- **Institute Type:** Diploma Engineering Institute (Affiliated to MSBTE, approved by AICTE & DTE)\n" +
                    "- **Principal:** Dr. S. S. Gaikwad\n\n" +
                    "**Departments:**\n" +
                    "1. Civil Engineering\n" +
                    "2. Computer Engineering (TYCO-A: 61, TYCO-B: 58)\n" +
                    "3. Electronics & Telecommunication Engineering (TYEJ: 61)\n" +
                    "4. Mechanical Engineering\n\n" +
                    "**Official Reference Portals:**\n" +
                    "- [https://polytechnic.jspmrscoe.edu.in/](https://polytechnic.jspmrscoe.edu.in/)\n" +
                    "- [https://www.jspmrscoe.edu.in/Diploma](https://www.jspmrscoe.edu.in/Diploma)\n" +
                    "- [https://msbte.ac.in/](https://msbte.ac.in/)"

            return ChatMessage(
                text = text,
                role = MessageRole.ASSISTANT,
                citations = listOf(
                    CitationModel(
                        title = "Polytechnic Official Portal",
                        url = "https://polytechnic.jspmrscoe.edu.in/",
                        institute = "POLYTECHNIC"
                    )
                ),
                warnings = listOf(offlineWarning),
                isOffline = true
            )
        }

        // Default offline response
        return ChatMessage(
            text = "### Offline Guidance Notice\n\n" +
                    "I am currently operating in offline mode using the bundled knowledge base.\n\n" +
                    "For inquiries regarding \"$query\", please connect to the backend server or visit the official portal: [https://jspmrscoe.edu.in/](https://jspmrscoe.edu.in/).",
            role = MessageRole.ASSISTANT,
            warnings = listOf(offlineWarning),
            isOffline = true
        )
    }

    private fun parseOfflineFees(json: JSONObject?): List<FeeItemModel> {
        val list = mutableListOf<FeeItemModel>()
        val arr = json?.optJSONArray("fee_schedule") ?: return list
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                FeeItemModel(
                    programme = o.optString("programme", ""),
                    category = o.optString("category", ""),
                    tuitionFee = o.optString("tuition_fee", ""),
                    developmentFee = o.optString("development_fee", ""),
                    universityFee = o.optString("university_fee", ""),
                    totalFee = o.optString("total_fee", "")
                )
            )
        }
        return list
    }

    private fun parseOfflinePlacements(json: JSONObject?): List<PlacementStatModel> {
        val list = mutableListOf<PlacementStatModel>()
        val arr = json?.optJSONArray("placement_statistics") ?: return list
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                PlacementStatModel(
                    academicYear = o.optString("academic_year", ""),
                    companiesVisited = o.optString("companies_visited", ""),
                    studentsPlaced = o.optString("students_placed", ""),
                    offers = o.optString("offers", ""),
                    maxSalary = o.optString("max_salary", "")
                )
            )
        }
        return list
    }

    private fun parseOfflineContacts(json: JSONObject?): List<ContactCardModel> {
        val list = mutableListOf<ContactCardModel>()
        val arr = json?.optJSONArray("official_contacts") ?: return list
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                ContactCardModel(
                    name = o.optString("name", ""),
                    role = o.optString("role", ""),
                    institute = o.optString("institute", ""),
                    phone = if (o.has("phone") && !o.isNull("phone")) o.getString("phone") else null,
                    email = if (o.has("email") && !o.isNull("email")) o.getString("email") else null,
                    department = if (o.has("department") && !o.isNull("department")) o.getString("department") else null,
                    sourceUrl = if (o.has("source_url") && !o.isNull("source_url")) o.getString("source_url") else null
                )
            )
        }
        return list
    }

    private fun String.lower(): String = this.lowercase(java.util.Locale.ROOT)
}
