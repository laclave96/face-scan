package com.savent.recognition.face.presentation.viewmodel

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.savent.recognition.face.ConnectivityObserver
import com.savent.recognition.face.MyApplication
import com.savent.recognition.face.R
import com.savent.recognition.face.data.local.datasource.AppPreferencesLocalDatasource
import com.savent.recognition.face.data.local.model.AppPreferences
import com.savent.recognition.face.domain.repository.PresenceRepository
import com.savent.recognition.face.domain.usecase.GetLastDetectedPersonsUseCase
import com.savent.recognition.face.domain.usecase.GetPlaceByCoordinatesUseCase
import com.savent.recognition.face.domain.usecase.GetPresencesUseCase
import com.savent.recognition.face.presentation.model.DetectedFaceItem
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SendPresenceViewModel(
    private val myApplication: MyApplication,
    private val appPreferencesLocalDatasource: AppPreferencesLocalDatasource,
    private val getLastDetectedPersonsUseCase: GetLastDetectedPersonsUseCase,
    private val getPlaceByCoordinatesUseCase: GetPlaceByCoordinatesUseCase,
    private val getPresencesUseCase: GetPresencesUseCase,
    private val presenceRepository: PresenceRepository
) : ViewModel() {

    private val _locating = MutableLiveData(false)
    val locating: LiveData<Boolean> = _locating

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private var _appPreferences: AppPreferences? = null

    private var locationManager: LocationManager? = null

    private val _latLng = MutableLiveData<LatLng>()
    val latLng: LiveData<LatLng> = _latLng

    private val _place = MutableLiveData("")
    val place: LiveData<String> = _place

    private val _networkStatus = MutableLiveData(ConnectivityObserver.Status.Available)
    val networkStatus: LiveData<ConnectivityObserver.Status> = _networkStatus

    private val _personsDetected = MutableLiveData<List<DetectedFaceItem>>()
    val personsDetected: LiveData<List<DetectedFaceItem>> = _personsDetected

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowMessage(val resId: Int? = null, val message: String? = null) : UiEvent()
        object Finish : UiEvent()
    }

    private var networkObserverJob: Job? = null
    private var loadAppPreferencesJob: Job? = null
    private var loadPersonsDetectedJob: Job? = null
    private var getPlaceJob: Job? = null
    private var locationUpdatesJob: Job? = null
    private var sendPresenceJob: Job? = null

    init {
        locationManager =
            myApplication.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        loadAppPreferences()
        observeNetworkChange()
        loadPersonsDetected()
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

    private fun loadPersonsDetected() {
        loadPersonsDetectedJob?.cancel()
        loadPersonsDetectedJob = viewModelScope.launch(Dispatchers.IO) {
            getLastDetectedPersonsUseCase().onEach {
                when (it) {
                    is Resource.Success -> {
                        _personsDetected.postValue(it.data)
                    }
                    else -> {
                        _uiEvent.emit(UiEvent.ShowMessage(it.resId, it.message))
                    }
                }
            }.collect()

        }
    }

    fun getPlace(latLng: LatLng) {
        _locating.postValue(true)
        getPlaceJob?.cancel()
        getPlaceJob = viewModelScope.launch(Dispatchers.IO) {
            if (!isInternetAvailable()) return@launch
            when (val result = getPlaceByCoordinatesUseCase(latLng)) {
                is Resource.Success -> {
                    _locating.postValue(false)
                    _place.postValue(result.data)
                }
                else -> {
                    _locating.postValue(false)
                    _place.postValue("Desconocido")
                    _uiEvent.emit(UiEvent.ShowMessage(R.string.location_error))
                }
            }
        }
    }

    fun sendPresences() {
        _loading.postValue(true)
        sendPresenceJob?.cancel()
        sendPresenceJob = viewModelScope.launch(Dispatchers.IO) {
            if (!isInternetAvailable()) return@launch
            when (val result =
                getPresencesUseCase(_appPreferences?.companyId!!, latLng.value!!, place.value!!)) {
                is Resource.Success -> {
                    when (val result1 = presenceRepository.insertPresences(
                        *(result.data?.toTypedArray() ?: arrayOf())
                    )) {
                        is Resource.Success -> {
                            _uiEvent.emit(UiEvent.Finish)
                        }
                        is Resource.Error -> {
                            _uiEvent.emit(UiEvent.ShowMessage(result1.resId, result1.message))
                            _loading.postValue(false)
                        }
                    }
                }
                is Resource.Error -> {
                    _loading.postValue(false)
                    _uiEvent.emit(UiEvent.ShowMessage(result.resId, result.message))
                }
            }
        }
    }


    private suspend fun requestLocationUpdates() {
        _locating.postValue(true)
        var isGpsProviderAvailable = false
        var isNetworkProviderAvailable = false
        if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000, 0f, locationListenerNetwork
            )
            isNetworkProviderAvailable = true
        }
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 0f, locationListenerGps
            )
            isGpsProviderAvailable = true
        }
        if (!isGpsProviderAvailable && !isNetworkProviderAvailable) {
            _locating.postValue(false)
            _uiEvent.emit(UiEvent.ShowMessage(resId = R.string.location_error))
        }

    }

    fun removeLocationUpdates() {
        locationManager?.removeUpdates(locationListenerGps)
        locationManager?.removeUpdates(locationListenerNetwork)
        _locating.value = false
    }

    fun runLocationUpdates() {
        locationUpdatesJob?.cancel()
        removeLocationUpdates()
        locationUpdatesJob = viewModelScope.launch {
            requestLocationUpdates()
        }
    }

    private val locationListenerNetwork: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _latLng.value = LatLng(location.latitude, location.longitude)
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    private val locationListenerGps: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _latLng.value = LatLng(location.latitude, location.longitude)
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    private suspend fun isInternetAvailable(): Boolean {
        if (_networkStatus.value != ConnectivityObserver.Status.Available) {
            _locating.postValue(false)
            _loading.postValue(false)
            _uiEvent.emit(UiEvent.ShowMessage(resId = R.string.internet_error))
            return false
        }
        return true
    }


}