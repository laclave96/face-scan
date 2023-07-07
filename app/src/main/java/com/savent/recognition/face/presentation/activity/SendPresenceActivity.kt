package com.savent.recognition.face.presentation.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.savent.recognition.face.AppConstants
import com.savent.recognition.face.R
import com.savent.recognition.face.databinding.ActivitySendPresenceBinding
import com.savent.recognition.face.domain.usecase.LocationRequestUseCase
import com.savent.recognition.face.presentation.dialog.PersonsDialog
import com.savent.recognition.face.presentation.viewmodel.SendPresenceViewModel
import com.savent.recognition.face.utils.IsLocationGranted
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SendPresenceActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySendPresenceBinding
    private val locationRequestUseCase = LocationRequestUseCase()
    private val sendPresenceViewModel: SendPresenceViewModel by viewModel()
    private var currentPlace: String? = null
    private var personsDialog: PersonsDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        subscribeToObservables()
    }

    private fun init() {
        binding = ActivitySendPresenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = sendPresenceViewModel
        initEvents()
    }

    private fun initEvents() {

        binding.locate.setOnClickListener {
            if (!IsLocationGranted(this)) {
                requestLocationPermission()
                return@setOnClickListener
            }
            executeLocationRequest()
        }

        binding.seeDetectedPersons.setOnClickListener {
            personsDialog =
                PersonsDialog(this, sendPresenceViewModel.personsDetected.value ?: listOf())
            personsDialog?.show()
        }

        binding.sendPresence.setOnClickListener {
            if (currentPlace.isNullOrEmpty()) {
                Toast.makeText(
                    this@SendPresenceActivity,
                    getString(R.string.location_error),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            binding.sendPresence.isEnabled = false
            sendPresenceViewModel.sendPresences()
        }

    }

    private fun subscribeToObservables() {

        sendPresenceViewModel.locating.observe(this) {
            if (it) binding.locating.playAnimation()
            else binding.locating.pauseAnimation()
        }

        sendPresenceViewModel.loading.observe(this) {
            if (it) {
                binding.progress.playAnimation()
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.progress.pauseAnimation()
                binding.progress.visibility = View.GONE
            }
        }

        sendPresenceViewModel.latLng.observe(this) {
            sendPresenceViewModel.removeLocationUpdates()
            sendPresenceViewModel.getPlace(it)
        }

        sendPresenceViewModel.personsDetected.observe(this) {
            personsDialog?.setData(it)
        }

        sendPresenceViewModel.place.observe(this) {
            currentPlace = it
            if (it.isEmpty()) return@observe
            binding.place.text = "\uD83D\uDCCD $it"
        }


        lifecycleScope.launchWhenCreated {
            sendPresenceViewModel.uiEvent.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { uiEvent ->
                    when (uiEvent) {
                        is SendPresenceViewModel.UiEvent.ShowMessage -> {
                            Toast.makeText(
                                this@SendPresenceActivity,
                                uiEvent.resId?.let { getString(it) } ?: uiEvent.message
                                ?: getString(R.string.unknown_error),
                                Toast.LENGTH_LONG
                            ).show()
                            binding.sendPresence.isEnabled = true
                        }
                        is SendPresenceViewModel.UiEvent.Finish -> {
                            finish()
                        }
                    }
                }

        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            AppConstants.LOCATION_PERMISSIONS, AppConstants.REQUEST_LOCATION_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && IsLocationGranted(this)) {
                executeLocationRequest()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQUEST_LOCATION_CODE && resultCode == Activity.RESULT_OK) {
            sendPresenceViewModel.runLocationUpdates()
        }
    }

    fun executeLocationRequest() {
        lifecycleScope.launch {
            locationRequestUseCase(this@SendPresenceActivity).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        sendPresenceViewModel.runLocationUpdates()
                    }
                    else -> {
                        return@collectLatest
                    }
                }

            }
        }
    }
}