package com.Skyflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Skyflow.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.collectDemo.setOnClickListener {
            startActivity(Intent(this, CollectActivity::class.java))
        }

        binding.revealDemo.setOnClickListener {
            startActivity(Intent(this, RevealActivity::class.java))
        }

        binding.validationDemo.setOnClickListener {
            startActivity(Intent(this, CustomValidationsActivity::class.java))
        }

        binding.inputFormattingDemo.setOnClickListener {
            startActivity(Intent(this, InputFormattingCollect::class.java))
        }

        binding.composableElementsDemo.setOnClickListener {
            startActivity(Intent(this, ComposableActivity::class.java))
        }

        binding.cardBrandChoiceDemo.setOnClickListener {
            startActivity(Intent(this, CardBrandChoiceActivity::class.java))
        }
    }
}
