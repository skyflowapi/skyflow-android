package com.Skyflow

import Skyflow.*
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName

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
    }
}

