package com.example.plantastic.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * OpenAI Chat Completions API request body
 */
@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "response_format") val responseFormat: ResponseFormat? = null,
    @Json(name = "max_tokens") val maxTokens: Int = 2048
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: List<ContentItem>
)

/**
 * Sealed class for content items in chat messages.
 * Uses Moshi's KotlinJsonAdapterFactory for serialization.
 */
sealed class ContentItem {
    data class TextContent(
        val type: String = "text",
        val text: String
    ) : ContentItem()

    data class ImageContent(
        val type: String = "image_url",
        val imageUrl: ImageUrl
    ) : ContentItem()
}

@JsonClass(generateAdapter = true)
data class ImageUrl(
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    @Json(name = "type") val type: String = "json_object"
)

/**
 * OpenAI Chat Completions API response body
 */
@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    @Json(name = "id") val id: String,
    @Json(name = "choices") val choices: List<Choice>,
    @Json(name = "usage") val usage: Usage? = null
)

@JsonClass(generateAdapter = true)
data class Choice(
    @Json(name = "index") val index: Int,
    @Json(name = "message") val message: ResponseMessage,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens") val promptTokens: Int,
    @Json(name = "completion_tokens") val completionTokens: Int,
    @Json(name = "total_tokens") val totalTokens: Int
)

/**
 * Parsed disease detection result from LLM response
 */
@JsonClass(generateAdapter = true)
data class DiseaseDetectionResult(
    @Json(name = "disease") val disease: DiseaseResult?
)

@JsonClass(generateAdapter = true)
data class DiseaseResult(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "affected_area_percent") val affectedAreaPercent: Float,
    @Json(name = "treatments") val treatments: List<TreatmentResult>
)

@JsonClass(generateAdapter = true)
data class TreatmentResult(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "type") val type: String,
    @Json(name = "application_method") val applicationMethod: String,
    @Json(name = "frequency") val frequency: String
)

/**
 * LLM-based plant detection result (includes plant type and disease)
 */
@JsonClass(generateAdapter = true)
data class PlantDetectionResult(
    @Json(name = "is_plant") val isPlant: Boolean,
    @Json(name = "plant_type") val plantType: String?, // "leaf", "flower", "fruit", "root", "mushroom", etc.
    @Json(name = "plant_description") val plantDescription: String?,
    @Json(name = "disease") val disease: DiseaseResult?
)
