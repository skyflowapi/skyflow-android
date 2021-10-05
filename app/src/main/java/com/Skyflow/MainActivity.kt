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
        collect_demo.setOnClickListener{
            val intent = Intent(this, CollectActivity::class.java)
            startActivity(intent)
        }
        cvv_generation.setOnClickListener{
            val intent = Intent(this, GenerateCvv::class.java)
            startActivity(intent)
        }
        payment_demo.setOnClickListener{
            val intent = Intent(this, PullFunds::class.java)
            startActivity(intent)
        }
        }
}

