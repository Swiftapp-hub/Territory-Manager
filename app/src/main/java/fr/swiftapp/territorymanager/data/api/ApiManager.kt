package fr.swiftapp.territorymanager.data.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.swiftapp.territorymanager.data.Territory
import fr.swiftapp.territorymanager.data.TerritoryDatabase
import fr.swiftapp.territorymanager.settings.getApiUrl
import fr.swiftapp.territorymanager.settings.getLastLocalUpdate
import fr.swiftapp.territorymanager.settings.getNameList
import fr.swiftapp.territorymanager.settings.updateLastLocalUpdate
import fr.swiftapp.territorymanager.settings.updateNamesList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.suspendCoroutine

class ApiManager(
    private val context: Context,
    private val db: TerritoryDatabase
) {
    private val requestQueue = Volley.newRequestQueue(context)

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private suspend fun getLastUpdate(apiUrl: String): String? = suspendCoroutine { cont ->
        val req = StringRequest(
            Request.Method.GET,
            apiUrl.replace("territory-manager", "territory-manager/last-update"),
            { res ->
                cont.resumeWith(Result.success(res))
            },
            {
                cont.resumeWith(Result.success(null))
            }
        )
        requestQueue.add(req)
    }

    private suspend fun getJson(apiUrl: String): JSONObject =
        suspendCoroutine { cont ->
            val req = JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                { res ->
                    cont.resumeWith(Result.success(res))
                },
                { error ->
                    cont.resumeWith(Result.failure(error))
                }
            )
            requestQueue.add(req)
        }

    private suspend fun uploadJson(apiUrl: String, json: JSONObject): JSONObject =
        suspendCoroutine { cont ->
            val req = JsonObjectRequest(
                Request.Method.PATCH,
                apiUrl,
                json,
                { res ->
                    cont.resumeWith(Result.success(res))
                },
                { error ->
                    cont.resumeWith(Result.failure(error))
                }
            )
            requestQueue.add(req)
        }

    fun updateLocal(finished: (error: Boolean) -> Unit) {
        scope.launch {
            try {
                val apiUrl = getApiUrl(context)
                if (apiUrl.isBlank()) {
                    Log.d("MY", "No api url")
                    finished(false)
                    return@launch
                }

                val lastUpdate = getLastUpdate(apiUrl)
                val lastLocalUpdate = getLastLocalUpdate(context)

                if (lastUpdate != lastLocalUpdate) {
                    val json = getJson(apiUrl)

                    updateNamesList(context, json.getString("names"))

                    db.territoryDao().deleteAll()
                    val gson = GsonBuilder().serializeNulls().disableHtmlEscaping().create()
                    val territories = json.getJSONArray("territories")
                    for (index in 0 until territories.length()) {
                        val text = territories.get(index).toString()
                        db.territoryDao().insert(gson.fromJson(text, Territory::class.java))
                    }

                    updateLastLocalUpdate(context, lastUpdate ?: "")
                }

                finished(false)
            } catch (e: Exception) {
                Log.e("MY", e.toString())
                finished(true)
            }
        }
    }

    fun uploadChanges(finished: (error: Boolean) -> Unit) {
        scope.launch {
            try {
                val apiUrl = getApiUrl(context)
                if (apiUrl.isBlank()) {
                    Log.d("MY", "No api url")
                    finished(false)
                    return@launch
                }

                val lastUpdate = getLastUpdate(apiUrl) ?: ""
                val lastLocalUpdate = getLastLocalUpdate(context)

                if (lastUpdate != lastLocalUpdate) {
                    Log.d("MY", "Download remote changes")
                    updateLocal {
                        finished(true)
                    }
                } else {
                    db.territoryDao().exportAll().collect { t ->
                        db.territoryDao().exportAllChanges().collect { tc ->
                            val names = getNameList(context)
                            val gson =
                                GsonBuilder().serializeNulls().disableHtmlEscaping()
                                    .create()

                            val json = JsonObject()
                            json.addProperty("names", names)
                            json.add("territories", gson.toJsonTree(t))
                            json.add("territories_changes", gson.toJsonTree(tc))

                            val date = uploadJson(apiUrl, JSONObject(json.toString()))
                            updateLastLocalUpdate(context, date.getString("date"))
                            Log.d("MY", "date: $date")
                            finished(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MY", e.toString())
                finished(true)
            }
        }
    }
}
