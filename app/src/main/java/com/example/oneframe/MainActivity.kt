package com.example.oneframe

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.oneframe.ui.theme.OneFrameTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.oneframe.ui.theme.Typography
import java.time.format.DateTimeFormatter

// Example emotion entries with color and percent
data class EmotionEntry(
    val label: String,
    val percent: Float,
    val color: Color
)

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DiaryWrite : Screen("dirayWrite")
    data class DiaryDetail(val id: Int) : Screen("diary/{id}") {
        fun createRoute() = "diary/$id"
    }
}

class NavigationRouter(private val navController: NavController) {

    fun navigateTo(screen: Screen) {
        val route = when (screen) {
            is Screen.Home -> Screen.Home.route
            is Screen.DiaryWrite -> Screen.DiaryWrite.route
            is Screen.DiaryDetail -> screen.createRoute()
        }
        navController.navigate(route)
    }

    fun popBack() {
        navController.popBackStack()
    }
}

class MainActivity : ComponentActivity() {
    val emotionDatas = listOf(
        EmotionEntry("행복", 0.32f, Color(0xFF2196F3)),   // Blue
        EmotionEntry("슬픔", 0.15f, Color(0xFF00BCD4)),   // Cyan
        EmotionEntry("기쁨", 0.23f, Color(0xFFE040FB)),   // Magenta
        EmotionEntry("분노", 0.17f, Color(0xFFFF9800)),   // Orange
        EmotionEntry("평온", 0.13f, Color(0xFFFFEB3B))    // Yellow
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val db = DatabaseProvider.getDatabase(context)  // 싱글톤으로 가져오기

            OneFrameTheme {
                val navController = rememberNavController()
                val router = remember { NavigationRouter(navController) }
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            router,
                            emotionDatas,
                            db
                        )
                    }
                    composable(Screen.DiaryWrite.route) {
                        DiaryWriteScreen(
                            router,
                            context,
                            db
                        )
                    }
//                    composable(
//                        Screen.DiaryDetail("").route,
//                        arguments = listOf(navArgument("id") { type = NavType.IntType })
//                    ) { backStackEntry ->
//                        val id = backStackEntry.arguments?.getInt("id") ?: 0
//                        DiaryDetailScreen(router, id)
//                    }
                }

            }
        }
    }
}

@Composable
fun HomeScreen(
    router: NavigationRouter,
    emotionDatas: List<EmotionEntry>,
    db: DiaryDatabase
) {
    val diaryListState = remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }

    // DB에서 데이터 불러오기
    LaunchedEffect(Unit) {
        diaryListState.value = db.diaryDao().getAllDiaries()
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

        WeekCalendar()

        Spacer(modifier = Modifier.height(50.dp))

        EmotionDonutChartWithLegend(emotionDatas)

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
                    // 아래 점 표시
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
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



// 다이어리 헤더와 리스트가 함께 작성된 뷰
@Composable
fun DiaryList(
    sampleList: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(35.dp)
                .padding(horizontal = 15.dp)
        ) {
            Text(
                "DiaryList",
                modifier = Modifier
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DiaryListContent(
            items = sampleList,
            modifier = Modifier
                .background(
                    Color.LightGray,
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
        )
    }
}

// 다이어리 리스트의 내용이 담긴 뷰
@Composable
fun DiaryListContent(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(  // LazyColumn은 수직 스크롤 리스트
        modifier = modifier
    ) {
        // items()를 통해 리스트 항목을 처리
        items(items) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE0E0FF), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "A", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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