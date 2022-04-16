package com.onermorkoc.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
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

    private var imageCapture: ImageCapture?=null
    private lateinit var outputDirectory : File
    private var flashSayi : Int?=null
    private var kameraSayi : Int?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraBaslat()
        flashOnOff_btn()
        cameraFrontBack_btn()
        outputDirectory = getOutputDirectory()
        requireActivity().takephoto_btn_id.setOnClickListener {
            videoCek()
        }
        requireActivity().button.setOnClickListener {
            stopVideo(time = 10)
        }

    }

    fun cameraBaslat() {
        val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener(
            {
                var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val kameraSayi = kameraSayi!! % 2
                if (kameraSayi == 1)
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                val cameraProvider: ProcessCameraProvider = processCameraProvider.get()
                imageCapture = ImageCapture.Builder().build()
                val preView = Preview.Builder().build().also {
                    if (videoView!=null)
                        it.setSurfaceProvider(videoView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    val abc = cameraProvider.bindToLifecycle(this,cameraSelector,imageCapture,preView)
                    val flashSayi= flashSayi!! % 2
                    if (flashSayi == 1) {
                        abc.cameraControl.enableTorch(true)
                        requireActivity().flash_btn_id.setImageResource(R.drawable.flash_on)
                    }else {
                        requireActivity().flash_btn_id.setImageResource(R.drawable.flash_off)
                    }

                }catch (e : Exception){

                }
            }, ContextCompat.getMainExecutor(requireContext()))
    }

    fun getOutputDirectory(): File {
        val mediaDir =requireActivity().externalMediaDirs.firstOrNull()?.apply {
            mkdirs()
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    fun flashOnOff_btn(){
        flashSayi = 2
        requireActivity().flash_btn_id.setOnClickListener {
            flashSayi = flashSayi!! + 1
            cameraBaslat()
        }
    }

    fun cameraFrontBack_btn(){
        kameraSayi = 2
        requireActivity().changeCamera_btn_id.setOnClickListener {
            kameraSayi = kameraSayi!! + 1
            cameraBaslat()
        }
    }

    override fun onResume() {
        super.onResume()
        cameraBaslat()
        flashOnOff_btn()
        cameraFrontBack_btn()
    }

    @SuppressLint("RestrictedApi")
    fun videoCek(){
        val videoCapture = VideoCapture.Builder().build()
        val videoFile = File(outputDirectory,
            SimpleDateFormat("yy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".mp4")
        val outputOptions = VideoCapture.OutputFileOptions.Builder(videoFile).build()

        if (checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        videoCapture?.startRecording(outputOptions, Executors.newSingleThreadExecutor(),object : VideoCapture.OnVideoSavedCallback {
            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                Toast.makeText(requireContext(),"Kaydedildi",Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: Int, message: String, cause: Throwable?) {

            }
        })
    }

    @SuppressLint("RestrictedApi")
    fun stopVideo(time: Long) {
        val videoCapture = VideoCapture.Builder().build()
        videoCapture.stopRecording()
    }
}