package com.example.oneframe.utils

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneframe.EmotionEntry
import com.example.oneframe.data.DiaryEntry
import com.example.oneframe.ui.theme.Typography
import kotlin.collections.forEach

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
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${(entry.percent * 100).toInt()}%",
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}