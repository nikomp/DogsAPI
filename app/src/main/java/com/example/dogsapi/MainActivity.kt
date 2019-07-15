package com.example.dogsapi

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.widget.ArrayAdapter



val LOGTAG="myLogs"
var apiServer: APIServer? = null

class MainActivity : AppCompatActivity() {


    var lvBreeds: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализируем Ретрофит
        val gson = GsonBuilder()
            .setLenient()
            .create()

        try {
            // Увеличим таймаут ретрофита
            val client = OkHttpClient.Builder()
            client.connectTimeout(90, TimeUnit.SECONDS)
            client.readTimeout(90, TimeUnit.SECONDS)
            client.writeTimeout(90, TimeUnit.SECONDS)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://dog.ceo/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client.build())
                .build()
            apiServer = retrofit.create(APIServer::class.java)
            Log.d(LOGTAG, "this.apiAccent")

        } catch (e: IllegalArgumentException) {
            Log.d(LOGTAG, "Ошибка инициализации АПИ Сервера " + e.stackTrace)
        }

        // Получим список пород собак
        if (apiServer!=null) {
            Log.d(LOGTAG, "apiServer!=null")
            CoroutineScope(Dispatchers.IO).launch {
                val result = apiServer?.getBreedsList()
                if (result != null) {
                    withContext(Dispatchers.Main) {
                        try {
                            if (result.isSuccessful) {
                                Log.d(LOGTAG, "response=${result.body()}")
                                val gson = Gson().toJson(result.body())
                                Log.d(LOGTAG, gson)

                                // Получим адаптер для отображения
                                val jsonObject: JSONObject = JSONObject(gson);
                                val breeds: JSONObject = jsonObject.getJSONObject("message")
                                var breedsarr: JSONArray=breeds.names()
                                Log.d(LOGTAG, breedsarr.toString())
                                var list = arrayListOf<String>()


                                for (i in 0 until breeds.length()) {
                                    list.add(breedsarr.getString(i))
                                    // Разновидности пород
                                    val breed = breeds.getJSONArray(breedsarr.getString(i))

                                    if (breed.length()>0) {
                                        for (j in 0 until breed.length()) {
                                            list.add(breedsarr.getString(i)+" "+breed.getString(j))
                                        }
                                    }


                                }

                                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, list)

                                val listview = findViewById<ListView>(R.id.breeds)
                                listview.adapter=adapter

                                listview.setOnItemClickListener{
                                        adapterView: AdapterView<*>?,
                                        view: View?, position: Int, l: Long ->

                                    val detail = Intent(this@MainActivity, photo::class.java)
                                    detail.putExtra("breed", list[position])
                                    startActivity(detail)

                                }


                                } else {
                                Log.d(LOGTAG, "Ошибка")
                            }
                        } catch (e: HttpException) {
                            Log.d(LOGTAG, e.message())
                        }
                    }
                }

            }
        }
    }



}
