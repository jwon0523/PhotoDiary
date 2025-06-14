package com.example.oneframe.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "홈")
    object DiaryCardList : BottomNavItem("list", Icons.Default.FormatListNumbered, "일기 목록")
    object DiaryWrite : BottomNavItem("write", Icons.Default.Edit, "작성하기")
    object MyPage : BottomNavItem("my", Icons.Default.Person, "마이페이지")
    object EmotionAnalysis : BottomNavItem("emotionStats", Icons.Default.BarChart, "감정 통계")

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
            is BottomNavItem.EmotionAnalysis -> screen.route
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