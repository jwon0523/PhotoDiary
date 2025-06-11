package com.example.oneframe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.time.LocalDate

@Composable
fun DiaryListScreen(
    db: DiaryDatabase
) {
    // 상태 관리: 일기 리스트와 로딩 상태
    var diaryListState by remember { mutableStateOf<List<DiaryEntry>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // DB에서 데이터 로드
    LaunchedEffect(Unit) {
        val diaries = db.diaryDao().getAllDiaries()
        diaryListState = diaries
        isLoading = false
    }

    // UI 출력
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        diaryListState.isNullOrEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("작성된 일기가 없습니다.", style = MaterialTheme.typography.bodyLarge)
            }
        }

        else -> {
            Column(
                modifier = Modifier.systemBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(15.dp))

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp)
                ) {
                    items(diaryListState!!) { diary ->
                        DiaryCard(diary)
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryCard(
    entry: DiaryEntry,
) {
    Card(
        shape = RoundedCornerShape(7.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(1.8.dp, shape = RoundedCornerShape(7.dp))
    ) {
        Column {
            // 이미지
            Image(
                painter = rememberAsyncImagePainter(entry.imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // 감정 태그와 제목
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                // 감정 태그 (작은 pill 형태)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color(0xFFFFA07A), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = entry.selectedEmotion,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 제목
                    Text(
                        text = entry.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = Color.Black,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = entry.createdAt.toFormattedDate(),
                        maxLines = 1,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.Black
                    )
                }
            }
        }
    }
}