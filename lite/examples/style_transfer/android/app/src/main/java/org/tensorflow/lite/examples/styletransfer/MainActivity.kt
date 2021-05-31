/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.styletransfer

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.tensorflow.lite.examples.styletransfer.camera.CameraFragment
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.Executors


// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

private const val TAG = "MainActivity"

class MainActivity :

  AppCompatActivity(),
  StyleFragment.OnListFragmentInteractionListener,
  CameraFragment.OnCaptureFinished{

  private var isRunningModel = false
  private val stylesFragment: StyleFragment = StyleFragment()
  private var selectedStyle: String = ""


  private lateinit var cameraFragment: CameraFragment
  private lateinit var viewModel: MLExecutionViewModel
  private lateinit var viewFinder: FrameLayout
  private lateinit var resultImageView: ImageView
  private lateinit var originalImageView: ImageView
  private lateinit var styleImageView: ImageView
  private lateinit var rerunButton: Button
  private lateinit var button1: Button
  private lateinit var drawable: Drawable
  private lateinit var bitmap: Bitmap
  private lateinit var captureButton: ImageButton
  private lateinit var progressBar: ProgressBar
  private lateinit var horizontalScrollView: HorizontalScrollView
  private lateinit var file : File
  private lateinit var modelResult: ModelExecutionResult

  private var lastSavedFile = ""
  private var useGPU = false
  private lateinit var styleTransferModelExecutor: StyleTransferModelExecutor
  private val inferenceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  private val mainScope = MainScope()
  private lateinit var auth: FirebaseAuth
  private var lensFacing = CameraCharacteristics.LENS_FACING_FRONT

  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val toolbar: Toolbar = findViewById(R.id.toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    val path = Environment.getExternalStorageDirectory().toString()
    viewFinder = findViewById(R.id.view_finder)
    resultImageView = findViewById(R.id.result_imageview)
    originalImageView = findViewById(R.id.original_imageview)
    styleImageView = findViewById(R.id.style_imageview)
    captureButton = findViewById(R.id.capture_button)
    button1 = findViewById(R.id.button1)
    progressBar = findViewById(R.id.progress_circular)
    horizontalScrollView = findViewById(R.id.horizontal_scroll_view)

    val useGpuSwitch: Switch = findViewById(R.id.switch_use_gpu)

    // Request camera permissions
    if (allPermissionsGranted()) {
      addCameraFragment()
    } else {
      ActivityCompat.requestPermissions(
        this,
        REQUIRED_PERMISSIONS,
        REQUEST_CODE_PERMISSIONS
      )
    }

    viewModel = AndroidViewModelFactory(application).create(MLExecutionViewModel::class.java)

    viewModel.styledBitmap.observe(
      this,
      Observer { resultImage ->
        if (resultImage != null) {
          updateUIWithResults(resultImage)
        }
      }
    )

    mainScope.async(inferenceThread) {
      styleTransferModelExecutor = StyleTransferModelExecutor(this@MainActivity, useGPU)
      Log.d(TAG, "Executor created")
    }

    useGpuSwitch.setOnCheckedChangeListener { _, isChecked ->
      useGPU = isChecked
      // Disable control buttons to avoid running model before initialization
      enableControls(false)

      // Reinitialize TF Lite models with new GPU setting
      mainScope.async(inferenceThread) {
        styleTransferModelExecutor.close()
        styleTransferModelExecutor = StyleTransferModelExecutor(this@MainActivity, useGPU)

        // Re-enable control buttons
        runOnUiThread { enableControls(true) }
      }
    }

    rerunButton = findViewById(R.id.rerun_button)
    rerunButton.setOnClickListener {
      startRunningModel()
    }
    button1.setOnClickListener {
      showDialog()
    }

    styleImageView.setOnClickListener {
      if (!isRunningModel) {
        stylesFragment.show(supportFragmentManager, "StylesFragment")
      }
    }

    progressBar.visibility = View.INVISIBLE
    lastSavedFile = getLastTakenPicture()
    setImageView(originalImageView, lastSavedFile)

    animateCameraButton()
    setupControls()
    enableControls(true)

    Log.d(TAG, "finished onCreate!!")
  }

  private fun animateCameraButton() {
    val animation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)
    animation.interpolator = BounceInterpolator()
    captureButton.animation = animation
    captureButton.animation.start()
  }
  private fun showDialog() {
    val input = EditText(this@MainActivity)
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Share")
    builder.setMessage("Please input image name")
    builder.setPositiveButton(
      "Share"
    ) { dialog, which ->
      Log.d(TAG,input.text.toString())
      saveToInternalStorage(modelResult.styledImage,input.text.toString());
    }
    val lp = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.MATCH_PARENT
    )
    input.layoutParams = lp
    builder.setView(input) // uncomment this line
    val dialog = builder.create()
    dialog.show()
  }
  private fun saveToInternalStorage(bitmapImage: Bitmap,picturename:String): String? {
    val cw = ContextWrapper(applicationContext)
    // path to /data/data/yourapp/app_data/imageDir
    val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
    // Create imageDir
    val mypath = File(directory,"/"+picturename+".jpg")
    var fos: FileOutputStream? = null
    try {
      fos = FileOutputStream(mypath)
      // Use the compress method on the BitMap object to write image to the OutputStream
      bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      Log.d(TAG,mypath.absolutePath)
      val storage: FirebaseStorage = FirebaseStorage.getInstance()
      val storageRef: StorageReference = storage.getReference()
      val mountainsRef: StorageReference = storageRef.child("mountains.jpg")
      val mountainImagesRef: StorageReference = storageRef.child("images/mountains.jpg")
      mountainsRef.name == mountainImagesRef.name
      mountainsRef.path == mountainImagesRef.path
      val file1 = Uri.fromFile(File(mypath.absolutePath))
      val riversRef: StorageReference = storageRef.child("images/" + file1.lastPathSegment)
      val uploadTask = riversRef.putFile(file1)
      val db = FirebaseFirestore.getInstance()
      auth = FirebaseAuth.getInstance();
      val user = FirebaseAuth.getInstance().currentUser
      val image: MutableMap<String, Any> = HashMap()
      image["name"] = picturename
      if (user != null) {
        image["Uploaded by"] =  user.uid
      }
      image["url"] = "https://firebasestorage.googleapis.com/v0/b/cem-68a11.appspot.com/o/images%2F"+picturename+".jpg?alt=media"

      db.collection("Images").document(picturename)[image] = SetOptions.merge()
    }
    return directory.absolutePath
  }
  private fun setImageView(imageView: ImageView, image: Bitmap) {
    Glide.with(baseContext)
      .load(image)
      .override(512, 512)
      .fitCenter()
      .into(imageView)
  }

  private fun setImageView(imageView: ImageView, imagePath: String) {
    Glide.with(baseContext)
      .asBitmap()
      .load(imagePath)
      .override(512, 512)
      .apply(RequestOptions().transform(CropTop()))
      .into(imageView)
  }

  private fun updateUIWithResults(modelExecutionResult: ModelExecutionResult) {
    progressBar.visibility = View.INVISIBLE
    resultImageView.visibility = View.VISIBLE
    setImageView(resultImageView, modelExecutionResult.styledImage)
    modelResult = modelExecutionResult

    val logText: TextView = findViewById(R.id.log_view)
    logText.text = modelExecutionResult.executionLog
    enableControls(true)
    horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
  }

  private fun enableControls(enable: Boolean) {
    isRunningModel = !enable
    rerunButton.isEnabled = enable
    captureButton.isEnabled = enable
  }

  private fun setupControls() {
    captureButton.setOnClickListener {
      it.clearAnimation()
      cameraFragment.takePicture()
    }

    findViewById<ImageButton>(R.id.toggle_button).setOnClickListener {
      lensFacing = if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
        CameraCharacteristics.LENS_FACING_FRONT
      } else {
        CameraCharacteristics.LENS_FACING_BACK
      }
      cameraFragment.setFacingCamera(lensFacing)
      addCameraFragment()
    }
  }

  private fun addCameraFragment() {
    cameraFragment = CameraFragment.newInstance()
    cameraFragment.setFacingCamera(lensFacing)
    supportFragmentManager.popBackStack()
    supportFragmentManager.beginTransaction()
      .replace(R.id.view_finder, cameraFragment)
      .commit()
  }

  /**
   * Process result from permission request dialog box, has the request
   * been granted? If yes, start Camera. Otherwise display a toast
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE_PERMISSIONS) {
      if (allPermissionsGranted()) {
        addCameraFragment()
        viewFinder.post { setupControls() }
      } else {
        Toast.makeText(
          this,
          "Permissions not granted by the user.",
          Toast.LENGTH_SHORT
        ).show()
        finish()
      }
    }
  }

  /**
   * Check if all permission specified in the manifest have been granted
   */
  private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
    checkPermission(
      it, Process.myPid(), Process.myUid()
    ) == PackageManager.PERMISSION_GRANTED
  }

  override fun onCaptureFinished(file: File) {
    val msg = "Photo capture succeeded: ${file.absolutePath}"
    Log.d(TAG, msg)

    lastSavedFile = file.absolutePath
    setImageView(originalImageView, lastSavedFile)
  }

  // And update once new picture is taken?
  // Alternatively we can provide user an ability to select any of taken photos
  private fun getLastTakenPicture(): String {
    val directory = baseContext.filesDir // externalMediaDirs.first()
    var files =
      directory.listFiles()?.filter { file -> file.absolutePath.endsWith(".jpg") }?.sorted()
    if (files == null || files.isEmpty()) {
      Log.d(TAG, "there is no previous saved file")
      return ""
    }

    val file = files.last()
    Log.d(TAG, "lastsavedfile: " + file.absolutePath)
    return file.absolutePath
  }

  override fun onListFragmentInteraction(item: String) {
    Log.d(TAG, item)
    selectedStyle = item
    stylesFragment.dismiss()

    startRunningModel()
  }

  private fun getUriFromAssetThumb(thumb: String): String {
    return "file:///android_asset/thumbnails/$thumb"
  }

  private fun startRunningModel() {
    if (!isRunningModel && lastSavedFile.isNotEmpty() && selectedStyle.isNotEmpty()) {
      val chooseStyleLabel: TextView = findViewById(R.id.choose_style_text_view)
      chooseStyleLabel.visibility = View.GONE
      enableControls(false)
      setImageView(styleImageView, getUriFromAssetThumb(selectedStyle))
      resultImageView.visibility = View.INVISIBLE
      progressBar.visibility = View.VISIBLE
      viewModel.onApplyStyle(
        baseContext, lastSavedFile, selectedStyle, styleTransferModelExecutor,
        inferenceThread
      )
    } else {
      Toast.makeText(this, "Previous Model still running", Toast.LENGTH_SHORT).show()
    }
  }

  // this transformation is necessary to show the top square of the image as the model
  // will work on this part only, making the preview and the result show the same base
  class CropTop : BitmapTransformation() {
    override fun transform(
      pool: BitmapPool,
      toTransform: Bitmap,
      outWidth: Int,
      outHeight: Int
    ): Bitmap {
      return if (toTransform.width == outWidth && toTransform.height == outHeight) {
        toTransform
      } else ImageUtils.scaleBitmapAndKeepRatio(toTransform, outWidth, outHeight)
    }

    override fun equals(other: Any?): Boolean {
      return other is CropTop
    }

    override fun hashCode(): Int {
      return ID.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
      messageDigest.update(ID_BYTES)
    }

    companion object {
      private const val ID = "org.tensorflow.lite.examples.styletransfer.CropTop"
      private val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))
    }
  }
}
