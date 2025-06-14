package com.example.oneframe.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

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