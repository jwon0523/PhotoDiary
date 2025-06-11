package com.example.oneframe

import android.content.Context
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.compose.rememberAsyncImagePainter
import com.example.oneframe.ui.theme.Typography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class EmotionEntry(
    val label: String,
    val percent: Float,
    val color: Color
)

@Entity
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val selectedEmotion: String,
    val content: String,
    val imageUri: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
interface DiaryDao {
    @Insert
    suspend fun insertDiary(entry: DiaryEntry)

    @Query("SELECT * FROM DiaryEntry")
    suspend fun getAllDiaries(): List<DiaryEntry>

    @Query("SELECT * FROM DiaryEntry WHERE id = :entryId")
    suspend fun getDiaryById(entryId: Int): DiaryEntry?

    @Query("DELETE FROM DiaryEntry WHERE id = :entryId")
    suspend fun deleteDiaryById(entryId: Int)
}

@Database(entities = [DiaryEntry::class], version = 1)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}

object DatabaseProvider {
    @Volatile
    private var INSTANCE: DiaryDatabase? = null

    fun getDatabase(context: Context): DiaryDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                DiaryDatabase::class.java,
                "diary_database"
            )
                .fallbackToDestructiveMigration() // DB 스키마 변경 시 기존 데이터 삭제
                .build()
            INSTANCE = instance
            instance
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "홈")
    object DiaryCardList : BottomNavItem("list", Icons.Default.FormatListNumbered, "일기 목록")
    object DiaryWrite : BottomNavItem("write", Icons.Default.Edit, "작성하기")
    object MyPage : BottomNavItem("my", Icons.Default.Person, "마이페이지")
    object EmotionStats : BottomNavItem("emotionStats", Icons.Default.BarChart, "감정 통계")

    // id를 받는 라우트는 일반 객체가 아닌 data class로
    data class DiaryDetail(val id: Int) : BottomNavItem("diary/{id}", Icons.Default.Description, "일기 상세") {
        fun createRoute(): String = "diary/$id"
    }
}

fun Long.toFormattedDate(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("yy.MM.dd"))
}

class NavigationRouter(private val navController: NavController) {

    fun navigateTo(screen: BottomNavItem) {
        val route = when (screen) {
            is BottomNavItem.Home -> screen.route
            is BottomNavItem.DiaryWrite -> screen.route
            is BottomNavItem.DiaryCardList -> screen.route
            is BottomNavItem.EmotionStats -> screen.route
            is BottomNavItem.MyPage -> screen.route
            is BottomNavItem.DiaryDetail -> screen.createRoute()
        }
        navController.navigate(route)
    }

    fun navigateToId(route: String) {
        navController.navigate(route)
    }

    fun popBack() {
        navController.popBackStack()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val db = DatabaseProvider.getDatabase(context)  // 싱글톤으로 가져오기

            val navController = rememberNavController()
            val router = remember { NavigationRouter(navController) }

            // 선택된 탭 상태를 remember로 관리
            val selectedItem = remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }

            OneFrameTheme {
                Scaffold(
                    bottomBar = {
                        CustomBottomBar(
                            selectedItem = selectedItem.value,
                            onItemSelected = { item ->
                                selectedItem.value = item
                                router.navigateTo(item) // 클릭 시 Navigation 처리
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavItem.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomNavItem.Home.route) {
                            HomeScreen(
                                router,
                                db
                            )
                        }

                        composable(BottomNavItem.DiaryCardList.route) {
                            DiaryListScreen(
                                router,
                                db
                            )
                        }

                        composable(BottomNavItem.DiaryWrite.route) {
                            DiaryWriteScreen(
                                router,
                                context,
                                db
                            )
                        }

                        composable(BottomNavItem.EmotionStats.route) {
                            EmotionStatsScreen()
                        }

                        composable(BottomNavItem.MyPage.route) {
                            MyPageScreen()
                        }

                        composable("diary/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                            DiaryDetailScreen(
                                diaryId = id,
                                db = db,
                                router = router
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                item = BottomNavItem.Home,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = Modifier.weight(1f)
            )

            BottomBarItem(
                item = BottomNavItem.DiaryCardList,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.weight(1f)) // 중앙 버튼 영역 비움

            BottomBarItem(
                item = BottomNavItem.EmotionStats,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = Modifier.weight(1f)
            )

            BottomBarItem(
                item = BottomNavItem.MyPage,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = Modifier.weight(1f)
            )
        }

        // 중앙의 큰 버튼
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp) // 위로 올려서 float처럼 보이게
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                .clickable { onItemSelected(BottomNavItem.DiaryWrite) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "작성",
                tint = Color.White
            )
        }
    }
}

@Composable
fun BottomBarItem(
    item: BottomNavItem,
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier
) {
    val isSelected = item == selectedItem
    val color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onItemSelected(item) }
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = color
        )
        Text(
            text = item.label,
            color = color,
            fontSize = 12.sp
        )
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