package kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSearchResponse(
    val items: List<GoogleSearchItem>? = null
)

@Serializable
data class GoogleSearchItem(
    val title: String? = null,
    val link: String? = null,
    val snippet: String? = null
)