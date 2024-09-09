package com.Skyflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        collect_demo.setOnClickListener {
            val intent = Intent(this, CollectActivity::class.java)
            startActivity(intent)
        }

        upsert_demo.setOnClickListener {
            val intent = Intent(this, UpsertFeature::class.java)
            startActivity(intent)
        }

        validation_demo.setOnClickListener {
            val intent = Intent(this, CustomValidationsActivity::class.java)
            startActivity(intent)
        }

        input_formatting_demo.setOnClickListener {
            val intent = Intent(this, InputFormattingCollect::class.java)
            startActivity(intent)
        }

        composable_elements_demo.setOnClickListener {
            val intent = Intent(this, ComposableActivity::class.java)
            startActivity(intent)
        }

        card_brand_choice_demo.setOnClickListener {
            val intent = Intent(this, CardBrandChoiceActivity::class.java)
            startActivity(intent)
        }
    }
}

