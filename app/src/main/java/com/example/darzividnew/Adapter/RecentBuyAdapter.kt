package com.example.darzividnew.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.darzividnew.databinding.RecentBuyItemBinding

class RecentBuyAdapter(
    private var context: Context,
    private var serviceNameList: ArrayList<String>,
    private var serviceImageList: ArrayList<String>,
    private var serviceQuantityList: ArrayList<String>,
    private var servicePriceList: ArrayList<Int>
) : RecyclerView.Adapter<RecentBuyAdapter.RecentViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val binding =
            RecentBuyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentViewHolder(binding)
    }

    override fun getItemCount(): Int = serviceNameList.size

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class RecentViewHolder(private val binding: RecentBuyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                serviceName.text = serviceNameList[position]
                servicePrice.text = servicePriceList[position].toString()
                serviceQuantity.text = serviceQuantityList[position]
                val uriString = serviceImageList[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(serviceImage)
            }
        }
    }

}
