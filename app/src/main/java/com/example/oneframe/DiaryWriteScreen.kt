package com.example.oneframe

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.compose.rememberAsyncImagePainter
import com.example.oneframe.ui.theme.OneFrameTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.contracts.contract

@Entity
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
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

@Composable
fun DiaryWriteScreen(context: Context, db: DiaryDatabase) {
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

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
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(1.5.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            TextField(
                value = titleText,
                onValueChange = { titleText = it },
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
                value = contentText,
                onValueChange = { contentText = it },
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
                    .height(450.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    space = 12.dp,
                    alignment = Alignment.End
                ),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            // DB에서 마지막으로 저장된 DiaryEntry를 불러오기
                            val diaries = db.diaryDao().getAllDiaries()
                            if (diaries.isNotEmpty()) {
                                val lastEntry = diaries.last()
                                // 1️⃣ DB 삭제
                                db.diaryDao().deleteDiaryById(lastEntry.id)
                                // 2️⃣ 이미지 삭제
                                deleteImage(context, lastEntry.imageUri)
                            }
                        }

                        // 상태 초기화
                        titleText = ""
                        contentText = ""
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
                            // 선택된 이미지를 내부 저장소에 저장
                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source = ImageDecoder.createSource(context.contentResolver, uri)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                            }
                            val savedUri = saveBitmapToInternalStorage(context, bitmap)

                            val currentTime = System.currentTimeMillis()

                            // Room DB에 저장
                            val entry = DiaryEntry(
                                title = titleText,
                                content = contentText,
                                imageUri = savedUri.toString(),
                                createdAt = currentTime,
                                updatedAt = currentTime
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                db.diaryDao().insertDiary(entry)
                            }

                            // 상태 초기화
                            titleText = ""
                            contentText = ""
                            selectedImageUri = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,   // 버튼 배경을 투명으로
                        contentColor = Color.White            // 버튼 텍스트 색상 지정
                    ),
                    modifier = Modifier
                        .shadow(1.5.dp, shape = RoundedCornerShape(20.dp))
                        .background(
                            MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Text("기록하기")
                }
            }
        }
    }
}

// 내부 저장소에 비트맵 저장 함수
fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"
    val fos: OutputStream?
    var imageUri: Uri? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = uri?.let { context.contentResolver.openOutputStream(it) }
        imageUri = uri
    } else {
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
        imageUri = Uri.fromFile(image)
    }

    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return imageUri
}

// 내부 저장소에서 이미지 파일 삭제 함수
fun deleteImage(context: Context, imageUriString: String) {
    val uri = Uri.parse(imageUriString)
    context.contentResolver.delete(uri, null, null)
}