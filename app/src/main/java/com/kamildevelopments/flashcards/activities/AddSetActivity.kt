package com.kamildevelopments.flashcards.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kamildevelopments.flashcards.R
import com.kamildevelopments.flashcards.adapters.CustomAdapter
import com.kamildevelopments.flashcards.database.AppDatabase
import com.kamildevelopments.flashcards.databinding.ActivityAddSetBinding
import com.kamildevelopments.flashcards.models.Flashcard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddSetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddSetBinding
    private val flashcards = mutableListOf<Flashcard>()
    private lateinit var db: AppDatabase
    private var setName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "flashcard-database"
        ).fallbackToDestructiveMigration().build()
        val flashcardAdapter = CustomAdapter(flashcards)
        binding.recyclerView.adapter = flashcardAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.saveButton.setOnClickListener {
            val question = binding.questionEditText.text.toString()
            val answer = binding.answerEditText.text.toString()

            if (question.isNotEmpty() && answer.isNotEmpty()) {
                val exists = flashcards.any { it.question.equals(question, ignoreCase = true) }

                if (exists) {
                    Toast.makeText(this, "This question already exists!", Toast.LENGTH_SHORT).show()
                } else {
                    val flashcard =
                        Flashcard(question = question, answer = answer, setName = setName)
                    flashcardAdapter.addFlashcard(flashcard)
                    binding.questionEditText.text.clear()
                    binding.answerEditText.text.clear()
                }

            } else {
                Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
            }

        }
        setupAddSetFlow()

    }

    private fun setupAddSetFlow() {
        binding.setCardView.visibility = android.view.View.GONE
        binding.saveNameButton.setOnClickListener {


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    setName = binding.setNameEditText.text.toString()
                    val setExists = db.flashcardDao().doesSetExist(setName) > 0

                    withContext(Dispatchers.Main) {
                        if (!setExists) {
                            binding.setCardView.visibility = android.view.View.VISIBLE
                            binding.setNameCardView.visibility = android.view.View.GONE
                        } else {
                            Toast.makeText(
                                this@AddSetActivity,
                                "Set with this name already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Log.d("Database", "Set exists: $setExists for setName: $setName")

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AddSetActivity,
                            "Error retrieving set names: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Log.e("Database", "Error retrieving set names", e)
                    e.printStackTrace()
                }
            }


        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu?.findItem(R.id.action_add)?.isVisible = false
        menu?.findItem(R.id.action_download)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.action_check -> {

                val flashcardsToSave = flashcards.toList()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        db.flashcardDao().insertAll(*flashcardsToSave.toTypedArray())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AddSetActivity,
                                "Flashcards saved to database!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AddSetActivity,
                                "Error saving flashcards: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        e.printStackTrace()
                    }
                }


                true
            }

            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
