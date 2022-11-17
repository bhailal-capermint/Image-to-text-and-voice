package com.example.newimagetotext

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.otaliastudios.cameraview.controls.Facing
import kotlinx.android.synthetic.main.activity_barcode_scanner.*
import java.io.ByteArrayOutputStream

class BarcodeScanningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)
        val lensFacing = savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.BACK
        setupCamera(lensFacing)

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
                scanBarcodes(InputImage.fromBitmap(bitmap,0))

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

    private fun scanBarcodes(image: InputImage) {
        // [START set_detector_options]
        val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC)
                .build()
        // [END set_detector_options]

        // [START get_detector]
        val scanner = BarcodeScanning.getClient()
        // Or, to specify the formats to recognize:
        // val scanner = BarcodeScanning.getClient(options)
        // [END get_detector]

        // [START run_detector]
        val result = scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    // [START_EXCLUDE]
                    // [START get_barcodes]
                    for (barcode in barcodes) {
                        val bounds = barcode.boundingBox
                        val corners = barcode.cornerPoints

                        val rawValue = barcode.rawValue

                        val valueType = barcode.valueType
                        // See API reference for complete list of supported types
                        when (valueType) {
                            Barcode.TYPE_WIFI -> {
                                val ssid = barcode.wifi!!.ssid
                                val password = barcode.wifi!!.password
                                val type = barcode.wifi!!.encryptionType
                            }
                            Barcode.TYPE_URL -> {
                                val title = barcode.url!!.title
                                val url = barcode.url!!.url
                            }
                        }
                    }
                    // [END get_barcodes]
                    // [END_EXCLUDE]
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }
        // [END run_detector]
    }

    companion object {
        private const val TAG = "BarCodeScanning"
        private const val KEY_LENS_FACING = "key-lens-facing"
    }
}