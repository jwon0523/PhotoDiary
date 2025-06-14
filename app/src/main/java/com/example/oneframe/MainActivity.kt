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
import com.example.oneframe.data.DatabaseProvider
import com.example.oneframe.navigation.BottomNavItem
import com.example.oneframe.navigation.NavigationRouter
import com.example.oneframe.ui.theme.Typography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class EmotionEntry(
    val label: String,
    val percent: Float,
    val color: Color
)

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

                        composable(BottomNavItem.EmotionAnalysis.route) {
                            val context = LocalContext.current
                            val diaryDao = remember { DatabaseProvider.getDatabase(context).diaryDao() }
                            var weeklyContents by remember { mutableStateOf<List<String>>(emptyList()) }

                            LaunchedEffect(Unit) {
                                val allDiaries = diaryDao.getAllDiaries()
                                val now = LocalDate.now()
                                val startOfWeek = now.with(DayOfWeek.MONDAY)
                                val endOfWeek = now.with(DayOfWeek.SUNDAY)

                                val filtered = allDiaries.filter { diary ->
                                    val diaryDate = Instant.ofEpochMilli(diary.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                                    diaryDate in startOfWeek..endOfWeek
                                }.map { diary ->
                                    "${diary.selectedEmotion}: ${diary.content}"
                                }

                                weeklyContents = filtered
                            }

                            WeeklyEmotionReportScreen(
                                weeklyDiaryContents = weeklyContents,
                                db = db
                            )
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
                item = BottomNavItem.EmotionAnalysis,
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