package com.example.dogsapi

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

const val site="https://dog.ceo/api/breed/"
const val imgDir="/images/random"

class Photo : AppCompatActivity() {


    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        val ivAvatar = findViewById<ImageView>(R.id.ivPhoto)

        val breed = intent.extras?.getString("breed")?.replace(" ","/")
        val url = site+breed+imgDir

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
                            val jsonObject = JSONObject(gson)
                            val imageUrl: String = jsonObject.getString("message")

                            val glideUrl = GlideUrl(
                                imageUrl, LazyHeaders.Builder()
                                    .build()
                            )

                            Glide
                                .with(this@Photo)
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

    }
}
