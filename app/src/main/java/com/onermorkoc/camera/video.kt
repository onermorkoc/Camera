package com.onermorkoc.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class video : Fragment() {

    private lateinit var outputDirectory: File
    private lateinit var videoCapture: VideoCapture
    private lateinit var cameraSelector : CameraSelector
    private var flashSayi = 0
    private var kameraSayi = 0
    private var videoSayi = 0

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoCapture = VideoCapture.Builder().build()
        outputDirectory = getOutputDirectory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraBaslat()
        videoCek()
        kameraDegistir()
        flashAcKapat()
    }

    fun cameraBaslat() {

        val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener(
            {
                if (kameraSayi % 2 == 0){
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
                    val abc = cameraProvider.bindToLifecycle(this, cameraSelector, videoCapture, preView)

                    if (flashSayi % 2 == 0) {
                        requireActivity().flash_btn_id.setImageResource(R.drawable.flash_off)
                    } else {
                        abc.cameraControl.enableTorch(true)
                        requireActivity().flash_btn_id.setImageResource(R.drawable.flash_on)
                    }

                } catch (e: Exception) {

                }
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.apply {
            mkdirs()
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    @SuppressLint("RestrictedApi")
    fun videoCek() {

        requireActivity().takephoto_btn_id.setOnClickListener {

            if (videoSayi % 2 == 0) {

                requireActivity().changeCamera_btn_id.visibility = View.INVISIBLE
                requireActivity().flash_btn_id.visibility = View.INVISIBLE

                val videoFile = File(outputDirectory, SimpleDateFormat("yy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".mp4")
                val outputOptions = VideoCapture.OutputFileOptions.Builder(videoFile).build()

                if (checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                    videoCapture.startRecording(outputOptions, Executors.newSingleThreadExecutor(), object : VideoCapture.OnVideoSavedCallback {
                        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                            //kaydedildi
                        }

                        override fun onError(error: Int, message: String, cause: Throwable?) {
                            //hata
                        }
                    })
                }

                requireActivity().takephoto_btn_id.setImageResource(R.drawable.stop)
            }else {
                videoCapture.stopRecording()
                requireActivity().takephoto_btn_id.setImageResource(R.drawable.take_camera)
                requireActivity().changeCamera_btn_id.visibility = View.VISIBLE
                requireActivity().flash_btn_id.visibility = View.VISIBLE
            }
            videoSayi += 1
        }
    }

    fun flashAcKapat(){
        requireActivity().flash_btn_id.setOnClickListener {
            flashSayi += 1
            cameraBaslat()
        }
    }

    fun kameraDegistir(){
        requireActivity().changeCamera_btn_id.setOnClickListener {
            kameraSayi += 1
            cameraBaslat()
        }
    }

    override fun onResume() {
        super.onResume()
        cameraBaslat()
        videoCek()
        kameraDegistir()
        flashAcKapat()
    }
}