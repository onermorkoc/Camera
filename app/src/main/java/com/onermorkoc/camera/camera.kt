package com.onermorkoc.camera

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class camera : Fragment() {

    private var imageCapture: ImageCapture?=null
    private lateinit var outputDirectory : File
    private var flashSayi : Int?=null
    private var kameraSayi : Int?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.RECORD_AUDIO), 1) //izin yoksa izin al
        else
            cameraBaslat()

        outputDirectory = getOutputDirectory()

        requireActivity().takephoto_btn_id.setOnClickListener {
            fotocek()
        }

        flashOnOff_btn()
        cameraFrontBack_btn()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            cameraBaslat() //izin verildigi an napsın
            else{
                //izini redderse napsın
            }
        }
    }

    fun getOutputDirectory(): File {
        val mediaDir =requireActivity().externalMediaDirs.firstOrNull()?.apply {
            mkdirs()
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    fun fotocek(){

        val imageCapture = imageCapture ?: return
        val photofile = File(outputDirectory,
            SimpleDateFormat("yy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg")
        val outputOption =ImageCapture.OutputFileOptions.Builder(photofile).build()

        imageCapture.takePicture(outputOption, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(requireContext(),"Kaydedildi", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: ImageCaptureException) {

                }
            }
        )
    }

    fun cameraBaslat() {
        val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener(
            {
            var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val kameraSayi = kameraSayi!! % 2
            if (kameraSayi == 1) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }
            val cameraProvider: ProcessCameraProvider = processCameraProvider.get()
                imageCapture = ImageCapture.Builder().build()
            val preView = Preview.Builder().build().also {
                if (cameraView!=null)
                    it.setSurfaceProvider(cameraView.surfaceProvider)
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

}