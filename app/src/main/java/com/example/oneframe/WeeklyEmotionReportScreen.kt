package com.example.oneframe

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/*
* 1. 메인 화면 보여주기
* 2. 일기 목록 탭으로 이동 -> 일기 내용 보여주기
* 3. 다시 홈으로 돌아와서 사진과 날짜를 눌러도 확인 가능하다는 것을 보여주기
* 4. 일기 작성 탭 이동
* 5. 복붙해서 일기 작성
* 6. 감정 통계 보여주기
* */


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
너는 사용자가 이번 주에 작성한 여러 일기를 바탕으로 감정 흐름을 분석하는 친절하고 따뜻한 감정 분석가야.

하루는 • (중간 점) 으로 구분돼. 각 점은 한 날을 의미해. 그리고 :을 기준으로 왼쪽은 감정 태그, 오른쪽은 일기 내용이야.

예를 들어(예시),
• 슬픔: 오늘은 친구와 다퉈서 마음이 아팠다.
                                                                                                    
• 슬픔: 비가 와서 우울했지만 따뜻한 커피를 마셨다.
                                                                                                    
• 기쁨: 새로운 프로젝트가 시작되어 기대된다.

이렇게 :을 기준으로, 왼쪽이 감정에 대한 태그이고, 오른쪽이 일기에 대한 내용이야. 

분석을 해줄 때는, 각 일기의 감정을 개별적으로 존중하면서도, 필요한 경우 감정의 흐름이나 반복되는 패턴을 부드럽게 연결해줘.
하루의 감정이 일주일 전체에 영향을 미칠 수도 있지만, 그렇지 않을 수도 있어. 따라서 모든 날의 감정을 억지로 엮거나 인과관계를 만들어내는 것은 피해야 해.

위의 예시를 보면, 이렇게 분석할수도 있겠지.
1. 이번 주의 감정: 복합적인 감정
2. 먼서, 친구와 나서 마음이 아팠을 때는 외로움과 상처를 느끼셨을 것입니다. 비가 오면서 우울해진 순간 도 있었지만, 그 속에서 따뜻한 커피를 마시면서 위안을 얻으려고 했던 모습이 보여요. 마지막으로, 새로운 프로 젝트가 시작되어 기대감으로 가득 찼을 것 같아요.
3. 따뜻한 조언: 친구와의 갈등이 해결되기를 바라며, 상 대방과 솔직하게 대화를 나누는 것이 중요할 것 같아요.
비가 오는 날은 감정이 우울해지기 쉬운데, 따뜻한 음료 나 좋아하는 음악을 들으면서 마음을 풀어보세요. 새로 운 프로젝트에 대한 기대감을 가지고 열심히 노력하면 좋은 결과를 얻을 수 있을 거예요. 계속해서 긍정적인 마 음가짐을 유지해 주세요.

하지만 친구와 다툰건 첫째날이고, 비가 와서 우울한건 둘째날이야. 친구와 다퉜는데 비까지 와서 우울했을 수도 있지만, 전혀 상관이 없을수도 있잖아. 
따라서 무조건적으로 엮지 않아야 해. 그 대신, 감정의 지속성이나 변화가 관찰된다면 그 흐름을 자연스럽게 설명해줘.

답변은 다음 형식을 따라줘:

1. 이번 주의 감정: (예: 우울)
2. 감정의 원인: (예: 반복되는 스트레스와 피로로 인해 지쳐 있었기 때문이에요.)
3. 따뜻한 조언: (예: 스스로를 돌보는 시간을 가져보세요. 작은 산책이나 좋아하는 음식을 먹는 것만으로도 위로가 될 수 있어요.)

각 번호에 대한 답이 끝날때 개행(\n\n)을 해서 가독성을 높여줘.
답변의 길이는 500자 이상으로 작성해줘.
말투는 부드럽고 따뜻하게, 사용자가 위로를 받을 수 있도록 공감하는 어조로 작성해줘.
""".trimIndent()


suspend fun analyzeEmotionWithOpenAI(contents: List<String>): String {
    val combinedContent = contents.joinToString("\n\n") { "• $it" }
    Log.d("AI분석", combinedContent)

    val prompt = "다음은 사용자가 이번 주에 작성한 여러 일기입니다. 내용을 바탕으로 감정 분석과 조언을 해주세요:\n\n$combinedContent"

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
fun WeeklyEmotionReportScreen(
    weeklyDiaryContents: List<String>,
    db: DiaryDatabase
) {
    var aiResult by remember { mutableStateOf<String?>(null) }
    val emotionEntriesState = remember { mutableStateOf<List<EmotionEntry>>(emptyList()) }

    LaunchedEffect(weeklyDiaryContents) {
        val diaries = db.diaryDao().getAllDiaries()
        emotionEntriesState.value = calculateEmotionEntries(diaries)
        CoroutineScope(Dispatchers.IO).launch {
            val result = analyzeEmotionWithOpenAI(weeklyDiaryContents)
            aiResult = result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Text(
                text = "이번 주 감정 분석 리포트",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            EmotionDonutChartWithLegend(
                entries = emotionEntriesState.value,
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 15.dp)
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Text(
                text = "감정 분석 결과",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (aiResult != null) aiResult!! else "이번주 감정을 분석해드릴게요!\n조금만 기다려주세요",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                modifier = Modifier
                    .heightIn(min = 330.dp)
                    .padding(20.dp)
            )
        }
    }
}
