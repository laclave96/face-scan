package com.savent.recognition.face.presentation.viewmodel

import android.graphics.Bitmap
import android.os.Build
import androidx.lifecycle.*
import com.savent.recognition.face.ConnectivityObserver
import com.savent.recognition.face.MyApplication
import com.savent.recognition.face.R
import com.savent.recognition.face.data.local.datasource.AppPreferencesLocalDatasource
import com.savent.recognition.face.data.local.model.AppPreferences
import com.savent.recognition.face.domain.repository.EmployeeRepository
import com.savent.recognition.face.domain.usecase.GetDetectedFacesUseCase
import com.savent.recognition.face.domain.usecase.SaveLocallyDetectedPersonsUseCase
import com.savent.recognition.face.presentation.model.DetectedFaceItem
import com.savent.recognition.face.presentation.model.FaceData
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.MappedByteBuffer


class FaceDetectionViewModel(
    private val myApplication: MyApplication,
    private val appPreferencesLocalDatasource: AppPreferencesLocalDatasource,
    private val getDetectedFacesUseCase: GetDetectedFacesUseCase,
    private val saveLocallyDetectedPersonsUseCase: SaveLocallyDetectedPersonsUseCase,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private var interpreter: Interpreter? = null
    private var useNNAPI = false
    private var tImage: TensorImage? = null
    private var tBuffer: TensorBuffer? = null
    private val inputImageWidth = 160
    private val inputImageHeight = 160
    private val INPUT_SIZE = 160
    private val IMAGE_MEAN = 127.5f
    private val IMAGE_STD = 128f

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private var _appPreferences: AppPreferences? = null

    private val _numFacesDetected = MutableLiveData(0)
    val numFacesDetected: LiveData<Int> = _numFacesDetected

    private val _remoteSearchCompleted = MutableLiveData(false)
    val remoteSearchCompleted: LiveData<Boolean> = _remoteSearchCompleted

    private val _networkStatus = MutableLiveData(ConnectivityObserver.Status.Available)
    val networkStatus: LiveData<ConnectivityObserver.Status> = _networkStatus

    private val _dataFaces = MutableStateFlow<List<FaceData>>(emptyList())

    private val _detectedFaces = MutableLiveData<List<DetectedFaceItem>>()
    val detectedFaces: LiveData<List<DetectedFaceItem>> = _detectedFaces

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowMessage(val resId: Int? = null, val message: String? = null) : UiEvent()
    }

    private var networkObserverJob: Job? = null
    private var loadAppPreferencesJob: Job? = null
    private var extractEmbeddingJob: Job? = null
    private var loadDetectedFacesJob: Job? = null
    private var remoteSearchJob: Job? = null
    private val mutex = Mutex()

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) useNNAPI = true
        loadAppPreferences()
        observeNetworkChange()
        loadDetectedFaces()

    }


    private fun observeNetworkChange() {
        networkObserverJob?.cancel()
        networkObserverJob = viewModelScope.launch(Dispatchers.IO) {
            _networkStatus.postValue((myApplication).currentNetworkStatus)
            myApplication.networkStatus.collectLatest {
                _networkStatus.postValue(it)
            }
        }

    }

    private fun loadAppPreferences() {
        loadAppPreferencesJob?.cancel()
        loadAppPreferencesJob = viewModelScope.launch(Dispatchers.IO) {
            appPreferencesLocalDatasource.getAppPreferences().onEach {
                if (it is Resource.Success && it.data != null) _appPreferences = it.data
            }.collect()
        }
    }

    fun extractingEmbeddings(bitmapFaces: List<Bitmap>, modelFile: MappedByteBuffer) {
        _numFacesDetected.postValue(bitmapFaces.size)
        _loading.postValue(true)
        extractEmbeddingJob?.cancel()
        extractEmbeddingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                loadInterpreter(modelFile)
                val dataFaces =
                    async { bitmapFaces.map { FaceData(it, getFeatureVector(it)) } }.await()

                _dataFaces.emit(dataFaces)
                _remoteSearchCompleted.postValue(false)
                interpreter?.close()
                _loading.postValue(false)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvent.emit(UiEvent.ShowMessage(message = "Error!.Vuelva a tomar una nueva foto"))
            }

        }

    }

    private fun loadDetectedFaces() {
        loadDetectedFacesJob?.cancel()
        loadDetectedFacesJob = viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            getDetectedFacesUseCase(_appPreferences?.companyId ?: 0, _dataFaces).onEach {
                when (it) {
                    is Resource.Success -> {
                        _detectedFaces.postValue(it.data)
                        getUnknownFacesVectors(
                            it.data ?: emptyList()
                        ).let { it1 ->
                            if (it1.isNotEmpty() && remoteSearchCompleted.value == false)
                                executeRemoteSearch(it1)
                            else {
                                val result =
                                    saveLocallyDetectedPersonsUseCase(it.data ?: listOf())
                                if (result is Resource.Error)
                                    _uiEvent.emit(UiEvent.ShowMessage(R.string.save_detected_faces_error))
                            }

                        }
                    }
                    else -> {
                        _uiEvent.emit(UiEvent.ShowMessage(it.resId, it.message))
                    }
                }
            }.collect()
        }

    }

    fun getUnknownFacesVectors(
        detectedFaces: List<DetectedFaceItem>
    ): List<FloatArray> {
        return detectedFaces.filter { it.isUnknown() }.map { it.faceData.featureVector }
    }

    fun imageReloaded() {
        _remoteSearchCompleted.value = true
    }


    fun executeRemoteSearch(facesVectors: List<FloatArray>) {
        _loading.postValue(true)
        remoteSearchJob?.cancel()
        remoteSearchJob = viewModelScope.launch(Dispatchers.IO) {
            if (!isInternetAvailable()) return@launch
            _appPreferences?.let { preferences ->
                when (val result =
                    employeeRepository.fetchEmployees(preferences.companyId, facesVectors)) {
                    is Resource.Success -> {
                        _remoteSearchCompleted.postValue(true)
                        _uiEvent.emit(UiEvent.ShowMessage(R.string.remote_search_completed))
                        _loading.postValue(false)
                    }
                    else -> {
                        _uiEvent.emit(UiEvent.ShowMessage(result.resId, result.message))
                        _loading.postValue(false)
                    }
                }
            } ?: _uiEvent.emit(UiEvent.ShowMessage(message = "Empresa Desconocida"))

        }
    }

    @Throws(IOException::class)
    private fun loadInterpreter(modelFile: MappedByteBuffer) {
        val options: Interpreter.Options = Interpreter.Options()
        options.setUseNNAPI(useNNAPI)
        interpreter = Interpreter(modelFile, options)
        val probabilityTensorIndex = 0
        val probabilityShape: IntArray? =
            interpreter?.getOutputTensor(probabilityTensorIndex)?.shape() // {1, EMBEDDING_SIZE}
        val probabilityDataType: DataType? =
            interpreter?.getOutputTensor(probabilityTensorIndex)?.dataType()
        tImage = TensorImage(DataType.FLOAT32)
        tBuffer = TensorBuffer.createFixedSize(probabilityShape ?: IntArray(0), probabilityDataType)
    }

    @Throws(Exception::class)
    private fun loadImage(bitmap: Bitmap): TensorImage {
        tImage?.load(bitmap)
        val imageProcessor: ImageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
            .build()
        return imageProcessor.process(tImage)
    }

    @Throws(Exception::class)
    private suspend fun getEmbedding(bitmap: Bitmap): FloatArray {
        mutex.withLock {
            tImage = loadImage(bitmap)
            interpreter?.run(tImage?.buffer, tBuffer?.buffer?.rewind())
        }
        return tBuffer?.floatArray ?: FloatArray(0)
    }

    @Throws(Exception::class)
    private suspend fun getFeatureVector(face: Bitmap): FloatArray {
        val scaledBmp: Bitmap = Bitmap.createScaledBitmap(
            face,
            INPUT_SIZE,
            INPUT_SIZE,
            false
        )

        return getEmbedding(scaledBmp)

    }

    suspend fun isInternetAvailable(): Boolean {
        if (_networkStatus.value != ConnectivityObserver.Status.Available) {
            _loading.postValue(false)
            _uiEvent.emit(UiEvent.ShowMessage(resId = R.string.internet_error))
            return false
        }
        return true
    }


}