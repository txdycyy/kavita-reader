package com.kavita.reader.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.w3c.dom.Element
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

@Singleton
class OpdsClient @Inject constructor() {
    private val client = OkHttpClient.Builder().build()

    fun validate(baseUrl: String, token: String) {
        fetchDocument("$baseUrl${opdsPath(token)}")
    }

    fun libraries(baseUrl: String, token: String): List<OpdsEntry> =
        fetchEntries("$baseUrl${opdsPath(token)}/libraries")

    fun books(baseUrl: String, token: String, libraryId: Int): List<OpdsEntry> {
        val initial = "$baseUrl${opdsPath(token)}/libraries/$libraryId"
        val entries = mutableListOf<OpdsEntry>()
        var nextUrl: String? = initial
        var pageCount = 0

        while (nextUrl != null && pageCount < 100) {
            val document = fetchDocument(nextUrl)
            entries += document.getElementsByTagName("entry").toElementList().map { it.toOpdsEntry() }
            nextUrl = document.getElementsByTagName("link")
                .toElementList()
                .firstOrNull { it.getAttribute("rel") == "next" }
                ?.getAttribute("href")
                ?.takeIf(String::isNotBlank)
                ?.let { resolve(baseUrl, it) }
            pageCount += 1
        }

        return entries
    }

    fun downloadFirstEpub(baseUrl: String, token: String, seriesId: Int): ResponseBody {
        val document = fetchDocument("$baseUrl${opdsPath(token)}/series/$seriesId")
        val href = document.getElementsByTagName("entry")
            .toElementList()
            .asSequence()
            .flatMap { entry -> entry.getElementsByTagName("link").toElementList().asSequence() }
            .firstOrNull { link ->
                link.getAttribute("rel").contains("acquisition") &&
                    link.getAttribute("type").contains("epub", ignoreCase = true)
            }
            ?.getAttribute("href")
            ?.takeIf(String::isNotBlank)
            ?: error("No EPUB acquisition link found for this series")

        val request = Request.Builder().url(resolve(baseUrl, href)).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) error("OPDS download failed with HTTP ${response.code}")
        return response.body ?: error("OPDS download returned an empty body")
    }

    private fun fetchEntries(url: String): List<OpdsEntry> =
        fetchDocument(url).getElementsByTagName("entry")
            .toElementList()
            .map { it.toOpdsEntry() }

    private fun fetchDocument(url: String): org.w3c.dom.Document {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) error("OPDS request failed with HTTP ${response.code}")
        return response.body?.byteStream()?.use { input ->
            xmlFactory().newDocumentBuilder().parse(input)
        } ?: error("OPDS request returned an empty body")
    }

    private fun Element.toOpdsEntry(): OpdsEntry {
        val id = getElementsByTagName("id").item(0)?.textContent?.toIntOrNull() ?: 0
        val title = getElementsByTagName("title").item(0)?.textContent.orEmpty()
        val href = getElementsByTagName("link")
            .toElementList()
            .firstOrNull { it.getAttribute("rel") == "subsection" }
            ?.getAttribute("href")
        return OpdsEntry(id = id, title = title, href = href)
    }

    private fun resolve(baseUrl: String, href: String): String =
        URI(baseUrl).resolve(href.replace("&amp;", "&")).toString()

    private fun opdsPath(token: String): String = "api/opds/${token.trim()}"

    private fun xmlFactory(): DocumentBuilderFactory =
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }

    private fun org.w3c.dom.NodeList.toElementList(): List<Element> =
        (0 until length).mapNotNull { item(it) as? Element }
}

data class OpdsEntry(
    val id: Int,
    val title: String,
    val href: String?
)
