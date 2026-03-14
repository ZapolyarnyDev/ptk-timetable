package io.github.zapolyarnydev.ptktimetable.data.remote.service

interface PortalService {
    suspend fun fetchPortalHtml(): String
    suspend fun downloadXls(url: String): ByteArray
}
