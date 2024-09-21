package com.kamildevelopments.flashcards;

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kamildevelopments.flashcards.adapters.CustomAdapter
import com.kamildevelopments.flashcards.database.AppDatabase
import com.kamildevelopments.flashcards.databinding.ActivityAddSetBinding
import com.kamildevelopments.flashcards.models.Flashcard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class AddSetActivity : AppCompatActivity() {
private lateinit var binding: ActivityAddSetBinding
    private val flashcards = mutableListOf<Flashcard>()
override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
binding = ActivityAddSetBinding.inflate(layoutInflater)
setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val flashcardAdapter = CustomAdapter(flashcards)
    binding.recyclerView.adapter = flashcardAdapter
    binding.recyclerView.layoutManager = LinearLayoutManager(this)
    binding.saveButton.setOnClickListener{
        val question = binding.questionEditText.text.toString()
        val answer = binding.answerEditText.text.toString()

        if (question.isNotEmpty() && answer.isNotEmpty()) {
            val flashcard = Flashcard(question = question, answer = answer, setName = "qwe")
            flashcardAdapter.addFlashcard(flashcard)
            binding.questionEditText.text.clear()
            binding.answerEditText.text.clear()
        } else {
            Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
        }

    }

}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_accept, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_add -> {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "flashcard-database"
                )
                    .fallbackToDestructiveMigration() // Optional for debugging
                    .build()

                val flashcardsToSave = flashcards.toList()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        db.flashcardDao().insertAll(*flashcardsToSave.toTypedArray())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddSetActivity, "Flashcards saved to database!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddSetActivity, "Error saving flashcards: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        e.printStackTrace()
                    }
                }


                true
            }
            R.id.action_settings -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
