package com.example.darzividnew.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.darzividnew.DetailsActivity
import com.example.darzividnew.Model.MenuItem
import com.example.darzividnew.databinding.MenuItemBinding

class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val requireContext: Context
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuViewHolder(private val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }
            }
        }

        private fun openDetailsActivity(position: Int) {
            val menuItem = menuItems[position]

            // a intent to open details activity and pass data
            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("MenuItemName", menuItem.serviceName)
                putExtra("MenuItemImage", menuItem.serviceImage)
                putExtra("MenuItemDescription", menuItem.serviceDescription)
                putExtra("MenuItemIngredients", menuItem.serviceIngredient)
                putExtra("MenuItemPrice", menuItem.servicePrice)

            }
            // start the details activity
            requireContext.startActivity(intent)

        }

        // set data in to recyclerview items name, price, image
        fun bind(position: Int) {
            val menuItem = menuItems[position]
            binding.apply {
                menuServiceName.text = menuItem.serviceName
                menuPrice.text = menuItem.servicePrice
                val uri = Uri.parse(menuItem.serviceImage)
                Glide.with(requireContext).load(uri).into(menuImage)
    }
}
    }
}
