package com.kamildevelopments.flashcards.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.kamildevelopments.flashcards.R
import com.kamildevelopments.flashcards.adapters.SetAdapter
import com.kamildevelopments.flashcards.adapters.ShowSetAdapter
import com.kamildevelopments.flashcards.database.AppDatabase
import com.kamildevelopments.flashcards.database.FlashcardDao
import com.kamildevelopments.flashcards.databinding.ActivityShowSetBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowSetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowSetBinding
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var showSetAdapter: ShowSetAdapter
    private lateinit var snapHelper : LinearSnapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowSetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        binding.showSetRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.showSetRecyclerView)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "flashcard-database").build()
        flashcardDao = db.flashcardDao()
    loadFlashcards()
    }
    private fun loadFlashcards() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val set = flashcardDao.getFlashcardsBySet(intent.getStringExtra("SET_NAME")!!)
                Log.d("Database", "Retrieved sets: $set")
                withContext(Dispatchers.Main) {
                    showSetAdapter = ShowSetAdapter(set)
                    binding.showSetRecyclerView.adapter = showSetAdapter
                }
            } catch (e: Exception) {
                Log.e("Database", "Error retrieving sets: ${e.message}")
                e.printStackTrace()
            }
        }
        binding.showSetRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    val centerView = snapHelper.findSnapView(binding.showSetRecyclerView.layoutManager)
                    val pos = binding.showSetRecyclerView.layoutManager!!.getPosition(centerView!!)
                    Log.d("CenterView", "CenterView position: $pos")
                }
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu?.findItem(R.id.action_add)?.isVisible = false
        menu?.findItem(R.id.action_check)?.isVisible = false
        menu?.findItem(R.id.action_download)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}