package com.example.darzividnew.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.darzividnew.databinding.BuyAgainItemBinding

class BuyAgainAdapter(
    private val buyAgainserviceName: MutableList<String>,
    private val buyAgainservicePrice: MutableList<String>,
    private val buyAgainserviceImage: MutableList<String>,
    private var requireContext: Context
) : RecyclerView
.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(
            buyAgainserviceName[position],
            buyAgainservicePrice[position],
            buyAgainserviceImage[position]
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding =
            BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun getItemCount(): Int = buyAgainserviceName.size


    inner   class BuyAgainViewHolder(private val binding: BuyAgainItemBinding) : RecyclerView.ViewHolder
        (binding.root) {
        fun bind(serviceName: String, servicePrice: String, serviceImage: String) {
            binding.buyAgainserviceName.text = serviceName
            binding.buyAgainservicePrice.text = servicePrice
            val uriString = serviceImage
            val uri= Uri.parse(uriString)
            Glide.with(requireContext).load(uri).into(binding.buyAgainserviceImage)
        }

    }
}