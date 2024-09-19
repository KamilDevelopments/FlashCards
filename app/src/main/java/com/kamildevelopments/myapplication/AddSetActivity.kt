package com.kamildevelopments.myapplication;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kamildevelopments.myapplication.databinding.ActivityAddSetBinding

class AddSetActivity : AppCompatActivity() {
private lateinit var binding: ActivityAddSetBinding

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
binding = ActivityAddSetBinding.inflate(layoutInflater)
setContentView(binding.root)

}
}
