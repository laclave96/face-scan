package com.savent.recognition.face.presentation.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.savent.recognition.face.R
import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.databinding.AddFaceVectorDialogBinding

class AddFaceVectorDialog(context: Context, faceVector: FloatArray, companyId: Int) :
    BottomSheetDialog(context) {

    private var binding: AddFaceVectorDialogBinding
    private var _listener: OnClickListener? = null

    interface OnClickListener {
        fun add(companyId: Int, faceVectors: List<FloatArray>, credentials: Credentials)
    }

    fun setOnClickListener(listener: OnClickListener) {
        _listener = listener
    }

    init {
        setCancelable(true)
        setOnCancelListener { dismiss() }

        val inflater = LayoutInflater.from(getContext())
        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_face_vector_dialog,
            null,
            false
        )
        setContentView(binding.root)

        binding.add.setOnClickListener {
            _listener?.add(
                companyId,
                listOf(faceVector),
                Credentials(
                    binding.rfcEdit.text.toString().trim(),
                    binding.pinEdit.text.toString().trim()
                )
            )
        }


        binding.close.setOnClickListener {
            dismiss()
        }

    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progress.playAnimation()
            binding.progress.visibility = View.VISIBLE
        } else {
            binding.progress.pauseAnimation()
            binding.progress.visibility = View.GONE
        }
    }
}