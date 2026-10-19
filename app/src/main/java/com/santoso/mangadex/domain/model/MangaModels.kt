package com.santoso.mangadex.domain.model

import com.google.gson.annotations.SerializedName

data class MangaListResponse(
    val result: String,
    val response: String,
    val data: List<MangaData>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

data class MangaResponse(
    val result: String,
    val response: String,
    val data: MangaData
)

data class MangaData(
    val id: String,
    val type: String,
    val attributes: MangaAttributes,
    val relationships: List<Relationship> = emptyList()
)

data class MangaAttributes(
    val title: Map<String, String>,
    val altTitles: List<Map<String, String>>? = null,
    val description: Map<String, String>? = null,
    val status: String? = null,
    val contentRating: String? = null,
    val lastVolume: String? = null,
    val lastChapter: String? = null,
    val tags: List<Tag>? = null,
    val state: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Tag(
    val id: String,
    val type: String,
    val attributes: TagAttributes
)

data class TagAttributes(
    val name: Map<String, String>,
    val group: String
)

data class Relationship(
    val id: String,
    val type: String,
    val attributes: RelationshipAttributes? = null
)

data class RelationshipAttributes(
    val fileName: String? = null,
    val name: String? = null
)

data class ChapterListResponse(
    val result: String,
    val response: String,
    val data: List<ChapterData>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

data class ChapterData(
    val id: String,
    val type: String,
    val attributes: ChapterAttributes,
    val relationships: List<Relationship>
)

data class ChapterAttributes(
    val volume: String?,
    val chapter: String?,
    val title: String?,
    val translatedLanguage: String,
    val externalUrl: String?,
    val publishAt: String,
    val readableAt: String,
    val createdAt: String,
    val updatedAt: String,
    val pages: Int,
    val version: Int
)

data class AtHomeResponse(
    val result: String,
    val baseUrl: String,
    val chapter: AtHomeChapter
)

data class AtHomeChapter(
    val hash: String,
    val data: List<String>,
    val dataSaver: List<String>
)
