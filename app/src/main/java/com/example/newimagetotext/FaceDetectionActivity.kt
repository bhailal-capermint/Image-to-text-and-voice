package com.example.newimagetotext

import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.example.newimagetotext.facedetector.FaceDetector
import com.example.newimagetotext.facedetector.Frame
import com.example.newimagetotext.facedetector.LensFacing
import com.otaliastudios.cameraview.controls.Facing
import kotlinx.android.synthetic.main.activity_face_detection.*

class FaceDetectionActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection)

        val lensFacing =
            savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.BACK
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

    override fun onDestroy() {
        super.onDestroy()
        viewfinder.destroy()
    }

    private fun setupCamera(lensFacing: Facing) {
        val faceDetector = FaceDetector(faceBoundsOverlay)
        viewfinder.facing = lensFacing
        viewfinder.addFrameProcessor {
            faceDetector.process(
                Frame(
                    data = it.getData(),
                    rotation = it.rotation,
                    size = Size(it.size.width, it.size.height),
                    format = it.format,
                    lensFacing = if (viewfinder.facing == Facing.BACK) LensFacing.BACK else LensFacing.FRONT
                )
            )
        }

        toggleCameraButton.setOnClickListener {
            viewfinder.toggleFacing()
        }
    }

    companion object {
        private const val TAG = "FaceDetection"
        private const val KEY_LENS_FACING = "key-lens-facing"
    }
}