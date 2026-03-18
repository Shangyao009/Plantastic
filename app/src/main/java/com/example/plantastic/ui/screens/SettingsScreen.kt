package com.example.plantastic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantastic.data.SettingsRepository
import com.example.plantastic.data.remote.ChatCompletionRequest
import com.example.plantastic.data.remote.ChatMessage
import com.example.plantastic.data.remote.ContentItem
import com.example.plantastic.data.remote.ApiServiceProvider
import com.example.plantastic.ui.components.PlantasticTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Test result data class
private data class ApiTestResult(val success: Boolean, val message: String)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Unified API settings
    var useCustomApi by remember { mutableStateOf(SettingsRepository.useCustomApi) }
    var apiKey by remember { mutableStateOf(SettingsRepository.apiKey) }
    var apiBaseUrl by remember { mutableStateOf(SettingsRepository.apiBaseUrl) }
    var apiModel by remember { mutableStateOf(SettingsRepository.apiModel) }

    // Separate APIs settings
    var useSeparateApis by remember { mutableStateOf(SettingsRepository.useSeparateApis) }
    var detectionApiKey by remember { mutableStateOf(SettingsRepository.detectionApiKey) }
    var detectionApiBaseUrl by remember { mutableStateOf(SettingsRepository.detectionApiBaseUrl) }
    var detectionApiModel by remember { mutableStateOf(SettingsRepository.detectionApiModel) }
    var chatApiKey by remember { mutableStateOf(SettingsRepository.chatApiKey) }
    var chatApiBaseUrl by remember { mutableStateOf(SettingsRepository.chatApiBaseUrl) }
    var chatApiModel by remember { mutableStateOf(SettingsRepository.chatApiModel) }

    var showSaveSuccess by remember { mutableStateOf(false) }

    // Unified API Test state
    var isTesting by remember { mutableStateOf(false) }
    var unifiedTestResult by remember { mutableStateOf<ApiTestResult?>(null) }

    // Detection API Test state
    var isTestingDetection by remember { mutableStateOf(false) }
    var detectionTestResult by remember { mutableStateOf<ApiTestResult?>(null) }

    // Chat API Test state
    var isTestingChat by remember { mutableStateOf(false) }
    var chatTestResult by remember { mutableStateOf<ApiTestResult?>(null) }

    val scope = rememberCoroutineScope()

    fun testUnifiedApi() {
        if (apiKey.isBlank()) {
            unifiedTestResult = ApiTestResult(false, "API key is empty")
            return
        }

        scope.launch {
            isTesting = true
            unifiedTestResult = null

            val result: ApiTestResult = withContext(Dispatchers.IO) {
                try {
                    SettingsRepository.useCustomApi = true
                    SettingsRepository.useSeparateApis = false
                    SettingsRepository.apiKey = apiKey
                    SettingsRepository.apiBaseUrl = apiBaseUrl
                    SettingsRepository.apiModel = apiModel
                    ApiServiceProvider.recreate()

                    val response = ApiServiceProvider.detectionApi.analyzePlantImage(
                        ChatCompletionRequest(
                            model = apiModel,
                            messages = listOf(
                                ChatMessage(
                                    role = "user",
                                    content = listOf(ContentItem.TextContent(text = "Hi"))
                                )
                            ),
                            maxTokens = 10
                        )
                    )

                    if (response.isSuccessful) {
                        ApiTestResult(true, "API is accessible and working!")
                    } else {
                        ApiTestResult(false, "API error: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    ApiTestResult(false, "Connection failed: ${e.message}")
                }
            }

            unifiedTestResult = result
            isTesting = false
        }
    }

    fun testDetectionApi() {
        if (detectionApiKey.isBlank()) {
            detectionTestResult = ApiTestResult(false, "API key is empty")
            return
        }

        scope.launch {
            isTestingDetection = true
            detectionTestResult = null

            val result: ApiTestResult = withContext(Dispatchers.IO) {
                try {
                    SettingsRepository.useSeparateApis = true
                    SettingsRepository.detectionApiKey = detectionApiKey
                    SettingsRepository.detectionApiBaseUrl = detectionApiBaseUrl
                    SettingsRepository.detectionApiModel = detectionApiModel
                    ApiServiceProvider.recreate()

                    val response = ApiServiceProvider.detectionApi.analyzePlantImage(
                        ChatCompletionRequest(
                            model = detectionApiModel,
                            messages = listOf(
                                ChatMessage(
                                    role = "user",
                                    content = listOf(ContentItem.TextContent(text = "Hi"))
                                )
                            ),
                            maxTokens = 10
                        )
                    )

                    if (response.isSuccessful) {
                        ApiTestResult(true, "Detection API is accessible and working!")
                    } else {
                        ApiTestResult(false, "API error: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    ApiTestResult(false, "Connection failed: ${e.message}")
                }
            }

            detectionTestResult = result
            isTestingDetection = false
        }
    }

    fun testChatApi() {
        if (chatApiKey.isBlank()) {
            chatTestResult = ApiTestResult(false, "API key is empty")
            return
        }

        scope.launch {
            isTestingChat = true
            chatTestResult = null

            val result: ApiTestResult = withContext(Dispatchers.IO) {
                try {
                    SettingsRepository.useSeparateApis = true
                    SettingsRepository.chatApiKey = chatApiKey
                    SettingsRepository.chatApiBaseUrl = chatApiBaseUrl
                    SettingsRepository.chatApiModel = chatApiModel
                    ApiServiceProvider.recreate()

                    val response = ApiServiceProvider.chatApi.analyzePlantImage(
                        ChatCompletionRequest(
                            model = chatApiModel,
                            messages = listOf(
                                ChatMessage(
                                    role = "user",
                                    content = listOf(ContentItem.TextContent(text = "Hi"))
                                )
                            ),
                            maxTokens = 10
                        )
                    )

                    if (response.isSuccessful) {
                        ApiTestResult(true, "Chat API is accessible and working!")
                    } else {
                        ApiTestResult(false, "API error: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    ApiTestResult(false, "Connection failed: ${e.message}")
                }
            }

            chatTestResult = result
            isTestingChat = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PlantasticTopBar(
            title = "API Settings",
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "\uD83D\uDCA1",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "API Configuration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configure LLM APIs for plant disease detection and chat. You can use a single API for both or configure separate APIs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enable Custom APIs Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Custom APIs",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Enable to use your own API credentials",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = useCustomApi,
                            onCheckedChange = { useCustomApi = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF4CAF50),
                                checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (useCustomApi) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Separate APIs",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Use different APIs for detection and chat",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = useSeparateApis,
                                onCheckedChange = { useSeparateApis = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF2196F3),
                                    checkedTrackColor = Color(0xFF2196F3).copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (useCustomApi) {
                if (useSeparateApis) {
                    // ===== SEPARATE APIs MODE =====

                    // Detection API Section
                    DetectionApiSection(
                        apiKey = detectionApiKey,
                        onApiKeyChange = { detectionApiKey = it },
                        apiBaseUrl = detectionApiBaseUrl,
                        onApiBaseUrlChange = { detectionApiBaseUrl = it },
                        apiModel = detectionApiModel,
                        onApiModelChange = { detectionApiModel = it },
                        isTesting = isTestingDetection,
                        testResult = detectionTestResult,
                        onTestClick = { testDetectionApi() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Chat API Section
                    ChatApiSection(
                        apiKey = chatApiKey,
                        onApiKeyChange = { chatApiKey = it },
                        apiBaseUrl = chatApiBaseUrl,
                        onApiBaseUrlChange = { chatApiBaseUrl = it },
                        apiModel = chatApiModel,
                        onApiModelChange = { chatApiModel = it },
                        isTesting = isTestingChat,
                        testResult = chatTestResult,
                        onTestClick = { testChatApi() }
                    )

                } else {
                    // ===== UNIFIED API MODE =====

                    // API Key
                    Text(
                        text = "API Key",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your API key") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Base URL
                    Text(
                        text = "Base URL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = apiBaseUrl,
                        onValueChange = { apiBaseUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://api.openai.com/") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Common endpoints hint
                    ApiHintsCard()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Model
                    Text(
                        text = "Model",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = apiModel,
                        onValueChange = { apiModel = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("gpt-4o") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Common models hint
                    ModelHintsCard()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Test API Button
                    TestApiButton(
                        isTesting = isTesting,
                        enabled = apiKey.isNotBlank(),
                        onClick = { testUnifiedApi() },
                        buttonText = "Test API Connection"
                    )

                    // Test Result
                    unifiedTestResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        TestResultCard(result = result)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Save Button
                Button(
                    onClick = {
                        SettingsRepository.useCustomApi = useCustomApi
                        SettingsRepository.useSeparateApis = useSeparateApis

                        // Unified API settings
                        SettingsRepository.apiKey = apiKey
                        SettingsRepository.apiBaseUrl = apiBaseUrl
                        SettingsRepository.apiModel = apiModel

                        // Separate API settings
                        SettingsRepository.detectionApiKey = detectionApiKey
                        SettingsRepository.detectionApiBaseUrl = detectionApiBaseUrl
                        SettingsRepository.detectionApiModel = detectionApiModel
                        SettingsRepository.chatApiKey = chatApiKey
                        SettingsRepository.chatApiBaseUrl = chatApiBaseUrl
                        SettingsRepository.chatApiModel = chatApiModel

                        ApiServiceProvider.recreate()
                        showSaveSuccess = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Save Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showSaveSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Settings saved successfully!",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Current Status
            CurrentStatusCard(
                useCustomApi = useCustomApi,
                useSeparateApis = useSeparateApis
            )
        }
    }
}

@Composable
private fun DetectionApiSection(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    apiBaseUrl: String,
    onApiBaseUrlChange: (String) -> Unit,
    apiModel: String,
    onApiModelChange: (String) -> Unit,
    isTesting: Boolean,
    testResult: ApiTestResult?,
    onTestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\uD83C\uDF3F Detection API",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Used for plant/disease detection and plant recognition",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API Key
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                placeholder = { Text("Enter detection API key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Base URL
            OutlinedTextField(
                value = apiBaseUrl,
                onValueChange = onApiBaseUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Base URL") },
                placeholder = { Text("https://api.openai.com/") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Model
            OutlinedTextField(
                value = apiModel,
                onValueChange = onApiModelChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Model") },
                placeholder = { Text("gpt-4o (vision model recommended)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TestApiButton(
                isTesting = isTesting,
                enabled = apiKey.isNotBlank(),
                onClick = onTestClick,
                buttonText = "Test Detection API"
            )

            testResult?.let {
                Spacer(modifier = Modifier.height(12.dp))
                TestResultCard(result = it)
            }
        }
    }
}

@Composable
private fun ChatApiSection(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    apiBaseUrl: String,
    onApiBaseUrlChange: (String) -> Unit,
    apiModel: String,
    onApiModelChange: (String) -> Unit,
    isTesting: Boolean,
    testResult: ApiTestResult?,
    onTestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\uD83D\uDCAC Chat API",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Text(
                text = "Used for conversational chat about plants",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API Key
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                placeholder = { Text("Enter chat API key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Base URL
            OutlinedTextField(
                value = apiBaseUrl,
                onValueChange = onApiBaseUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Base URL") },
                placeholder = { Text("https://api.openai.com/") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Model
            OutlinedTextField(
                value = apiModel,
                onValueChange = onApiModelChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Model") },
                placeholder = { Text("gpt-4o (or gpt-3.5-turbo for cost savings)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TestApiButton(
                isTesting = isTesting,
                enabled = apiKey.isNotBlank(),
                onClick = onTestClick,
                buttonText = "Test Chat API"
            )

            testResult?.let {
                Spacer(modifier = Modifier.height(12.dp))
                TestResultCard(result = it)
            }
        }
    }
}

@Composable
private fun ApiHintsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Common Base URLs:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• OpenAI: https://api.openai.com/",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "• Anthropic: https://api.anthropic.com/",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "• Custom: http://localhost:8000/",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ModelHintsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Common Models:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• OpenAI: gpt-4o (vision), gpt-4-turbo, gpt-3.5-turbo",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "• Anthropic: claude-3-opus, claude-3-sonnet",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TestApiButton(
    isTesting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    buttonText: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = !isTesting && enabled,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isTesting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Testing...")
        } else {
            Text(
                text = buttonText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TestResultCard(result: ApiTestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.success) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = result.message,
                fontSize = 13.sp,
                color = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun CurrentStatusCard(
    useCustomApi: Boolean,
    useSeparateApis: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Configuration",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val statusColor = if (useCustomApi) Color(0xFF4CAF50) else Color(0xFFFF9800)
            val statusText = if (useCustomApi) "Custom API" else "Default API"

            Text(
                text = "Status: $statusText",
                fontSize = 14.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )

            if (useCustomApi) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (useSeparateApis) "Mode: Separate APIs" else "Mode: Unified API",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (useSeparateApis) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Detection: ${ApiServiceProvider.detectionModel}",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Chat: ${ApiServiceProvider.chatModel}",
                        fontSize = 12.sp,
                        color = Color(0xFF1565C0)
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Model: ${ApiServiceProvider.detectionModel}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
