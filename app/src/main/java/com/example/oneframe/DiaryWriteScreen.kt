package com.example.oneframe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.oneframe.data.DiaryDatabase
import com.example.oneframe.data.DiaryEntry
import com.example.oneframe.navigation.BottomNavItem
import com.example.oneframe.navigation.NavigationRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Parcelize
data class DiaryFormState(
    var title: String = "",
    var content: String = "",
    var selectedImageUri: Uri? = null
) : Parcelable

@Composable
private fun emotionList(
    selectedEmotion: String,
    onEmotionSelected: (String) -> Unit
) {
    val emotions = listOf("행복", "슬픔", "기쁨", "분노", "평온")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(emotions) { emotion ->
            Button(
                onClick = { onEmotionSelected(emotion) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedEmotion == emotion) MaterialTheme.colorScheme.primaryContainer else Color.LightGray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(emotion)
            }
        }
    }
}

@Composable
fun DiaryWriteScreen(
    router: NavigationRouter,
    context: Context,
    db: DiaryDatabase
) {
//    var diaryFormState by remember { mutableStateOf(DiaryFormState()) }
    var title: String by remember { mutableStateOf("") }
    var content: String by remember { mutableStateOf("") }
    var selectedImageUri: Uri? by remember { mutableStateOf(null) }
    var selectedEmotion: String by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 15.dp)
    ) {
        Spacer(modifier = Modifier.height(5.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.5.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                placeholder = { Text("오늘의 하루의 제목을 지어봐요") },
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(5.dp))

            emotionList(
                selectedEmotion,
                onEmotionSelected = { selectedEmotion = it }
            )

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier = Modifier.
                fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp)
                    .clickable {
                        galleryLauncher.launch("image/*")
                    }
            ) {
                if(selectedImageUri == null) {
                    Text("사진 선택")
                } else {
                    selectedImageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop, // 꽉 차게 채우기
                            modifier = Modifier
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("오늘의 하루는 어떠셨나요?") },
                maxLines = Int.MAX_VALUE,  // 입력 가능한 줄 수 제한 해제
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )

            // Column에서 wieght으로 Spacer 적용하면 세로 최대 비율 적용됨
            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    space = 12.dp,
                    alignment = Alignment.End
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, bottom = 10.dp)
            ) {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            // DB에서 마지막으로 저장된 DiaryEntry를 불러오기
                            val diaries = db.diaryDao().getAllDiaries()
                            if (diaries.isNotEmpty()) {
                                val lastEntry = diaries.last()
                                // DB 삭제
                                db.diaryDao().deleteDiaryById(lastEntry.id)
                                // 이미지 삭제
                                deleteImage(context, lastEntry.imageUri)
                            }
                        }

                        title = ""
                        content = ""
                        selectedImageUri = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,   // 버튼 배경을 투명으로
                        contentColor = Color.Black            // 버튼 텍스트 색상 지정
                    ),
                    modifier = Modifier
                        .shadow(1.5.dp, shape = RoundedCornerShape(20.dp))
                        .background(Color.White, shape = RoundedCornerShape(20.dp))
                ) {
                    Text("삭제하기")
                }

                Button(
                    onClick = {
                        selectedImageUri?.let { uri ->
                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source = ImageDecoder.createSource(context.contentResolver, uri)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                            }
                            val savedUri = saveBitmapToAppStorage(context, bitmap)

                            val currentTime = System.currentTimeMillis()

                            // Room DB에 저장
                            val entry = DiaryEntry(
                                title = title,
                                selectedEmotion = selectedEmotion,
                                content = content,
                                imageUri = savedUri.toString(),
                                createdAt = currentTime,
                                updatedAt = currentTime
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                db.diaryDao().insertDiary(entry)
                            }

                            // 상태 초기화
                            title = ""
                            content = ""
                            selectedImageUri = null

                            router.navigateTo(BottomNavItem.Home)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,   // 버튼 배경을 투명으로
                        contentColor = Color.White            // 버튼 텍스트 색상 지정
                    ),
                    modifier = Modifier
                        .shadow(1.5.dp, shape = RoundedCornerShape(20.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Text("기록하기")
                }
            }
        }
    }
}

/**
 * 비트맵 이미지를 앱 전용 저장소(내부저장소 전용 폴더)에 저장하고,
 * 저장된 파일의 Uri를 반환하는 함수.
 *
 * @param context Context
 * @param bitmap 저장할 Bitmap 객체
 * @return 저장된 이미지의 Uri
 */
fun saveBitmapToAppStorage(context: Context, bitmap: Bitmap): Uri {
    // 앱 전용 저장소의 images 디렉토리 경로를 가져오기
    val imagesDir = File(context.filesDir, "images")
    if (!imagesDir.exists()) {
        imagesDir.mkdir() // images 폴더가 없으면 생성
    }

    // 고유한 파일명 생성
    val filename = "IMG_${System.currentTimeMillis()}.jpg"
    val imageFile = File(imagesDir, filename)

    // 파일 출력 스트림 열기
    val fos: OutputStream = FileOutputStream(imageFile)

    // 비트맵을 JPEG로 압축해서 파일에 저장
    fos.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }

    // 저장된 파일의 Uri를 반환
    return Uri.fromFile(imageFile)
}

fun deleteImage(context: Context, imageUriString: String) {
    val file = File(Uri.parse(imageUriString).path ?: return)
    if (file.exists()) {
        file.delete()
    }
}