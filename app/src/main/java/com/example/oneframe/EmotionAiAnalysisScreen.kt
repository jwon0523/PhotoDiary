package com.example.oneframe

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// --- OpenAI API 관련 ---
data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

interface OpenAIService {
    @POST("v1/chat/completions")
    suspend fun getEmotionAnalysis(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}

object OpenAIClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    val service: OpenAIService = retrofit.create(OpenAIService::class.java)
}

object OpenAISecrets {
    const val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY
}

val SYSTEM_PROMPT = """
너는 사용자 일기 내용을 기반으로 감정을 분석하고, 그 감정의 원인을 파악한 뒤 조언을 제공하는 친절한 감정 분석가야.
너의 목표는 감정을 정확히 분석하고, 사용자에게 따뜻한 말로 위로하거나 조언을 주는 거야.
답변은 다음 형식을 따라줘:

1. 감정: (예: 슬픔)
2. 감정의 이유: (예: 친구와의 오해로 마음이 상했기 때문이에요.)
3. 조언: (예: 누구나 갈등을 겪을 수 있어요. 너무 자책하지 말고, 대화를 시도해보는 건 어때요?)

말투는 부드럽고 공감 가는 어조로 작성해줘.
""".trimIndent()


suspend fun analyzeEmotionWithOpenAI(content: String): String {
    val prompt = "다음은 사용자의 일기입니다. 내용을 바탕으로 감정 분석과 조언을 해주세요:\n\n$content"
    val request = ChatRequest(
        messages = listOf(
            Message("system", SYSTEM_PROMPT),
            Message("user", prompt)
        )
    )

    val auth = "Bearer ${OpenAISecrets.OPENAI_API_KEY}"

    return try {
        val response = OpenAIClient.service.getEmotionAnalysis(auth, request)
        if (response.isSuccessful) {
            response.body()?.choices?.firstOrNull()?.message?.content ?: "분석 결과 없음"
        } else if (response.code() == 429) {
            "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
        } else {
            "서버 오류: ${response.code()}"
        }
    } catch (e: Exception) {
        "분석 중 오류 발생: ${e.message}"
    }
}

// --- Compose View ---
@Composable
fun EmotionAiAnalysisScreen(content: String) {
    var result by remember { mutableStateOf("AI 분석 중입니다...") }

    LaunchedEffect(content) {
        CoroutineScope(Dispatchers.IO).launch {
            val aiResult = analyzeEmotionWithOpenAI(content)
            result = aiResult
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("AI 감정 분석 결과:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text(result)
    }
}
