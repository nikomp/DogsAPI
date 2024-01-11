package com.example.dogsapi

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


const val LOGTAG="myLogs"
const val baseUrl="https://dog.ceo/api/"
var apiServer: APIServer? = null
var jobBreeds: Job?=null

class MainActivity : AppCompatActivity() {


    init {
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
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client.build())
                .build()
            apiServer = retrofit.create(APIServer::class.java)
            Log.d(LOGTAG, "this.apiAccent")

        } catch (e: IllegalArgumentException) {
            Log.d(LOGTAG, "Ошибка инициализации АПИ Сервера " + e.stackTrace)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Получим список пород собак
        if (apiServer!=null) {
            Log.d(LOGTAG, "apiServer!=null")
            jobBreeds= CoroutineScope(Dispatchers.IO).launch {
                val result = apiServer?.getBreedsList()
                if (result != null) {
                    withContext(Dispatchers.Main) {
                        try {
                            if (result.isSuccessful) {
                                Log.d(LOGTAG, "response=${result.body()}")
                                val gson = Gson().toJson(result.body())
                                Log.d(LOGTAG, gson)

                                // Получим адаптер для отображения
                                val jsonObject = JSONObject(gson)
                                val breeds: JSONObject = jsonObject.getJSONObject("message")
                                val breedsarr: JSONArray=breeds.names()
                                Log.d(LOGTAG, breedsarr.toString())
                                val list = arrayListOf<String>()


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
                                        _,
                                        _,
                                        position: Int,
                                        _: Long ->

                                    val detail = Intent(this@MainActivity, Photo::class.java)
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

    override fun onStop() {
        jobBreeds?.cancel()
        super.onStop()
    }



}
