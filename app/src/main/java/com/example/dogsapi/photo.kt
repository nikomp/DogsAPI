package com.example.dogsapi

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.net.URLEncoder

class photo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        val ivAvatar = findViewById<ImageView>(R.id.ivPhoto)

        val breed = intent.extras.getString("breed").replace(" ","/")
        val url = "https://dog.ceo/api/breed/$breed/images/random"

        Log.d(LOGTAG, url)
        // Получим адрес рандомной фото
        CoroutineScope(Dispatchers.IO).launch {
            val result = apiServer?.getImageURL(url)
            if (result != null) {
                withContext(Dispatchers.Main) {
                    try {
                        if (result.isSuccessful) {
                            Log.d(LOGTAG, "response=${result.body()}")

                            val gson = Gson().toJson(result.body())
                            Log.d(LOGTAG, gson)

                            // Получим адаптер для отображения
                            val jsonObject: JSONObject = JSONObject(gson);
                            val imageUrl: String = jsonObject.getString("message")

                            val glideUrl = GlideUrl(
                                imageUrl, LazyHeaders.Builder()
                                    .build()
                            )

                            Glide
                                .with(this@photo)
                                .load(glideUrl)
                                .into(ivAvatar)
                            
                            
                        } else {
                            Log.d(LOGTAG, "Ошибка")
                        }
                    } catch (e: HttpException) {
                        Log.d(LOGTAG, e.message())
                    }
                }
            }

        }



        //var url="""https://images.dog.ceo/breeds/airedale/n02096051_3472.jpg"""





        /*val glideUrl = GlideUrl(
            url, LazyHeaders.Builder()
                .build()
        )

        Glide
            .with(this)
            .load(glideUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(ivAvatar)*/
    }
}
