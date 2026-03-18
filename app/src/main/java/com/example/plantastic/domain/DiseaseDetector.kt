package com.example.plantastic.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import com.example.plantastic.data.PlantDatabase
import com.example.plantastic.data.model.Disease
import com.example.plantastic.data.model.Treatment
import com.example.plantastic.data.model.TreatmentType
import com.example.plantastic.data.remote.ApiServiceProvider
import com.example.plantastic.data.remote.ChatCompletionRequest
import com.example.plantastic.data.remote.ChatMessage
import com.example.plantastic.data.remote.ContentItem
import com.example.plantastic.data.remote.DiseaseDetectionResult
import com.example.plantastic.data.remote.ImageUrl
import com.example.plantastic.data.remote.ResponseFormat
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Disease detector that uses OpenAI GPT-4 Vision API for plant disease detection.
 * Falls back to mock data if API call fails.
 */
object DiseaseDetector {

    // Use the same Moshi instance as ApiServiceProvider for consistency
    private val moshi: Moshi = ApiServiceProvider.moshi

    /**
     * Analyzes a plant image and detects diseases using OpenAI API.
     * Falls back to mock detection if API fails.
     */
    suspend fun detectDisease(context: Context, imageUri: Uri): Disease {
        return try {
            detectDiseaseFromApi(context, imageUri)
        } catch (e: Exception) {
            // Fallback to mock detection if API fails
            e.printStackTrace()
            detectDiseaseMock(imageUri)
        }
    }

    /**
     * Calls OpenAI API to detect disease from image
     */
    private suspend fun detectDiseaseFromApi(context: Context, imageUri: Uri): Disease {
        return withContext(Dispatchers.IO) {
            val base64Image = encodeImageToBase64(context, imageUri)

            val content = listOf(
                ContentItem.TextContent(
                    text = """Analyze this plant image and identify any diseases. Return a JSON object with the following structure:
{
  "disease": {
    "id": "disease_id",
    "name": "Disease Name",
    "description": "Description of the disease",
    "confidence": 0.0-1.0,
    "affected_area_percent": 0-100,
    "treatments": [
      {
        "id": "treatment_id",
        "name": "Treatment Name",
        "description": "Treatment description",
        "type": "CHEMICAL" or "ORGANIC",
        "application_method": "How to apply",
        "frequency": "How often"
      }
    ]
  }
}

If the plant appears healthy, return:
{
  "disease": {
    "id": "healthy",
    "name": "Healthy",
    "description": "No disease detected. Your plant looks healthy!",
    "confidence": 0.99,
    "affected_area_percent": 0,
    "treatments": []
  }
}"""
                ),
                ContentItem.ImageContent(
                    imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
                )
            )

            val messages = listOf(
                ChatMessage(role = "user", content = content)
            )

            val request = ChatCompletionRequest(
                model = ApiServiceProvider.detectionModel,
                messages = messages,
                responseFormat = ResponseFormat("json_object")
            )

            val response = ApiServiceProvider.detectionApi.analyzePlantImage(request)

            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code()} - ${response.message()}")
            }

            val responseBody = response.body()
                ?: throw Exception("Empty response from API")

            val contentStr = responseBody.choices.firstOrNull()?.message?.content
                ?: throw Exception("No content in API response")

            parseDiseaseResponse(contentStr)
        }
    }

    /**
     * Parses the JSON response from OpenAI API
     */
    private fun parseDiseaseResponse(jsonContent: String): Disease {
        val adapter = moshi.adapter(DiseaseDetectionResult::class.java)
        val result = adapter.fromJson(jsonContent)
            ?: throw Exception("Failed to parse API response")

        val diseaseResult = result.disease
            ?: throw Exception("No disease data in response")

        return Disease(
            id = diseaseResult.id,
            name = diseaseResult.name,
            description = diseaseResult.description,
            confidence = diseaseResult.confidence,
            affectedAreaPercent = diseaseResult.affectedAreaPercent,
            treatments = diseaseResult.treatments.map { treatmentResult ->
                Treatment(
                    id = treatmentResult.id,
                    name = treatmentResult.name,
                    description = treatmentResult.description,
                    type = if (treatmentResult.type.equals("CHEMICAL", ignoreCase = true))
                        TreatmentType.CHEMICAL else TreatmentType.ORGANIC,
                    applicationMethod = treatmentResult.applicationMethod,
                    frequency = treatmentResult.frequency
                )
            }
        )
    }

    /**
     * Encodes image from URI to base64 string
     */
    private fun encodeImageToBase64(context: Context, imageUri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")

        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Mock disease detection for fallback or when API is not configured
     */
    fun detectDiseaseMock(imageUri: Uri): Disease {
        val diseases = PlantDatabase.sampleDiseases
        val random = kotlin.random.Random.nextFloat()

        return when {
            random < 0.25f -> diseases.first { it.id == "powdery_mildew" }
            random < 0.5f -> diseases.first { it.id == "leaf_spot" }
            random < 0.75f -> diseases.first { it.id == "rust" }
            else -> diseases.first { it.id == "healthy" }
        }
    }

    /**
     * Returns a list of detected diseases with their confidence scores.
     * For MVP, returns the top result only.
     */
    suspend fun detectMultipleDiseases(context: Context, imageUri: Uri): List<Disease> {
        val primaryDisease = detectDisease(context, imageUri)
        return listOf(primaryDisease)
    }

    /**
     * Chat with the model about the plant/disease.
     * Uses the disease info and image as context.
     */
    suspend fun chatAboutPlant(
        context: Context,
        imageUri: Uri,
        disease: Disease,
        userMessage: String
    ): String {
        return try {
            chatWithApi(context, imageUri, disease, userMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            "Sorry, I couldn't process your question. Please try again."
        }
    }

    private suspend fun chatWithApi(
        context: Context,
        imageUri: Uri,
        disease: Disease,
        userMessage: String
    ): String {
        return withContext(Dispatchers.IO) {
            val base64Image = encodeImageToBase64(context, imageUri)

            val systemPrompt = """
You are a plant disease expert assistant. The user scanned a plant and the analysis showed:
- Disease: ${disease.name}
- Description: ${disease.description}
- Confidence: ${(disease.confidence * 100).toInt()}%
${if (disease.treatments.isNotEmpty()) "Treatments: ${disease.treatments.joinToString { it.name }}" else ""}

Answer the user's question based on this information. Be helpful, concise, and friendly.
            """.trimIndent()

            val content = listOf(
                ContentItem.TextContent(text = systemPrompt),
                ContentItem.ImageContent(imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image")),
                ContentItem.TextContent(text = userMessage)
            )

            val messages = listOf(
                ChatMessage(role = "user", content = content)
            )

            val request = ChatCompletionRequest(
                model = ApiServiceProvider.chatModel,
                messages = messages,
                maxTokens = 500
            )

            val response = ApiServiceProvider.chatApi.analyzePlantImage(request)

            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code()}")
            }

            val responseBody = response.body()
                ?: throw Exception("Empty response")

            responseBody.choices.firstOrNull()?.message?.content
                ?: throw Exception("No content in response")
        }
    }
}
