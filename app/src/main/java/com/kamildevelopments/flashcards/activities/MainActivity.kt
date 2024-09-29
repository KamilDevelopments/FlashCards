package com.kamildevelopments.flashcards.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        sharedPreferences =
            getSharedPreferences("com.kamildevelopments.flashcards", Context.MODE_PRIVATE)
        binding.setRecyclerView.layoutManager = LinearLayoutManager(this)
        val db =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "flashcard-database")
                .build()
        flashcardDao = db.flashcardDao()
        loadSets()
        showFirstTimeMessage()
    }

    // Function to show the alert dialog with the first-time message
    private fun showFirstTimeMessage() {
        val hasSeenMessage = sharedPreferences.getBoolean("hasSeenMessage", false)
        if (!hasSeenMessage) {
            val manager = this.packageManager
            val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
            AlertDialog.Builder(this)
                .setTitle("Welcome to Flashcards App!")
                .setMessage("Thank you for downloading my app! Here, you can create, edit, and manage your flashcards sets. Enjoy learning!\n Also note that this is Alpha - ${info.longVersionCode} version of the app and it might have some bugs. If you find any, or you want your ideas to appear in the app please message me at \n kamildevelopments@gmail.com      ")
                .setPositiveButton("Got it") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            sharedPreferences.edit().putBoolean("hasSeenMessage", true).apply()
        }

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

    private fun showImportConfirmationDialog(
        setName: String,
        numberOfCards: Int,
        onConfirm: (Boolean) -> Unit
    ) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Import Flashcards")
        builder.setMessage("Set Name: $setName\nNumber of Cards: $numberOfCards\n\nDo you want to import this set?")

        // "Confirm" button
        builder.setPositiveButton("Confirm") { dialog, _ ->
            onConfirm(true)
            dialog.dismiss()
        }

        // "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            onConfirm(false)
            dialog.dismiss()
        }

        // Show the dialog
        builder.create().show()
    }

    private fun importSetFromJson() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip != null && clip.itemCount > 0) {
            val json = clip.getItemAt(0).text.toString()
            val gson = Gson()
            val listType = object : TypeToken<List<Flashcard>>() {}.type
            val flashcards: List<Flashcard> = gson.fromJson(json, listType)
            if (flashcards.isNotEmpty()) {
                val setName = flashcards[0].setName
                val numberOfCards = flashcards.size

                showImportConfirmationDialog(setName, numberOfCards) { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val existingSets = flashcardDao.getAllSets()

                                if (existingSets.contains(setName)) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Set with name '$setName' already exists!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    flashcardDao.insertAll(*flashcards.toTypedArray())
                                    Log.d("Import", "Flashcards imported successfully")

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Flashcards imported successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("Import", "Error importing flashcards: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Import canceled", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "No flashcards found in the JSON",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@MainActivity, "No JSON found on clipboard", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun deleteSetFromDatabase(setName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Set")
        builder.setMessage("Are you sure you want to delete the set: $setName?")

        builder.setPositiveButton("Yes") { _, _ ->
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

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

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
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
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

