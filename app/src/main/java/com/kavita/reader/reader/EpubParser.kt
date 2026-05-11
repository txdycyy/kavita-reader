package com.kavita.reader.reader

import org.w3c.dom.Element
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class EpubParser {
    fun parse(file: File): EpubBook {
        ZipFile(file).use { zip ->
            val opfPath = findOpfPath(zip)
            val opfDocument = zip.getInputStream(zip.getEntry(opfPath)).use { input ->
                xmlFactory().newDocumentBuilder().parse(input)
            }
            val title = opfDocument.getElementsByTagName("dc:title")
                .item(0)
                ?.textContent
                ?.takeIf { it.isNotBlank() }
                ?: file.nameWithoutExtension

            val manifest = buildManifest(opfDocument)
            val spineIds = opfDocument.getElementsByTagName("itemref")
                .toElementList()
                .mapNotNull { it.getAttribute("idref").takeIf(String::isNotBlank) }

            val basePath = opfPath.substringBeforeLast('/', "")
            val chapters = spineIds.mapNotNull { idRef ->
                val href = manifest[idRef] ?: return@mapNotNull null
                val entryPath = listOf(basePath, href)
                    .filter { it.isNotBlank() }
                    .joinToString("/")
                    .replace("//", "/")
                val entry = zip.getEntry(entryPath) ?: return@mapNotNull null
                val html = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                EpubChapter(
                    title = entry.name.substringAfterLast('/').substringBeforeLast('.'),
                    body = html.toReadableText()
                )
            }.filter { it.body.isNotBlank() }

            return EpubBook(title = title, chapters = chapters.ifEmpty {
                listOf(EpubChapter(title = title, body = "This EPUB does not expose readable XHTML chapters."))
            })
        }
    }

    private fun findOpfPath(zip: ZipFile): String {
        val container = zip.getInputStream(zip.getEntry("META-INF/container.xml")).use { input ->
            xmlFactory().newDocumentBuilder().parse(input)
        }
        return container.getElementsByTagName("rootfile")
            .toElementList()
            .firstNotNullOfOrNull { it.getAttribute("full-path").takeIf(String::isNotBlank) }
            ?: error("EPUB container does not contain a package document")
    }

    private fun buildManifest(opfDocument: org.w3c.dom.Document): Map<String, String> =
        opfDocument.getElementsByTagName("item")
            .toElementList()
            .associate { it.getAttribute("id") to it.getAttribute("href") }

    private fun xmlFactory(): DocumentBuilderFactory =
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }

    private fun org.w3c.dom.NodeList.toElementList(): List<Element> =
        (0 until length).mapNotNull { item(it) as? Element }

    private fun String.toReadableText(): String =
        replace(Regex("(?is)<(script|style).*?</\\1>"), "")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .lines()
            .joinToString("\n") { it.trim() }
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
}
