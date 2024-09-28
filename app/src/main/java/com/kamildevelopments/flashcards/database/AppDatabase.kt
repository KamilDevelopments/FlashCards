package com.kamildevelopments.flashcards.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import com.kamildevelopments.flashcards.models.Flashcard

@Dao
interface FlashcardDao {
    @Insert
    suspend fun insertAll(vararg flashcards: Flashcard)

    @Query("SELECT * FROM Flashcard WHERE setName = :setName")
    suspend fun getFlashcardsBySet(setName: String): List<Flashcard>

    @Query("SELECT DISTINCT setName FROM Flashcard")
    suspend fun getAllSets(): List<String>

    @Query("DELETE FROM Flashcard WHERE setName = :setName")
    suspend fun deleteSetBySetName(setName: String)

    @Query("SELECT COUNT(*) FROM Flashcard WHERE setName = :setName")
    suspend fun doesSetExist(setName: String): Int
}

@Database(entities = [Flashcard::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
}