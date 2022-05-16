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
    private lateinit var cameraSelector : CameraSelector
    private var flashSayi = 0
    private var kameraSayi = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputDirectory = getOutputDirectory()
        imageCapture = ImageCapture.Builder().build()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1) //izin yoksa izin al
        }else{
            cameraBaslat()
        }
        fotocek()
        kameraDegistir()
        flashAcKapat()
    }

    @Deprecated("Deprecated in Java")
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

        requireActivity().takephoto_btn_id.setOnClickListener {

            val photofile = File(outputDirectory, SimpleDateFormat("yy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg")
            val outputOption =ImageCapture.OutputFileOptions.Builder(photofile).build()

            imageCapture!!.takePicture(outputOption, ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback{
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Toast.makeText(requireContext(),"Kaydedildi", Toast.LENGTH_SHORT).show()
                    }
                    override fun onError(exception: ImageCaptureException) {

                    }
                }
            )
        }
    }

    fun cameraBaslat() {

        val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener({

            if (kameraSayi % 2 == 0){
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            }else{
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }

            val cameraProvider: ProcessCameraProvider = processCameraProvider.get()

            val preView = Preview.Builder().build().also {
                if (cameraView!=null)
                    it.setSurfaceProvider(cameraView.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                val abc = cameraProvider.bindToLifecycle(this,cameraSelector,imageCapture,preView)

                if (flashSayi % 2 == 0) {
                    requireActivity().flash_btn_id.setImageResource(R.drawable.flash_off)
                } else {
                    abc.cameraControl.enableTorch(true)
                    requireActivity().flash_btn_id.setImageResource(R.drawable.flash_on)
                }

            }catch (e : Exception){

            }
            }, ContextCompat.getMainExecutor(requireContext())
        )
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
        fotocek()
        kameraDegistir()
        flashAcKapat()
    }
}