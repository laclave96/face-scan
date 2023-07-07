package com.savent.recognition.face.presentation.activity

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.face.Face
import com.savent.recognition.face.AppConstants
import com.savent.recognition.face.R
import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.databinding.ActivityFaceDetectionBinding
import com.savent.recognition.face.detectionutils.BitmapUtils
import com.savent.recognition.face.detectionutils.FaceDetectorProcessor
import com.savent.recognition.face.presentation.dialog.AddFaceVectorDialog
import com.savent.recognition.face.presentation.dialog.FacesDialog
import com.savent.recognition.face.presentation.viewmodel.AddFaceVectorViewModel
import com.savent.recognition.face.presentation.viewmodel.FaceDetectionViewModel
import com.savent.recognition.face.utils.CameraIntent
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class FaceDetectionActivity : AppCompatActivity(), FaceDetectorProcessor.OnCompletionListener,
    FacesDialog.OnClickListener, AddFaceVectorDialog.OnClickListener {
    private lateinit var binding: ActivityFaceDetectionBinding
    private val faceDetectionViewModel: FaceDetectionViewModel by viewModel()

    private val SIZE_SCREEN = "w:screen" // Match screen width
    private val SIZE_1920_1080 = "w:1920" // ~1920*1080 in a normal ratio
    private val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
    private val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio
    private val KEY_IMAGE_URI = "com.savent.recognition.face.KEY_IMAGE_URI"
    private val selectedSize = SIZE_1024_768
    var isLandScape = false
    private var imageUri: Uri? = null
    private lateinit var resizedBitmap: Bitmap
    private var imageMaxWidth = 0
    private var imageMaxHeight = 0
    private var detectorProcessor: FaceDetectorProcessor? = null
    private var facesDialog: FacesDialog? = null
    private var addFaceVectorDialog: AddFaceVectorDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        subscribeToObservables()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        imageUri?.let {
            outState.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Uri.parse(savedInstanceState.getString(KEY_IMAGE_URI))?.let {
            imageUri = it
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun init() {
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = faceDetectionViewModel
        intent.getStringExtra("imageUri")?.let { Uri.parse(it) }?.let { imageUri = it }
        detectorProcessor = FaceDetectorProcessor(this)
        detectorProcessor?.addCompletionListener(this)
        /*isLandScape =
            (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)*/

        initEvents()
        initFacesDetection()
    }

    private fun initEvents() {
        /*binding.root
            .viewTreeObserver
            .addOnGlobalLayoutListener(
                object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        imageMaxWidth = binding.root.width
                        imageMaxHeight = binding.root.height - 70
                        if (SIZE_SCREEN == selectedSize) {
                            initFacesDetection()
                        }
                    }
                })*/

        binding.remoteSearch.setOnClickListener {
            if (faceDetectionViewModel.remoteSearchCompleted.value == true) {
                Toast.makeText(this, getString(R.string.remote_search_completed), Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            faceDetectionViewModel.detectedFaces.value?.let { detectedFaces ->
                if (detectedFaces.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_detected_faces), Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }
                faceDetectionViewModel.getUnknownFacesVectors(detectedFaces).let {
                    if (it.isNotEmpty()) {
                        faceDetectionViewModel.executeRemoteSearch(it)
                        return@setOnClickListener
                    }
                    Toast.makeText(this, getString(R.string.no_unknown_faces), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        binding.seeDetectedFaces.setOnClickListener {
            facesDialog = FacesDialog(this, faceDetectionViewModel.detectedFaces.value ?: listOf())
            facesDialog?.setOnClickListener(this)
            facesDialog?.show()
        }

        binding.next.setOnClickListener {
            faceDetectionViewModel.detectedFaces.value?.let { detectedFaces ->
                if (detectedFaces.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_detected_faces), Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }
                faceDetectionViewModel.getUnknownFacesVectors(detectedFaces).let {
                    if (it.isNotEmpty()) {
                        if (faceDetectionViewModel.remoteSearchCompleted.value == false) {
                            Toast.makeText(
                                this,
                                getString(R.string.pending_remote_search),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            return@setOnClickListener
                        }
                        Toast.makeText(
                            this,
                            getString(R.string.unknown_detected_faces),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        return@setOnClickListener
                    }
                    startActivity(Intent(this, SendPresenceActivity::class.java))
                }

            }

        }

        binding.back.setOnClickListener {
            finish()
        }
    }

    private fun subscribeToObservables() {

        faceDetectionViewModel.loading.observe(this) {
            if (it) {
                binding.progress.playAnimation()
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.progress.pauseAnimation()
                binding.progress.visibility = View.GONE
            }
        }

        faceDetectionViewModel.detectedFaces.observe(this) {
            facesDialog?.setData(it)
        }

        addFaceVectorViewModel.loading.observe(this) {
            addFaceVectorDialog?.setLoading(it)
        }


        lifecycleScope.launchWhenCreated {
            faceDetectionViewModel.uiEvent.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { uiEvent ->
                    when (uiEvent) {
                        is FaceDetectionViewModel.UiEvent.ShowMessage -> {
                            Toast.makeText(
                                this@FaceDetectionActivity,
                                uiEvent.resId?.let { getString(it) } ?: uiEvent.message
                                ?: getString(R.string.unknown_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

        }
    }

    fun takePicture(view: View) {
        imageUri = CameraIntent.start(this)
    }

    private fun initFacesDetection() {
        try {
            if (imageUri == null) return

            if (SIZE_SCREEN == selectedSize && imageMaxWidth == 0) {
                // UI layout has not finished yet, will reload once it's ready.
                return
            }

            val imageBitmap =
                BitmapUtils.getBitmapFromContentUri(contentResolver, imageUri) ?: return


            // Clear the overlay first
            binding.graphicOverlay.clear()

            // Get the dimensions of the image view
            val (first, second) = getDynamicWidthHeight()//getTargetedWidthHeight()

            // Determine how much to scale down the image
            val scaleFactor = Math.max(
                imageBitmap.width.toFloat() / first.toFloat(),
                imageBitmap.height.toFloat() / second.toFloat()
            )

            resizedBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                (imageBitmap.width / scaleFactor).toInt(),
                (imageBitmap.height / scaleFactor).toInt(),
                true
            )

            binding.preview.setImageBitmap(resizedBitmap)

            detectorProcessor?.let {
                binding.graphicOverlay.setImageSourceInfo(
                    resizedBitmap.width, resizedBitmap.height,  /* isFlipped= */false
                )
                it.processBitmap(resizedBitmap, binding.graphicOverlay)
            }

        } catch (e: IOException) {
            imageUri = null
        }
    }

    private fun getTargetedWidthHeight(): Pair<Int, Int> {
        val targetWidth: Int
        val targetHeight: Int
        when (selectedSize) {
            SIZE_SCREEN -> {
                targetWidth = imageMaxWidth
                targetHeight = imageMaxHeight
            }
            SIZE_640_480 -> {
                targetWidth = if (isLandScape) 640 else 480
                targetHeight = if (isLandScape) 480 else 640
            }
            SIZE_1024_768 -> {
                targetWidth = if (isLandScape) 1024 else 768
                targetHeight = if (isLandScape) 768 else 1024
            }
            SIZE_1920_1080 -> {
                targetWidth = if (isLandScape) 1920 else 1080
                targetHeight = if (isLandScape) 1080 else 1920
            }
            else -> throw IllegalStateException("Unknown size")
        }
        return Pair(targetWidth, targetHeight)
    }

    private fun getDynamicWidthHeight(): Pair<Int, Int> {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val screenDensity = resources.displayMetrics.density
        val screenWidthDp = screenWidth.toFloat() / screenDensity
        val screenHeightDp = screenHeight.toFloat() / screenDensity
        val horizontalMarginDp = 30F
        val verticalReservedDp = 275F
        val targetWidth = ((screenWidthDp - horizontalMarginDp) * screenDensity).toInt()
        val imageRatio = 1.33F
        val targetHeight = Math.min(
            (targetWidth * imageRatio).toInt(),
            ((screenHeightDp - verticalReservedDp) * screenDensity
                    ).toInt()
        )
        return Pair(targetWidth, targetHeight)
    }

    private fun cropFaces(faces: List<Face>): ArrayList<Bitmap> {
        val bitmapFaces = ArrayList<Bitmap>()
        val boxs = ArrayList<Rect>()
        for (face in faces) {
            boxs.add(Rect(face.boundingBox))
        }
        for (i in 0 until boxs.size) {
            val rect = boxs[i]
            bitmapFaces.add(
                Bitmap.createBitmap(
                    resizedBitmap,
                    rect.left,
                    rect.top,
                    rect.width(),
                    rect.height()
                )
            )
        }
        return bitmapFaces
    }

    override fun onSuccess(faces: MutableList<Face>?) {
        if (faces?.isEmpty() == true) return
        faces?.let {
            getModelFile()?.let { model ->
                faceDetectionViewModel.extractingEmbeddings(cropFaces(it), model)
            }
        }

    }

    override fun onFailure(e: Exception?) {
        Toast.makeText(this, "Error al detectar los rostros", Toast.LENGTH_LONG).show()
    }

    @Throws(IOException::class)
    private fun getModelFile(modelPath: String = "facenet.tflite"): MappedByteBuffer? {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppConstants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (imageUri == null) Toast.makeText(this, R.string.uri_error, Toast.LENGTH_LONG).show()
            faceDetectionViewModel.imageReloaded()
            initFacesDetection()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun add(faceVector: FloatArray) {
        addFaceVectorViewModel.getCurrentCompanyId()?.let { companyId ->
            if (faceDetectionViewModel.remoteSearchCompleted.value == false) {
                Toast.makeText(
                    this,
                    "Complete una busqueda remota de su rostro antes",
                    Toast.LENGTH_LONG,

                ).show()
                return
            }
            addFaceVectorDialog = AddFaceVectorDialog(
                this,
                faceVector,
                companyId,

            )
            addFaceVectorDialog?.setOnClickListener(this)
            addFaceVectorDialog?.show()
            return
        }
        Toast.makeText(this, getString(R.string.unknown_company), Toast.LENGTH_LONG).show()

    }


    override fun onDestroy() {
        detectorProcessor?.removeCompletionListener()
        super.onDestroy()
    }

    override fun add(companyId: Int, faceVectors: List<FloatArray>, credentials: Credentials) {
        addFaceVectorViewModel.addFaceVector(companyId, faceVectors, credentials)
    }


}