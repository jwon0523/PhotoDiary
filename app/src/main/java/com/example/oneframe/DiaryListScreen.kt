package com.example.oneframe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.oneframe.data.DiaryDatabase
import com.example.oneframe.data.DiaryEntry
import com.example.oneframe.navigation.BottomNavItem
import com.example.oneframe.navigation.NavigationRouter
import com.example.oneframe.navigation.toFormattedDate
import com.example.oneframe.utils.getEmotionColor
import kotlin.collections.isNullOrEmpty

@Composable
fun DiaryListScreen(
    router: NavigationRouter,
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
                        DiaryCard(
                            router,
                            diary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryCard(
    router: NavigationRouter,
    entry: DiaryEntry,
) {
    Card(
        shape = RoundedCornerShape(7.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(1.8.dp, shape = RoundedCornerShape(7.dp))
            .clickable {
                router.navigateToId(BottomNavItem.DiaryDetail(entry.id).createRoute())
            }
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
                        .background(getEmotionColor(entry.selectedEmotion), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = entry.selectedEmotion,
                        style = MaterialTheme.typography.labelSmall,
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
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = entry.createdAt.toFormattedDate(),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}