package com.example.styletransfermobileapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.styletransfermobileapp.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class MainActivity : BaseActivity<ActivityMainBinding>(), UploadRequestBody.UploadCallback {

    private val client = HttpClient(CIO)
    private var mSelectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnSendRequest.setOnClickListener {
            makeAPICall()
        }

        binding.ivImage.setOnClickListener {
            chooseImageFromGallery()
        }
    }

    private fun makeAPICall() {
        if (mSelectedImageFileUri == null) {
            Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
        } else {
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(mSelectedImageFileUri!!, "r", null) ?: return
            val file = File(cacheDir, contentResolver.getFileName(mSelectedImageFileUri!!))
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            binding.progressBar.progress = 0
            val body = UploadRequestBody(file, "image/jpeg", this)

            MyAPI().uploadImage(
                MultipartBody.Part.createFormData("file", file.name, body),
//                RequestBody.create(MediaType.parse("multipart/form-data"), "Image to transform")
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    binding.progressBar.progress = 100
                    binding.root.snackbar("Image uploaded successfully")

                    val isFileDownloaded = downloadImage(response.body()!!)
                    if (!isFileDownloaded) {
                        showErrorSnackBar(R.string.download_error)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showErrorSnackBar(R.string.upload_error)
                    Log.e("API", t.message.toString())
                }
            })
        }
    }

    private fun downloadImage(body: ResponseBody): Boolean {
        try {
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream =
                    FileOutputStream(getExternalFilesDir(null).toString() + File.separator + "image.jpg")
                var c = 0
                while ((inputStream.read().also { c = it }) != -1) {
                    outputStream.write(c)
                }
            } catch (e: IOException) {
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }

//            val bitmap = BitmapFactory.decodeFile(getExternalFilesDir(null).toString() + File.separator + "image.jpg")
            Glide.with(this)
                .load(getExternalFilesDir(null).toString() + File.separator + "image.jpg")
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.ivImage)
            return true
        } catch (e: IOException) {
            return false
        }
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    private val galleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data!!.data != null) {
                    mSelectedImageFileUri = result.data!!.data
                    Glide.with(this)
                        .load(mSelectedImageFileUri)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(binding.ivImage)
                }
            }
        }

    private fun chooseImageFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    galleryImageResultLauncher.launch(galleryIntent)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }

        }).onSameThread().check()
    }

    private suspend fun nstApiCall() {
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://192.168.0.77:8000/nst",
            formData = formData {
                append(
                    "file",
                    File(mSelectedImageFileUri.toString()).readBytes(),
                    Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                    })
            }
        )
        val responseBody: ByteArray = response.receive()
        Glide.with(this)
            .asBitmap()
            .load(responseBody)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.ivImage)
    }

    private suspend fun testApiCall() {
        val response: HttpResponse = client.get("http://20.113.133.133:80/")
        val stringBody: String = response.receive()
        Log.i("API", stringBody)
    }

    override fun onProgressUpdate(percentage: Int) {
        binding.progressBar.progress = percentage
    }

}
