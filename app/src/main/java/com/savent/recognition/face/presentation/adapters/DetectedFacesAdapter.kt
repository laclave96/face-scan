package com.savent.recognition.face.presentation.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import com.savent.recognition.face.R
import com.savent.recognition.face.presentation.model.DetectedFaceItem

class DetectedFacesAdapter(private val context: Context) :
    RecyclerView.Adapter<DetectedFacesAdapter.DetectedFaceViewHolder>() {

    private val faces = ArrayList<DetectedFaceItem>()

    private var _listener: OnClickListener? = null

    interface OnClickListener {
        fun add(faceVector: FloatArray)
    }

    fun setOnClickListener(listener: OnClickListener) {
        _listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectedFaceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.detected_face_item, parent, false)
        return DetectedFaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetectedFaceViewHolder, position: Int) {
        val faceItem = faces[position]
        holder.image.setImageBitmap(faceItem.faceData.bitmap)
        if (faceItem.isUnknown()){
            holder.name.text = context.getString(R.string.unknown)
            holder.add.visibility =  View.VISIBLE
            holder.image.borderColor = context.getColor(R.color.red)
        }else{
            holder.name.text = faceItem.name
            holder.add.visibility =  View.GONE
            holder.image.borderColor = context.getColor(R.color.green)
        }

        holder.add.setOnClickListener {
            _listener?.add(faceItem.faceData.featureVector)
        }
    }

    override fun getItemCount(): Int = faces.size

    override fun getItemId(position: Int): Long {
        return faces[position].faceData.featureVector.hashCode().toLong()
    }

    fun setData(newFaces: List<DetectedFaceItem>) {
        val diffCallback = DetectedFaceDiffCallBack(faces, newFaces)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        faces.clear()
        faces.addAll(newFaces)
        diffResult.dispatchUpdatesTo(this)
    }

    class DetectedFaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: CircularImageView = itemView.findViewById(R.id.image)
        val name: TextView = itemView.findViewById(R.id.name)
        val add: ConstraintLayout = itemView.findViewById(R.id.add)
    }
}