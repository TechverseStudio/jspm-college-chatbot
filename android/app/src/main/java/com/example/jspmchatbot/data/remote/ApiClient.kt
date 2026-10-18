package com.example.jspmchatbot.data.remote

import android.content.Context
import com.example.jspmchatbot.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    suspend fun checkHealth(baseUrl: String): Boolean = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val cleanUrl = baseUrl.trimEnd('/') + "/api/health"
            val url = URL(cleanUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            conn.responseCode == 200
        } catch (e: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun sendChat(baseUrl: String, query: String, institute: String): ChatMessage = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trimEnd('/') + "/api/chat"
        val url = URL(cleanUrl)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 12000
            conn.readTimeout = 15000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true

            val reqObj = JSONObject().apply {
                put("message", query)
                put("institute", institute)
                put("session_id", "android-session-1")
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(reqObj.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val respText = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }

            if (responseCode !in 200..299) {
                throw Exception("Server returned HTTP $responseCode: $respText")
            }

            val json = JSONObject(respText)
            val answer = json.optString("answer", "No answer text returned from server.")

            val citations = mutableListOf<CitationModel>()
            val citArray = json.optJSONArray("citations") ?: JSONArray()
            for (i in 0 until citArray.length()) {
                val c = citArray.getJSONObject(i)
                citations.add(
                    CitationModel(
                        title = c.optString("title", "Official Source"),
                        url = c.optString("url", ""),
                        institute = c.optString("institute", institute),
                        status = c.optString("status", "VERIFIED")
                    )
                )
            }

            val warnings = mutableListOf<WarningModel>()
            val warnArray = json.optJSONArray("warnings") ?: JSONArray()
            for (i in 0 until warnArray.length()) {
                val w = warnArray.getJSONObject(i)
                val srcList = mutableListOf<String>()
                val sArr = w.optJSONArray("sources") ?: JSONArray()
                for (j in 0 until sArr.length()) {
                    srcList.add(sArr.getString(j))
                }
                warnings.add(
                    WarningModel(
                        title = w.optString("title", "Institutional Notice"),
                        message = w.optString("message", ""),
                        severity = w.optString("severity", "warning"),
                        sources = srcList
                    )
                )
            }

            var placementStats: MutableList<PlacementStatModel>? = null
            val psArray = json.optJSONArray("placement_stats")
            if (psArray != null && psArray.length() > 0) {
                placementStats = mutableListOf()
                for (i in 0 until psArray.length()) {
                    val p = psArray.getJSONObject(i)
                    placementStats.add(
                        PlacementStatModel(
                            academicYear = p.optString("academic_year", ""),
                            companiesVisited = p.optString("companies_visited", ""),
                            studentsPlaced = p.optString("students_placed", ""),
                            offers = p.optString("offers", ""),
                            maxSalary = p.optString("max_salary", "")
                        )
                    )
                }
            }

            var feeBreakdown: MutableList<FeeItemModel>? = null
            val feeArray = json.optJSONArray("fee_breakdown")
            if (feeArray != null && feeArray.length() > 0) {
                feeBreakdown = mutableListOf()
                for (i in 0 until feeArray.length()) {
                    val f = feeArray.getJSONObject(i)
                    feeBreakdown.add(
                        FeeItemModel(
                            programme = f.optString("programme", ""),
                            category = f.optString("category", ""),
                            tuitionFee = f.optString("tuition_fee", ""),
                            developmentFee = f.optString("development_fee", ""),
                            universityFee = f.optString("university_fee", ""),
                            totalFee = f.optString("total_fee", "")
                        )
                    )
                }
            }

            var contactCards: MutableList<ContactCardModel>? = null
            val ccArray = json.optJSONArray("contact_cards")
            if (ccArray != null && ccArray.length() > 0) {
                contactCards = mutableListOf()
                for (i in 0 until ccArray.length()) {
                    val c = ccArray.getJSONObject(i)
                    contactCards.add(
                        ContactCardModel(
                            name = c.optString("name", ""),
                            role = c.optString("role", ""),
                            institute = c.optString("institute", ""),
                            phone = if (c.has("phone") && !c.isNull("phone")) c.getString("phone") else null,
                            email = if (c.has("email") && !c.isNull("email")) c.getString("email") else null,
                            department = if (c.has("department") && !c.isNull("department")) c.getString("department") else null,
                            sourceUrl = if (c.has("source_url") && !c.isNull("source_url")) c.getString("source_url") else null
                        )
                    )
                }
            }

            ChatMessage(
                text = answer,
                role = MessageRole.ASSISTANT,
                citations = citations,
                warnings = warnings,
                placementStats = placementStats,
                feeBreakdown = feeBreakdown,
                contactCards = contactCards,
                isOffline = false
            )
        } finally {
            conn.disconnect()
        }
    }

    suspend fun fetchQuickQuestions(baseUrl: String, institute: String): List<QuickQuestion> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val cleanUrl = baseUrl.trimEnd('/') + "/api/quick-questions?institute=" + institute
            val url = URL(cleanUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val respText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                val array = JSONArray(respText)
                val list = mutableListOf<QuickQuestion>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        QuickQuestion(
                            id = item.optString("id", "q$i"),
                            category = item.optString("category", "General"),
                            question = item.optString("question", ""),
                            institute = item.optString("institute", "ALL")
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) {
            // fallback handled by repository
        } finally {
            conn?.disconnect()
        }
        emptyList()
    }

    fun loadOfflineBundle(context: Context): JSONObject? {
        return try {
            context.assets.open("offline_knowledge.json").use { stream ->
                val text = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
                JSONObject(text)
            }
        } catch (e: Exception) {
            null
        }
    }
}
