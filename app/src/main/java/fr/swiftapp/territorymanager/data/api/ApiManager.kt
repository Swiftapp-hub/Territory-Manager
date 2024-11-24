package fr.swiftapp.territorymanager.data.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import fr.swiftapp.territorymanager.settings.getApiUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.suspendCoroutine

class ApiManager(private val context: Context) {
    private val requestQueue = Volley.newRequestQueue(context)

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private suspend fun getLastUpdate(apiUrl: String): String = suspendCoroutine { cont ->
        val req = StringRequest(
            Request.Method.GET,
            apiUrl.replace("territory-manager", "territory-manager/last-update"),
            { res ->
                cont.resumeWith(Result.success(res))
            },
            {
                cont.resumeWith(Result.failure(it))
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
                val json = getJson(apiUrl)
                val lastUpdate = getLastUpdate(apiUrl)

                Log.d("MY", "$json \n $lastUpdate")
                finished(false)
            } catch (e: Exception) {
                Log.e("MY", e.toString())
                finished(true)
            }
        }
    }
}