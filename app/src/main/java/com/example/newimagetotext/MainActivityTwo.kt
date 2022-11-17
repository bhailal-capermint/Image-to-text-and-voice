package com.example.newimagetotext

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.permissionx.guolindev.PermissionX
import java.io.IOException

class MainActivityTwo: AppCompatActivity() {
    var inputImageBtn: MaterialButton? = null
    var getTextBtn:MaterialButton? = null
    var imageView: ShapeableImageView? = null
    var imageText: EditText? = null
    var imageUri: Uri? = null
    val CAMERA_REQUEST_CODE = 100
    val STORAGE_REQUEST_CODE = 101
//    private val cameraPermissions: Array<String>
//    private val storagePermissions: Array<String>

    var progressDialog: ProgressDialog? = null

    var textRecognizer: TextRecognizer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputImageBtn = findViewById(R.id.inputImageBtn)
        getTextBtn = findViewById(R.id.getTextBtn)
        imageView = findViewById(R.id.imageView)
        imageText = findViewById(R.id.imageText)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        inputImageBtn!!.setOnClickListener {
            checkForStoragePermission()
        }
        getTextBtn!!.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(this@MainActivityTwo, "Pick image first", Toast.LENGTH_SHORT).show()
            } else {
                getImageFromText()
            }
        }
    }

    private fun getImageFromText() {
        progressDialog!!.setMessage("Getting image.....")
        progressDialog!!.show()
        try {
            val inputImage = InputImage.fromFilePath(this, imageUri!!)
            progressDialog!!.setMessage("Getting text....")
            val textTaskResult = textRecognizer!!.process(inputImage).addOnSuccessListener { text ->
                progressDialog!!.dismiss()
                val recognizedText = text.text
                imageText!!.setText(recognizedText)
            }.addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(this@MainActivityTwo, "" + e.message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            progressDialog!!.dismiss()
            Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForStoragePermission() {

        PermissionX.init(this@MainActivityTwo)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        .request { allGranted, grantedList, deniedList ->
            if (allGranted) {
                ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start()
//                Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
            } else {
//                Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
            }
        }
    }
    fun goToTranslateBtn(view: View?) {
        val intent = Intent(this@MainActivityTwo, TranslateActivity::class.java)
        startActivity(intent)
    }

    fun goToSpeakerBtn(view: View?) {
        val intent = Intent(this@MainActivityTwo, SpeakerActivity::class.java)
        startActivity(intent)
    }

    fun goToFaceDetectBtn(view:View?){
        val intent = Intent(this@MainActivityTwo, FaceDetectionActivity::class.java)
        startActivity(intent)
    }

    fun goToImageLabeling(view:View?){
        val intent = Intent(this@MainActivityTwo, ImageLabelingActivity::class.java)
        startActivity(intent)
    }

    fun goToBarcodeScanning(view: View?){
        val intent = Intent(this@MainActivityTwo, BarcodeScanningActivity::class.java)
        startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            imageUri = data?.data
            imageView?.setImageURI(data?.data)
            getTextBtn?.visibility = View.VISIBLE
//            showSelectedFile()
//            val file: File = ImagePicker.getFile(data)!!
//            val filePath:String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
    }


}