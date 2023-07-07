package com.savent.recognition.face.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.savent.recognition.face.AppConstants
import com.savent.recognition.face.R
import com.savent.recognition.face.databinding.ActivityMainBinding
import com.savent.recognition.face.presentation.dialog.CompaniesDialog
import com.savent.recognition.face.presentation.viewmodel.MainViewModel
import com.savent.recognition.face.utils.CameraIntent
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), CompaniesDialog.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModel()
    private var imageUri: Uri? = null
    private var companiesDialog: CompaniesDialog? = null
    private val KEY_IMAGE_URI = "com.savent.recognition.face.KEY_IMAGE_URI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        subscribeToObservables()
    }

    private fun init() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = mainViewModel
        initEvents()
    }

    private fun initEvents() {
        binding.companyContainer.setOnClickListener {
            mainViewModel.reloadCompanies()
            companiesDialog = CompaniesDialog(this, mainViewModel.companies.value ?: listOf())
            companiesDialog?.setOnClickListener(this)
            companiesDialog?.show()
        }
    }

    private fun subscribeToObservables() {

        mainViewModel.selectedCompany.observe(this){
            binding.companyTv.text = it
        }

        mainViewModel.companies.observe(this) {
            companiesDialog?.setData(it)
        }

        lifecycleScope.launchWhenCreated {
            mainViewModel.uiEvent.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { uiEvent ->
                    when (uiEvent) {
                        is MainViewModel.UiEvent.ShowMessage -> {
                            Toast.makeText(
                                this@MainActivity,
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
        if (!mainViewModel.isCompanySelected()) {
            Toast.makeText(this,getString(R.string.select_company),Toast.LENGTH_LONG).show()
            return
        }
        imageUri = CameraIntent.start(this)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppConstants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri?.let {
                val intent = Intent(this, FaceDetectionActivity::class.java)
                intent.putExtra("imageUri", it.toString())
                startActivity(intent)
            }?:Toast.makeText(this,R.string.uri_error, Toast.LENGTH_LONG).show()

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onClick(companyId: Int) {
        mainViewModel.setCompany(companyId)
    }


}