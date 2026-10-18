package com.example.jspmchatbot.data.model

enum class MessageRole {
    USER,
    ASSISTANT
}

data class PlacementStatModel(
    val academicYear: String,
    val companiesVisited: String,
    val studentsPlaced: String,
    val offers: String,
    val maxSalary: String
)

data class FeeItemModel(
    val programme: String,
    val category: String,
    val tuitionFee: String,
    val developmentFee: String,
    val universityFee: String,
    val totalFee: String
)

data class ContactCardModel(
    val name: String,
    val role: String,
    val institute: String,
    val phone: String? = null,
    val email: String? = null,
    val department: String? = null,
    val sourceUrl: String? = null
)

data class CitationModel(
    val title: String,
    val url: String,
    val institute: String,
    val status: String = "VERIFIED"
)

data class WarningModel(
    val title: String,
    val message: String,
    val severity: String = "warning",
    val sources: List<String> = emptyList()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val citations: List<CitationModel> = emptyList(),
    val warnings: List<WarningModel> = emptyList(),
    val placementStats: List<PlacementStatModel>? = null,
    val feeBreakdown: List<FeeItemModel>? = null,
    val contactCards: List<ContactCardModel>? = null,
    val isOffline: Boolean = false,
    val isError: Boolean = false
)

data class QuickQuestion(
    val id: String,
    val category: String,
    val question: String,
    val institute: String
)

data class InstituteOption(
    val code: String,
    val displayName: String,
    val shortName: String
)

val CAMPUS_INSTITUTES = listOf(
    InstituteOption("ALL", "All Tathawade Campus", "All"),
    InstituteOption("RSCOE", "Rajarshi Shahu College of Engineering", "RSCOE Engg"),
    InstituteOption("RSCOPR", "Rajarshi Shahu College of Pharmacy", "Pharmacy"),
    InstituteOption("JIMS", "Jayawant Institute of Management Studies", "JIMS (MBA/MCA)"),
    InstituteOption("RSCOED", "Rajarshi Shahu College of Education", "B.Ed Education"),
    InstituteOption("POLYTECHNIC", "JSPM Polytechnic Diploma", "Polytechnic")
)
