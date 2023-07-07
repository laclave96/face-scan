package com.savent.recognition.face.presentation.dialog

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.savent.recognition.face.R
import com.savent.recognition.face.presentation.adapters.DetectedFacesAdapter
import com.savent.recognition.face.presentation.model.DetectedFaceItem

class PersonsDialog(context: Context, faces: List<DetectedFaceItem>) : BottomSheetDialog(context),
    DetectedFacesAdapter.OnClickListener {

    private var facesAdapter: DetectedFacesAdapter
    private val allFacesDetected: List<DetectedFaceItem> = faces

    fun setData(newFaces: List<DetectedFaceItem>) {
        facesAdapter.setData(newFaces)
    }

    init {
        setCancelable(true)
        setOnCancelListener { dismiss() }
        setContentView(R.layout.default_dialog)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        val title = findViewById<TextView>(R.id.title)
        val icon = findViewById<ImageView>(R.id.iconSearch)
        val close = findViewById<ImageView>(R.id.close)
        val searchView = findViewById<EditText>(R.id.searchView)

        title?.text = context.getString(R.string.persons_detected)

        facesAdapter = DetectedFacesAdapter(context)
        facesAdapter.setOnClickListener(this)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        facesAdapter.setHasStableIds(true)
        recyclerView?.adapter = facesAdapter
        recyclerView?.itemAnimator = DefaultItemAnimator()
        setData(faces)

        searchView?.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    facesAdapter.setData(allFacesDetected.filter { it.name.contains(s,true) })
                    if (s.toString().isNotEmpty()) {
                        icon?.visibility = View.VISIBLE
                        icon?.setImageResource(R.drawable.ic_round_close_24)
                    } else {
                        icon?.setImageResource(R.drawable.ic_round_search_24)
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })

        icon?.setOnClickListener {
            searchView?.setText("")
        }

        close?.setOnClickListener {
            dismiss()
        }

    }

    override fun add(faceVector: FloatArray) {
    }

}