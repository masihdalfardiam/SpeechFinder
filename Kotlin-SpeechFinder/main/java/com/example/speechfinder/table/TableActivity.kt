package com.example.speechfinder.table

import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.speachfinder.R
import com.example.speechfinder.ApiResponseReload
import com.example.speechfinder.SharedPrefUtil
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class TableActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_table)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchData()
    }


    private fun fetchData() {
        val token = "Token " + SharedPrefUtil.getToken(applicationContext)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.speechfinder.ir/reload")
            .addHeader("Authorization", token)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                runOnUiThread {
                    Toast.makeText(this@TableActivity, "Request Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val apiResponseReload = Gson().fromJson(json, ApiResponseReload::class.java)

                    runOnUiThread {
                        adapter = UserInfoAdapter(
                            apiResponseReload.data.user,
                            apiResponseReload.data.numbers
                        )
                        recyclerView.adapter = adapter
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@TableActivity, "Request Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }
}