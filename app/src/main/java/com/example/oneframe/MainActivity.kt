package com.example.oneframe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.oneframe.ui.theme.OneFrameTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import java.time.YearMonth

// 샘플 데이터
val sampleList = listOf("사과", "바나나", "체리", "포도", "딸기")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OneFrameTheme {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CalendarScreen()

                    SimpleList(items = sampleList)
                }
            }
        }
    }
}

@Composable
fun SimpleList(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(  // LazyColumn은 수직 스크롤 리스트
        modifier = modifier.padding(16.dp)
    ) {
        // items()를 통해 리스트 항목을 처리
        items(items) { item ->
            // 리스트 아이템 UI
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

// 커스텀 캘린더 Composable 함수
@Composable
fun CustomCalendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate = LocalDate.now(),  // 선택된 날짜를 나타내는 상태
    onDateSelected: (LocalDate) -> Unit = {}    // 날짜를 선택했을 때 호출될 콜백
) {
    // 현재 달을 상태로 관리 (월 이동 버튼 클릭 시 업데이트)
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // 현재 달의 날짜 수 (28~31일)
    val daysInMonth = currentMonth.lengthOfMonth()
    // 현재 달의 첫 번째 날짜
    val firstDayOfMonth = currentMonth.atDay(1)
    // 첫 번째 날의 요일 (Sunday=0, Saturday=6)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    Column(modifier = modifier.padding(16.dp)) {
        // 상단: 월/연도 + 이전/다음 월 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 현재 월/연도 표시 (예: June 2025)
            Text(
                text = "${currentMonth.month} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            // 월 이동 버튼 (<, >)
            Row {
                Text(
                    text = "<",
                    modifier = Modifier
                        .clickable { currentMonth = currentMonth.minusMonths(1) }  // 이전 달로 이동
                        .padding(horizontal = 8.dp),
                    fontSize = 18.sp
                )
                Text(
                    text = ">",
                    modifier = Modifier
                        .clickable { currentMonth = currentMonth.plusMonths(1) }   // 다음 달로 이동
                        .padding(horizontal = 8.dp),
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 요일(일~토) 표시
        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.values().forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.name.take(3),      // 요일 이름 앞 3글자 (예: SUN, MON)
                    modifier = Modifier.weight(1f),     // 7등분으로 나눔
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 날짜를 그리드 형태로 표시
        // 첫 주의 요일에 따라 공백칸을 넣기 위해 전체 셀 수 계산
        val totalCells = daysInMonth + firstDayOfWeek

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),               // 7열 고정
            modifier = Modifier.height(300.dp)         // 높이 고정
        ) {
            items(totalCells) { index ->
                if (index < firstDayOfWeek) {
                    // 첫 주에서 비어있는 칸 처리 (1일 이전의 공백)
                    Box(modifier = Modifier.size(40.dp))
                } else {
                    // 날짜를 계산 (1일부터 시작)
                    val day = index - firstDayOfWeek + 1
                    val date = currentMonth.atDay(day)
                    val isSelected = date == selectedDate  // 선택된 날짜인지 확인

                    // 날짜 셀 UI
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(2.dp)
                            .background(
                                // 선택된 날짜는 primaryContainer 색으로 강조
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { onDateSelected(date) },  // 클릭 시 콜백 호출
                        contentAlignment = Alignment.Center
                    ) {
                        // 날짜 숫자 표시
                        Text(
                            text = day.toString(),
                            fontSize = 14.sp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer  // 선택된 날짜의 글자색
                            else
                                MaterialTheme.colorScheme.onSurface           // 일반 날짜의 글자색
                        )
                    }
                }
            }
        }
    }
}

// 예시: CustomCalendar를 화면에 띄우는 Composable
@Composable
fun CalendarScreen() {
    // 선택된 날짜를 상태로 관리
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 배경색을 MaterialTheme에 맞추어 Surface로 전체 배경 설정
    Surface(modifier = Modifier, color = MaterialTheme.colorScheme.background) {
        CustomCalendar(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                // 날짜 클릭 시 선택된 날짜 업데이트
                selectedDate = date
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OneFramePreview() {
    CalendarScreen()
}