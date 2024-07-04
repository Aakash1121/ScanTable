package com.aakash.scantableapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.aakash.scantableapp.api.ApiService
import com.aakash.scantableapp.api.RetrofitClient
import com.aakash.scantableapp.databinding.ActivityMainBinding
import com.aakash.scantableapp.model.ImageModel
import com.aakash.scantableapp.model.TableModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.mayank.simplecropview.callback.CropCallback
import com.mayank.simplecropview.callback.LoadCallback
import com.mayank.simplecropview.callback.SaveCallback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private var imagePath = ""
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        handleListener()
    }

    private fun handleListener() {
        mBinding.btnScanImage.setOnClickListener {
            imagePicker()
        }

        mBinding.btnGenerateTable.setOnClickListener {
            mBinding.progressBar.visibility = View.VISIBLE

            mBinding.simpleCropView.crop(imageUri).execute(mCropCallback)


        }
    }

    private fun imagePicker() {
        ImagePicker.with(this)
            .crop()                    //Crop image(Optional), Check Customization for more option
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }


    private fun getFileNameFromPath(filePath: String): String {
        val separatorIndex = filePath.lastIndexOf('/')

        if (separatorIndex != -1 && separatorIndex < filePath.length - 1) {
            val fileName = filePath.substring(separatorIndex + 1)
            return fileName
        }

        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                imageUri = data?.data!!

                imagePath = imageUri.path!!

                Log.i("IMAGE_PATH", imagePath)

                mBinding.simpleCropView.load(imageUri).execute(mLoadCallback);

            }

            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val mLoadCallback: LoadCallback = object : LoadCallback {

        override fun onSuccess() {


            mBinding.simpleCropView.setInitialFrameScale(0.5f);

            mBinding.btnGenerateTable.visibility = View.VISIBLE

        }

        override fun onError(e: Throwable) {}
    }

    private val mCropCallback: CropCallback = object : CropCallback {
        override fun onSuccess(cropped: Bitmap) {
            mBinding.simpleCropView.save(cropped)
                .compressFormat(Bitmap.CompressFormat.JPEG)
                .execute(createSaveUri(), mSaveCallback)
        }

        override fun onError(e: Throwable) {}
    }

    private val mSaveCallback: SaveCallback = object : SaveCallback {
        override fun onSuccess(outputUri: Uri) {

            try {
                val service = RetrofitClient.getRetrofitClient().create(ApiService::class.java)

                // Create request body with file
                val imageFile = File(imageUri.path!!)
                // Create request body with file
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                val call = service.uploadFile(body)
                call.enqueue(object : Callback<TableModel> {
                    override fun onResponse(
                        call: Call<TableModel>,
                        response: Response<TableModel>
                    ) {
                        mBinding.progressBar.visibility = View.GONE

                        if (response.isSuccessful) {
                            // Handle successful response
                            val tableModel = response.body()
                            val arrayList = ArrayList<ArrayList<String>>()
                            if (tableModel != null) {
                                arrayList.addAll(tableModel.tableTextList)
                            }
                            Log.i("UPLOAD_DATA", "SUCCESSFULL: $arrayList")
                            val intentData = Gson().toJson(tableModel)
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    TableViewActivity::class.java
                                ).putExtra("INTENT_DATA", intentData)
                            )
                        } else {
                            // Handle unsuccessful response
                        }
                    }

                    override fun onFailure(call: Call<TableModel>, t: Throwable) {
                        mBinding.progressBar.visibility = View.GONE
                        Log.i("UPLOAD_DATA", "FAILED")

                    }
                })

            } catch (e: IOException) {
                mBinding.progressBar.visibility = View.GONE
                e.printStackTrace()

            }
        }

        override fun onError(e: Throwable) {}
    }

    fun createSaveUri(): Uri {
        return imageUri
    }
}
