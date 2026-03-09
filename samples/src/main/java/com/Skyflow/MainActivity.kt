package com.Skyflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Skyflow.databinding.ActivityMainBinding
import com.Skyflow.databinding.ActivityRevealBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.collectDemo.setOnClickListener {
            val intent = Intent(this, CollectActivity::class.java)
            startActivity(intent)
        }

        binding.upsertDemo.setOnClickListener {
            val intent = Intent(this, UpsertFeature::class.java)
            startActivity(intent)
        }

        binding.validationDemo.setOnClickListener {
            val intent = Intent(this, CustomValidationsActivity::class.java)
            startActivity(intent)
        }

        binding.inputFormattingDemo.setOnClickListener {
            val intent = Intent(this, InputFormattingCollect::class.java)
            startActivity(intent)
        }

        binding.composableElementsDemo.setOnClickListener {
            val intent = Intent(this, ComposableActivity::class.java)
            startActivity(intent)
        }

        binding.cardBrandChoiceDemo.setOnClickListener {
            val intent = Intent(this, CardBrandChoiceActivity::class.java)
            startActivity(intent)
        }
    }
}

