package com.onermorkoc.camera

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaActionSound
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.camera.*
import java.text.SimpleDateFormat
import java.util.*

class Camera : Fragment() {

    private lateinit var cameraSelector : CameraSelector
    private lateinit var permissionsList: Array<String>
    private var imageCapture: ImageCapture?=null
    private var flashCount = 0
    private var cameraCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageCapture = ImageCapture.Builder().build()
        permissionsList = arrayOf(CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkPermissions(4, requireContext(), permissionsList)){
            startCamera()
        }else{
            requestPermissions(permissionsList, 1)
        }

        takePhoto()
        changeCamera()
        openCloseFlash()
    }

    fun checkPermissions(j: Int, requireContext: Context, permissionsList: Array<String>): Boolean{
        var i = -1
        while (++i < j){
            if (checkSelfPermission(requireContext, permissionsList[i]) == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) //izin verildigi an napsın
                startCamera()
            else{
                //izini redderse napsın
            }
        }
    }

    private fun takePhoto(){

        requireActivity().takephoto_btn_id.setOnClickListener {

            if (checkPermissions(3, requireContext(), permissionsList)){

                requireActivity().progressBar.visibility = View.VISIBLE
                val name = SimpleDateFormat("yy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis())

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/CameraX/Images")
                    }
                }

                val outputOptions = ImageCapture.OutputFileOptions.Builder(requireContext().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

                imageCapture!!.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                    object : ImageCapture.OnImageSavedCallback{
                        @SuppressLint("WrongConstant")
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            requireActivity().progressBar.visibility = View.GONE
                            MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
                        }
                        override fun onError(exception: ImageCaptureException) {

                        }
                    }
                )

            }else{
                Toast.makeText(requireContext(), requireActivity().getString(R.string.permission_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {

        val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener({

            if (cameraCount % 2 == 0){
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
                val camera = cameraProvider.bindToLifecycle(this,cameraSelector,imageCapture,preView)

                if (flashCount % 2 == 0) {
                    requireActivity().flash_btn_id.setImageResource(R.drawable.flash_off)
                } else {
                    camera.cameraControl.enableTorch(true)
                    requireActivity().flash_btn_id.setImageResource(R.drawable.flash_on)
                }

            }catch (e : Exception){

            }
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun openCloseFlash(){
        requireActivity().flash_btn_id.setOnClickListener {
            if (checkPermissions(1, requireContext(), permissionsList)){
                flashCount += 1
                startCamera()
            }
        }
    }

    private fun changeCamera(){
        requireActivity().changeCamera_btn_id.setOnClickListener {
            if (checkPermissions(1,requireContext(), permissionsList)){
                cameraCount += 1
                startCamera()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCamera()
        takePhoto()
        changeCamera()
        openCloseFlash()
    }
}