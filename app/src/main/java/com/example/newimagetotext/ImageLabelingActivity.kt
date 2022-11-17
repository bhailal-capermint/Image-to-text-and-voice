package com.example.newimagetotext

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.ImageFormat.NV21
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newimagetotext.facedetector.FaceDetector
import com.example.newimagetotext.facedetector.Frame
import com.example.newimagetotext.facedetector.LensFacing
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.otaliastudios.cameraview.controls.Facing
import kotlinx.android.synthetic.main.activity_face_detection.*
import kotlinx.android.synthetic.main.activity_image_labeling.*
import kotlinx.android.synthetic.main.activity_image_labeling.toggleCameraButton
import kotlinx.android.synthetic.main.activity_image_labeling.viewfinder
import java.io.ByteArrayOutputStream

class ImageLabelingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_labeling)
        val lensFacing = savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.BACK
        setupCamera(lensFacing)
    }

    override fun onResume() {
        super.onResume()
        viewfinder.open()
    }

    override fun onPause() {
        super.onPause()
        viewfinder.close()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(KEY_LENS_FACING, viewfinder.facing)
        super.onSaveInstanceState(outState)
    }

    private fun setupCamera(lensFacing: Facing) {
        viewfinder.facing = lensFacing
        viewfinder.addFrameProcessor {frame ->
            if (frame.format == ImageFormat.NV21
                && frame.dataClass == ByteArray::class.java) {
                val data = frame.getData<ByteArray>()
                val yuvImage = YuvImage(data,
                    frame.format,
                    frame.size.width,
                    frame.size.height,
                    null)
                val jpegStream = ByteArrayOutputStream()
                yuvImage.compressToJpeg(
                    Rect(0, 0,
                    frame.size.width,
                    frame.size.height), 100, jpegStream)
                val jpegByteArray = jpegStream.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(jpegByteArray,
                    0, jpegByteArray.size)
                bitmap.toString()
                labelImages(InputImage.fromBitmap(bitmap,0))

            }


//            labeler.process(it)
//                Frame(
//                    data = it.getData(),
//                    rotation = it.rotation,
//                    size = Size(it.size.width, it.size.height),
//                    format = it.format,
//                    lensFacing = if (viewfinder.facing == Facing.BACK) LensFacing.BACK else LensFacing.FRONT
//                )
//            )
        }

        toggleCameraButton.setOnClickListener {
            viewfinder.toggleFacing()
        }
    }

    private fun labelImages(image: InputImage) {
        val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.8f)
                .build()

        val labeler = ImageLabeling.getClient(options)

        // [START run_detector]
        val result = labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    // [START_EXCLUDE]
                    // [START get_labels]
                    for (label in labels) {
                        val text = label.text
                        val confidence = label.confidence
                        Toast.makeText(this,text.toString() + confidence.toString(),Toast.LENGTH_SHORT).show()
                    }
                    // [END get_labels]
                    // [END_EXCLUDE]
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
        // [END run_detector]
    }

    private fun configureAndRunImageLabeler(image: InputImage) {
        // [START on_device_image_labeler]
        // To use default options:
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        // Or, to set the minimum confidence required:
        // val options = ImageLabelerOptions.Builder()
        //     .setConfidenceThreshold(0.7f)
        //     .build()
        // val labeler = ImageLabeling.getClient(options)

        // [END on_device_image_labeler]

        // Process image with custom onSuccess() example
        // [START process_image]
        labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    // ...
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
        // [END process_image]

        // Process image with example onSuccess()
        labeler.process(image)
                .addOnSuccessListener { labels ->
                    // [START get_image_label_info]
                    for (label in labels) {
                        val text = label.text
                        val confidence = label.confidence
                        val index = label.index
                    }
                    // [END get_image_label_info]
                }
    }

    companion object {
        private const val TAG = "ImageLabeling"
        private const val KEY_LENS_FACING = "key-lens-facing"
    }
}