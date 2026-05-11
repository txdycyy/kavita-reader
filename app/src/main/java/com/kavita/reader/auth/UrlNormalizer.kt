package com.kavita.reader.auth

import java.net.URI

object UrlNormalizer {
    fun normalize(input: String): String {
        val trimmed = input.trim()
        require(trimmed.isNotBlank()) { "Server URL is required" }

        val withScheme = if ("://" in trimmed) trimmed else "https://$trimmed"
        val uri = URI(withScheme)
        require(uri.host != null) { "Server URL must include a host" }

        val scheme = uri.scheme.lowercase()
        require(scheme == "http" || scheme == "https") { "Only HTTP and HTTPS URLs are supported" }

        val path = uri.rawPath?.trimEnd('/')?.takeUnless { it.isBlank() } ?: ""
        val port = if (uri.port == -1) "" else ":${uri.port}"
        return "$scheme://${uri.host.lowercase()}$port$path/"
    }
}
