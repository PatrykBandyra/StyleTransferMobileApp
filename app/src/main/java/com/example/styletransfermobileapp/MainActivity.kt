package com.example.styletransfermobileapp

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.styletransfermobileapp.databinding.ActivityMainBinding
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val client = HttpClient(CIO)
    private var mSelectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnSendRequest.setOnClickListener {
            lifecycleScope.launch {
//                nstApiCall()
                testApiCall()
            }
        }
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    private val galleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data!!.data != null) {
                    mSelectedImageFileUri = result.data!!.data
                }
            }
        }

    private suspend fun nstApiCall() {
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://192.168.0.77:8000/nst",
            formData = formData {
                append("description", "Image to modify")
                append("image", File(mSelectedImageFileUri.toString()).readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                })
            }
        ) {
            onUpload { bytesSentTotal, contentLength ->

            }
        }
    }

    private suspend fun testApiCall() {
        val response: HttpResponse = client.get("http://20.113.133.133:80/")
        val stringBody: String = response.receive()
        Log.i("API", stringBody)
    }

}
