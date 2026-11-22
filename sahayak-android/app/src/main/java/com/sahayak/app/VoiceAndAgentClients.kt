package com.sahayak.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object VoiceAndAgentClients {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val PREFS = "sahayak_settings"

    fun saveCredentials(context: Context, sarvam: String, divyam: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("sarvam_cred", sarvam.trim())
            .putString("divyam_cred", divyam.trim())
            .apply()
    }

    fun getSarvamCredential(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("sarvam_cred", "") ?: ""

    fun getDivyamCredential(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("divyam_cred", "") ?: ""

    suspend fun postJson(
        endpointUrl: String,
        headerName: String,
        headerValue: String,
        payload: JSONObject
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        if (headerValue.isBlank()) return@withContext Result.failure(IllegalStateException("Missing credential"))
        val req = Request.Builder()
            .url(endpointUrl)
            .addHeader(headerName, headerValue)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) Result.failure(Exception("HTTP ${resp.code}"))
            else Result.success(JSONObject(body))
        }
    }
}
