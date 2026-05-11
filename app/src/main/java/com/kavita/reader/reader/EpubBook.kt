package com.kavita.reader.reader

data class EpubBook(
    val title: String,
    val chapters: List<EpubChapter>
)

data class EpubChapter(
    val title: String,
    val body: String
)
