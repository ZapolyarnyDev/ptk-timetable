package io.github.zapolyarnydev.ptktimetable.data.remote.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import java.net.URI

class PortalServiceImpl(
    private val client: HttpClient = HttpClient(OkHttp),
    private val baseUrl: String = PORTAL_URL
) : PortalService {

    override suspend fun fetchPortalHtml(): String {
        val response = client.get(baseUrl)
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Portal HTML request failed: ${response.status}")
        }
        return response.body()
    }

    override suspend fun downloadXls(url: String): ByteArray {
        val absoluteUrl = resolveUrl(url)
        val response = client.get(absoluteUrl)
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("XLS request failed: ${response.status}; url=$absoluteUrl")
        }
        return response.body()
    }

    private fun resolveUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        return URI(baseUrl).resolve(trimmed).toString()
    }

    companion object {
        const val PORTAL_URL = "https://portal.novsu.ru/univer/timetable/spo/"
    }
}
