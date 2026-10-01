package com.carscan.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WikiInfo(val title: String, val extract: String, val thumb: String?, val page: String?)

object Wiki {
    suspend fun summary(query: String): WikiInfo? = withContext(Dispatchers.IO) {
        val t = URLEncoder.encode(query.trim().replace(' ', '_'), "UTF-8")
        for (lang in listOf("ru", "en")) {
            try {
                val c = URL("https://$lang.wikipedia.org/api/rest_v1/page/summary/$t").openConnection() as HttpURLConnection
                c.setRequestProperty("User-Agent", "CarScan/0.1 (personal project)")
                c.connectTimeout = 8000
                c.readTimeout = 8000
                if (c.responseCode == 200) {
                    val j = JSONObject(c.inputStream.bufferedReader().use { it.readText() })
                    return@withContext WikiInfo(
                        j.optString("title"),
                        j.optString("extract"),
                        j.optJSONObject("thumbnail")?.optString("source"),
                        j.optJSONObject("content_urls")?.optJSONObject("mobile")?.optString("page")
                    )
                }
            } catch (e: Exception) {
            }
        }
        null
    }
}
