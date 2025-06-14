package com.example.oneframe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.oneframe.data.DiaryDatabase
import com.example.oneframe.data.DiaryEntry
import com.example.oneframe.navigation.NavigationRouter
import com.example.oneframe.navigation.toFormattedDate
import com.example.oneframe.utils.getEmotionColor

@Composable
fun DiaryDetailScreen(diaryId: Int, db: DiaryDatabase, router: NavigationRouter) {
    var diaryState = remember { mutableStateOf<DiaryEntry?>(null) }

    LaunchedEffect(Unit) {
        diaryState.value = db.diaryDao().getDiaryById(diaryId)
    }

    diaryState?.let { entry ->
        val entry = diaryState.value ?: return
        DiaryContent(entry)
    } ?: run {
        // 로딩 중 혹은 에러 처리 뷰
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("일기를 불러오는 중입니다...")
        }
    }
}

@Composable
private fun DiaryContent(entry: DiaryEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())  // 내용 스크롤 가능하게
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        // 제목 + 감정
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            EmotionTag(emotion = entry.selectedEmotion)
        }

       Spacer(modifier = Modifier.height(15.dp))

        // 대표 이미지
        Image(
            painter = rememberAsyncImagePainter(entry.imageUri),
            contentDescription = "Diary Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜
        Text(
            text = entry.createdAt.toFormattedDate(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 본문 내용
        Text(
            text = entry.content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun EmotionTag(emotion: String) {
    val color = getEmotionColor(emotion)

    Box(
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = emotion,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}