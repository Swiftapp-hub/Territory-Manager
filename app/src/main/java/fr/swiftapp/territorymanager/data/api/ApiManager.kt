package fr.swiftapp.territorymanager.data.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import fr.swiftapp.territorymanager.data.Territory
import fr.swiftapp.territorymanager.data.TerritoryDatabase
import fr.swiftapp.territorymanager.settings.getApiUrl
import fr.swiftapp.territorymanager.settings.getLastLocalUpdate
import fr.swiftapp.territorymanager.settings.updateLastLocalUpdate
import fr.swiftapp.territorymanager.settings.updateNamesList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.suspendCoroutine

class ApiManager(private val context: Context, private val db: TerritoryDatabase) {
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

    private suspend fun getJson(apiUrl: String): JSONObject = suspendCoroutine { cont ->
        val req = JsonObjectRequest(Request.Method.GET, apiUrl, null,
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
}
