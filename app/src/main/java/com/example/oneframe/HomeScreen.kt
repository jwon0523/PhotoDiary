package com.example.oneframe

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.oneframe.ui.theme.Typography
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    router: NavigationRouter,
    db: DiaryDatabase
) {
    val diaryListState = remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }
    val emotionEntriesState = remember { mutableStateOf<List<EmotionEntry>>(emptyList()) }
    val diaryDatesState = remember { mutableStateOf<List<LocalDate>>(emptyList()) }

    // DB에서 데이터 불러오기
    LaunchedEffect(Unit) {
        val diaries = db.diaryDao().getAllDiaries()
        diaryListState.value = diaries
        emotionEntriesState.value = calculateEmotionEntries(diaries)

        // 작성한 일기 날짜만 추출 (LocalDate로 변환)
        val diaryDates = diaries.map {
            LocalDate.ofEpochDay(it.createdAt / (24 * 60 * 60 * 1000))
        }
        diaryDatesState.value = diaryDates
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "OneFrame,\n당신의 하루를 사진 한장과 기록하세요",
            color = Color.Black,
            style = Typography.titleLarge,
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        ImageCarousel(
            router,
            diaryListState
        )

        Spacer(modifier = Modifier.height(15.dp))

        WeekCalendar(diaryDatesState.value)

        Spacer(modifier = Modifier.height(50.dp))

        EmotionDonutChartWithLegend(
            entries = emotionEntriesState.value
        )

        Button(onClick = { router.navigateTo(Screen.DiaryWrite) }) {
            Text("하루 기록하기")
        }
    }
}

@Composable
fun ImageCarousel(
    router: NavigationRouter,
    diaryListState: MutableState<List<DiaryEntry>>,
    modifier: Modifier = Modifier
) {
    val carouselSize = 280.dp

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(carouselSize),
        horizontalArrangement = Arrangement.spacedBy(8.dp),  // 항목 사이 간격
//        contentPadding = PaddingValues(horizontal = 16.dp)   // 양쪽 여백
    ) {
        // LazyRow나 LazyColumn에서 반복 시에는 items이 스크롤 최적화가 더 잘됨
        items(diaryListState.value) { entry ->
            Box(
                modifier = Modifier
                    .width(carouselSize) // 아이템 너비
                    .fillMaxHeight()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(entry.imageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop, // 꽉 차게 채우기
                    modifier = Modifier
                        .matchParentSize()  // 부모 Box와 같은 크기로
                        .clip(RoundedCornerShape(8.dp))
                )

                Text(
                    text = "${entry.createdAt}",
                    modifier = Modifier.align(Alignment.BottomStart),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun WeekCalendar(
    diaryDate: List<LocalDate> = emptyList(),
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val daysInMonth = remember(currentMonth) {
        val days = mutableListOf<LocalDate>()
        val firstDayOfMonth = currentMonth.withDayOfMonth(1)
        val lastDayOfMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())

        var day = firstDayOfMonth
        while (day <= lastDayOfMonth) {
            days.add(day)
            day = day.plusDays(1)
        }
        days
    }

    Column(modifier = modifier) {
        // 상단 년월 + 화살표 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "",
                    tint = Color.Gray
                )
            }

            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM, d EEEE")),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜 박스 영역 (수평 스크롤)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(daysInMonth) { date ->
                val isSelected = date == selectedDate
                val hasDiary = diaryDate.contains(date)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF6C4AB6) else Color(0xFFF0F0F0))
                        .clickable { selectedDate = date }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Black
                    )

                    Text(
                        text = date.dayOfWeek.name.take(3),
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if(hasDiary) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(Color.Green, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

fun calculateEmotionEntries(diaryList: List<DiaryEntry>): List<EmotionEntry> {
    val emotionCountMap = mutableMapOf<String, Int>()

    // 감정별 개수 세기
    diaryList.forEach { entry ->
        val emotion = entry.selectedEmotion
        if (emotion.isNotBlank()) {
            emotionCountMap[emotion] = emotionCountMap.getOrDefault(emotion, 0) + 1
        }
    }

    val total = emotionCountMap.values.sum().toFloat()

    // EmotionEntry 리스트로 변환
    return emotionCountMap.map { (emotion, count) ->
        EmotionEntry(
            label = emotion,
            percent = count / total,
            color = getEmotionColor(emotion)
        )
    }
}

// 감정별 색상 매칭 함수
fun getEmotionColor(emotion: String): Color {
    return when (emotion) {
        "행복" -> Color(0xFFE57373)
        "슬픔" -> Color(0xFF64B5F6)
        "기쁨" -> Color(0xFFFFB74D)
        "분노" -> Color(0xFFBA68C8)
        "평온" -> Color(0xFF4DB6AC)
        else -> Color.LightGray
    }
}

@Composable
fun EmotionDonutChartWithLegend(
    entries: List<EmotionEntry>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut chart
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .padding(8.dp)
            ) {
                val diameter = size.minDimension
                val thickness = diameter * 0.24f
                var startAngle = -90f
                for (entry in entries) {
                    val sweep = entry.percent * 360f
                    drawArc(
                        color = entry.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = thickness
                        )
                    )
                    startAngle += sweep
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                for (entry in entries) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable {
                                Log.i("EmotionChart", "$entry")

                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(entry.color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = entry.label,
                            style = Typography.labelMedium,
                            color = Color.Black,
                            modifier = Modifier.width(50.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${(entry.percent * 100).toInt()}%",
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}