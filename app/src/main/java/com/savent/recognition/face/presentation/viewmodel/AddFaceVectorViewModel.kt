package com.savent.recognition.face.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savent.recognition.face.ConnectivityObserver
import com.savent.recognition.face.MyApplication
import com.savent.recognition.face.R
import com.savent.recognition.face.data.local.datasource.AppPreferencesLocalDatasource
import com.savent.recognition.face.data.local.model.AppPreferences
import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.domain.repository.EmployeeRepository
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddFaceVectorViewModel(
    private val myApplication: MyApplication,
    private val appPreferencesLocalDatasource: AppPreferencesLocalDatasource,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _networkStatus = MutableLiveData(ConnectivityObserver.Status.Available)
    val networkStatus: LiveData<ConnectivityObserver.Status> = _networkStatus

    private var _appPreferences: AppPreferences? = null

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        class ShowMessage(val resId: Int? = null, val message: String? = null) : UiEvent()
        object Finish : UiEvent()
    }

    private var networkObserverJob: Job? = null
    private var loadAppPreferencesJob: Job? = null
    private var defaultJob: Job? = null

    init {
        loadAppPreferences()
        observeNetworkChange()
    }

    private fun observeNetworkChange() {
        networkObserverJob?.cancel()
        networkObserverJob = viewModelScope.launch(Dispatchers.IO) {
            _networkStatus.postValue(myApplication.currentNetworkStatus)
            myApplication.networkStatus.collectLatest {
                _networkStatus.postValue(it)
            }
        }

    }

    private fun loadAppPreferences() {
        loadAppPreferencesJob?.cancel()
        loadAppPreferencesJob = viewModelScope.launch(Dispatchers.IO) {
            appPreferencesLocalDatasource.getAppPreferences().onEach {
                if (it is Resource.Success && it.data != null) {
                    _appPreferences = it.data
                }

            }.collect()
        }
    }

    fun addFaceVector(companyId: Int, faceVectors: List<FloatArray>, credentials: Credentials) {
        _loading.postValue(true)
        defaultJob?.cancel()
        defaultJob = viewModelScope.launch(Dispatchers.IO) {

            if (!isInternetAvailable()) return@launch
            when (val result =
                employeeRepository.insertEmployeeFaceVector(companyId, faceVectors, credentials)) {
                is Resource.Success -> {
                    when (val result1 = result.data?.let {
                        employeeRepository.insertEmployeesLocally(
                            it
                        )
                    } ?: Resource.Error()) {
                        is Resource.Success -> {
                            _uiEvent.emit(UiEvent.Finish)
                        }
                        else -> {
                            _loading.postValue(false)
                            _uiEvent.emit(UiEvent.ShowMessage(result1.resId, result1.message))
                        }
                    }

                }
                else -> {
                    _loading.postValue(false)
                    _uiEvent.emit(UiEvent.ShowMessage(result.resId, result.message))
                }
            }
        }
    }

    fun getCurrentCompanyId() = _appPreferences?.companyId

    private suspend fun isInternetAvailable(): Boolean {
        if (_networkStatus.value != ConnectivityObserver.Status.Available) {
            _loading.postValue(false)
            _uiEvent.emit(UiEvent.ShowMessage(resId = R.string.internet_error))
            return false
        }
        return true
    }
}