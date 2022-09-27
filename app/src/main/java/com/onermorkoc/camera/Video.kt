package com.onermorkoc.camera

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.ContentValues
import android.media.MediaActionSound
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class Video : Fragment() {

    private lateinit var videoCapture: VideoCapture
    private lateinit var cameraSelector : CameraSelector
    private var flashCount = 0
    private var cameraCount = 0
    private var videoCount = 0
    private val handler = Handler(Looper.myLooper()!!)
    private var runnable: Runnable? = null
    private lateinit var permissionsList: Array<String>

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoCapture = VideoCapture.Builder().build()
        permissionsList = arrayOf(CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Camera().checkPermissions(1, requireContext(), permissionsList)) {
            startCamera()
        }

        takeVideo()
        changeCamera()
        openCloseFlash()
    }

    private fun startCamera() {

        val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())

        processCameraProvider.addListener(
            {
                if (cameraCount % 2 == 0){
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                }else{
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                }

                val cameraProvider = processCameraProvider.get()

                val preView = Preview.Builder().build().also {
                    if (videoView != null)
                        it.setSurfaceProvider(videoView.surfaceProvider)
                }

                try {

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(this, cameraSelector, videoCapture, preView)

                    if (flashCount % 2 == 0) {
                        requireActivity().flash_btn_id.setImageResource(R.drawable.flash_off)
                    } else {
                        camera.cameraControl.enableTorch(true)
                        requireActivity().flash_btn_id.setImageResource(R.drawable.flash_on)
                    }

                } catch (e: Exception) {

                }
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    @SuppressLint("RestrictedApi", "MissingPermission")
    private fun takeVideo() {

        requireActivity().takephoto_btn_id.setOnClickListener {

            if (Camera().checkPermissions(4, requireContext(), permissionsList)) {

                if (videoCount % 2 == 0) {

                    requireActivity().changeCamera_btn_id.visibility = View.INVISIBLE
                    requireActivity().flash_btn_id.visibility = View.INVISIBLE
                    MediaActionSound().play(MediaActionSound.START_VIDEO_RECORDING)
                    timeVideo()

                    val name = SimpleDateFormat("yy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis())

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/CameraX/Videos")
                        }
                    }

                    val outputOptions = VideoCapture.OutputFileOptions.Builder(requireContext().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues).build()

                    videoCapture.startRecording(outputOptions, Executors.newSingleThreadExecutor(), object : VideoCapture.OnVideoSavedCallback {
                        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                            //kaydedildi
                        }

                        override fun onError(error: Int, message: String, cause: Throwable?) {
                            //hata
                        }
                    })

                    requireActivity().takephoto_btn_id.setImageResource(R.drawable.stop)
                }else {
                    videoCapture.stopRecording()
                    handler.removeCallbacks(runnable!!)
                    requireActivity().videoTime_id.text = ""
                    MediaActionSound().play(MediaActionSound.STOP_VIDEO_RECORDING)
                    requireActivity().takephoto_btn_id.setImageResource(R.drawable.take_camera)
                    requireActivity().changeCamera_btn_id.visibility = View.VISIBLE
                    requireActivity().flash_btn_id.visibility = View.VISIBLE
                }
                videoCount += 1
            }else{
                Toast.makeText(requireContext(), requireActivity().getString(R.string.permission_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCloseFlash(){
        requireActivity().flash_btn_id.setOnClickListener {
            if (Camera().checkPermissions(1, requireContext(), permissionsList)) {
                flashCount += 1
                startCamera()
            }
        }
    }

    private fun changeCamera(){
        requireActivity().changeCamera_btn_id.setOnClickListener {
            if (Camera().checkPermissions(1, requireContext(), permissionsList)) {
                cameraCount += 1
                startCamera()
            }
        }
    }

    private fun timeVideo(){
        var count = 0
        runnable = Runnable {
            count++
            var string = ""
            val hours = count / 3600
            val minutes = (count % 3600) / 60
            val seconds = count % 60
            if (hours > 0){
                if (hours < 10)
                    string = "0$hours:"
                else
                    string = "$hours:"
            }
            if (minutes > 0){
                if (minutes < 10)
                    string += "0$minutes:"
                else
                    string += "$minutes:"
            }
            if (seconds < 10)
                string += "0$seconds"
            else
                string +="$seconds"
            requireActivity().videoTime_id.text = string
            handler.postDelayed(runnable!!, 1000)
        }
        handler.post(runnable!!)
    }

    override fun onResume() {
        super.onResume()
        startCamera()
        takeVideo()
        changeCamera()
        openCloseFlash()
    }
}