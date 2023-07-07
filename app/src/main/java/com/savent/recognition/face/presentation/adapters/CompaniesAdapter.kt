package com.savent.recognition.face.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.savent.recognition.face.R
import com.savent.recognition.face.data.local.model.CompanyEntity
import com.savent.recognition.face.utils.NameFormat


class CompaniesAdapter(private val context: Context) :
    RecyclerView.Adapter<CompaniesAdapter.CompanyViewHolder>()  {

    val companies = ArrayList<CompanyEntity>()
    private var _listener: OnClickListener? = null

    interface OnClickListener {
        fun onClick(companyId: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        _listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.string_layout, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val companyItem = companies[position]
        holder.name.text = NameFormat.format(companyItem.name)
        holder.name.setOnClickListener {
            _listener?.onClick(companyItem.remoteId)
        }
    }

    override fun getItemCount(): Int = companies.size

    override fun getItemId(position: Int): Long = companies[position].id.toLong()

    fun setData(newCompanies: List<CompanyEntity>) {
        val diffCallback = CompaniesDiffCallBack(companies, newCompanies)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        companies.clear()
        companies.addAll(newCompanies)
        diffResult.dispatchUpdatesTo(this)
    }

    class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.text)
    }
}