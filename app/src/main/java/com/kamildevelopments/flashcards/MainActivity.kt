package com.kamildevelopments.flashcards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.kamildevelopments.flashcards.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.kamildevelopments.flashcards.adapters.SetAdapter
import com.kamildevelopments.flashcards.database.AppDatabase
import com.kamildevelopments.flashcards.database.FlashcardDao
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
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "flashcard-database").build()
        flashcardDao = db.flashcardDao()
        loadSets()

    }
    private fun loadSets() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sets = flashcardDao.getAllSets()
                Log.d("Database", "Retrieved sets: $sets")
                withContext(Dispatchers.Main) {
                    setAdapter = SetAdapter(sets) { setName ->
                        showFlashcardsForSet(setName) // Handle clicking on a set
                    }
                    binding.setRecyclerView.adapter = setAdapter
                }
            } catch (e: Exception) {
                Log.e("Database", "Error retrieving sets: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    private fun showFlashcardsForSet(setName: String) {
        //val intent = Intent(this, FlashcardSetActivity::class.java)
       // intent.putExtra("SET_NAME", setName)
        //startActivity(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                val intent = Intent(this, AddSetActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onResume(){
        super.onResume()
        loadSets()
    }
}

