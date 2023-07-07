package com.savent.recognition.face.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savent.recognition.face.ConnectivityObserver
import com.savent.recognition.face.MyApplication
import com.savent.recognition.face.R
import com.savent.recognition.face.data.local.datasource.AppPreferencesLocalDatasource
import com.savent.recognition.face.data.local.model.AppPreferences
import com.savent.recognition.face.data.local.model.CompanyEntity
import com.savent.recognition.face.domain.repository.CompanyRepository
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.time.measureDurationForResult

class MainViewModel(
    private val myApplication: MyApplication,
    private val appPreferencesLocalDatasource: AppPreferencesLocalDatasource,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _networkStatus = MutableLiveData(ConnectivityObserver.Status.Available)
    val networkStatus: LiveData<ConnectivityObserver.Status> = _networkStatus

    private var _appPreferences: AppPreferences? = null

    private val _selectedCompany = MutableLiveData("Seleccione una Empresa")
    val selectedCompany: LiveData<String> = _selectedCompany

    private val _companies = MutableLiveData<List<CompanyEntity>>()
    val companies: LiveData<List<CompanyEntity>> = _companies

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowMessage(val resId: Int? = null, val message: String? = null) : UiEvent()
    }

    private var networkObserverJob: Job? = null
    private var loadAppPreferencesJob: Job? = null
    private var loadCompaniesJob: Job? = null
    private var reloadCompaniesJob: Job? = null
    private var defaultJob: Job? = null

    init {
        loadAppPreferences()
        observeNetworkChange()
        reloadCompanies()
        loadCompanies()
    }


    private fun observeNetworkChange() {
        networkObserverJob?.cancel()
        networkObserverJob = viewModelScope.launch(Dispatchers.IO) {
            _networkStatus.postValue(myApplication.currentNetworkStatus)
            myApplication.networkStatus.collectLatest {
                _networkStatus.postValue(it)
                if (it == ConnectivityObserver.Status.Available)
                    reloadCompanies()
            }
        }

    }

    private fun loadAppPreferences() {
        loadAppPreferencesJob?.cancel()
        loadAppPreferencesJob = viewModelScope.launch(Dispatchers.IO) {
            appPreferencesLocalDatasource.getAppPreferences().onEach {

                if (it is Resource.Success && it.data != null) {
                    _appPreferences = it.data
                    when (val result = companyRepository.getCompanyByRemoteId(it.data.companyId)) {
                        is Resource.Success -> {
                            result.data?.let { company -> _selectedCompany.postValue(company.name) }
                        }
                        else -> {
                            _uiEvent.emit(UiEvent.ShowMessage(result.resId, result.message))
                        }
                    }

                }

            }.collect()
        }
    }

    fun isCompanySelected(): Boolean = _appPreferences?.companyId != null

    private fun loadCompanies() {
        loadCompaniesJob?.cancel()
        loadCompaniesJob = viewModelScope.launch(Dispatchers.IO) {
            companyRepository.getCompanies("").onEach {
                when (it) {
                    is Resource.Success -> {
                        _companies.postValue(it.data)
                    }
                    else -> {
                        _uiEvent.emit(UiEvent.ShowMessage(it.resId, it.message))
                    }
                }

            }.collect()
        }

    }

    fun reloadCompanies() {
        _loading.postValue(true)
        reloadCompaniesJob?.cancel()
        reloadCompaniesJob = viewModelScope.launch(Dispatchers.IO) {
            if (!isInternetAvailable()) return@launch
            when (val result = companyRepository.fetchCompanies()) {
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowMessage(result.resId, result.message))
                }
            }

        }
    }

    fun setCompany(companyId: Int) {
        defaultJob?.cancel()
        defaultJob = viewModelScope.launch(Dispatchers.IO) {
            when (val result =
                appPreferencesLocalDatasource.updateAppPreferences(AppPreferences(companyId))) {
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowMessage(result.resId, result.message))
                }

            }

        }

    }


    private suspend fun isInternetAvailable(): Boolean {
        if (_networkStatus.value != ConnectivityObserver.Status.Available) {
            _loading.postValue(false)
            _uiEvent.emit(UiEvent.ShowMessage(resId = R.string.internet_error))
            return false
        }
        return true
    }


}