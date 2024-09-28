package com.kamildevelopments.flashcards.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.kamildevelopments.flashcards.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kamildevelopments.flashcards.R
import com.kamildevelopments.flashcards.adapters.SetAdapter
import com.kamildevelopments.flashcards.database.AppDatabase
import com.kamildevelopments.flashcards.database.FlashcardDao
import com.kamildevelopments.flashcards.models.Flashcard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var setAdapter: SetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.setRecyclerView.layoutManager = LinearLayoutManager(this)
        val db =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "flashcard-database")
                .build()
        flashcardDao = db.flashcardDao()
        loadSets()

    }

    private fun loadSets() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sets = flashcardDao.getAllSets()
                Log.d("Database", "Retrieved sets: $sets")
                withContext(Dispatchers.Main) {
                    setAdapter = SetAdapter(sets, { setName ->
                        showFlashcardsForSet(setName)
                    },
                        { setName ->
                            deleteSetFromDatabase(setName)
                        }, { setName ->
                            exportSetToJson(setName)
                        })
                    binding.setRecyclerView.adapter = setAdapter
                }
            } catch (e: Exception) {
                Log.e("Database", "Error retrieving sets: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun exportSetToJson(setName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val flashcards = flashcardDao.getFlashcardsBySet(setName)
                val gson = Gson()
                val json = gson.toJson(flashcards)
                Log.d("Export", "Flashcards JSON: $json")
                withContext(Dispatchers.Main) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Flashcards JSON", json)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(
                        this@MainActivity,
                        "Flashcards copied to clipboard!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("Export", "Error exporting flashcards: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun importSetFromJson() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip != null && clip.itemCount > 0) {
            val json = clip.getItemAt(0).text.toString()

            // Convert JSON back to Flashcard objects
            val gson = Gson()
            val listType = object : TypeToken<List<Flashcard>>() {}.type
            val flashcards: List<Flashcard> = gson.fromJson(json, listType)

            // Check if the set name already exists
            if (flashcards.isNotEmpty()) {
                val setName = flashcards[0].setName

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val existingSets = flashcardDao.getAllSets()

                        if (existingSets.contains(setName)) {
                            // Set name already exists, notify the user
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Set with name '$setName' already exists!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // Set name doesn't exist, proceed with insertion
                            flashcardDao.insertAll(*flashcards.toTypedArray())
                            Log.d("Import", "Flashcards imported successfully")

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Flashcards imported successfully!", Toast.LENGTH_SHORT).show()
                           loadSets()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Import", "Error importing flashcards: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        } else {
            Toast.makeText(this@MainActivity, "No JSON found on clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSetFromDatabase(setName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                flashcardDao.deleteSetBySetName(setName)
                loadSets()
            } catch (e: Exception) {
                Log.e("Database", "Error deleting set: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun showFlashcardsForSet(setName: String) {
        val intent = Intent(this, ShowSetActivity::class.java)
        intent.putExtra("SET_NAME", setName)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu?.findItem(R.id.action_check)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                val intent = Intent(this, AddSetActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.action_download -> {
                importSetFromJson()
                true
            }

            R.id.action_settings -> {

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSets()
    }
}

