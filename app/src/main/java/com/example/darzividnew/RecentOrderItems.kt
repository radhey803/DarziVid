package com.example.darzividnew

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.darzividnew.Adapter.RecentBuyAdapter
import com.example.darzividnew.Model.OrderDetails
import com.example.darzividnew.databinding.ActivityRecentOrderItemsBinding

class RecentOrderItems : AppCompatActivity() {
    private val binding: ActivityRecentOrderItemsBinding by lazy {
        ActivityRecentOrderItemsBinding.inflate(layoutInflater)
    }
    private lateinit var allServiceNames: ArrayList<String>
    private lateinit var allServiceImages: ArrayList<String>
    private lateinit var allServicePrices: ArrayList<String>
    private lateinit var allServiceQuantities: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }
        val recentOrderItems =
            intent.getSerializableExtra("RecentBuyOrderItem") as ArrayList<OrderDetails>
        recentOrderItems?.let { orderDetails ->
            if (orderDetails.isNotEmpty()) {
                val recentOrderItem = orderDetails[0]

                allServiceNames = recentOrderItem.serviceNames as ArrayList<String>
                allServiceImages = recentOrderItem.serviceImages as ArrayList<String>
                allServicePrices = recentOrderItem.servicePrices as ArrayList<String>
                allServiceQuantities = recentOrderItem.serviceQuantities as ArrayList<Int>
            }
        }
        setAdapter()
    }

    private fun setAdapter() {
        val rv = binding.recyclerViewRecentBuy
        rv.layoutManager = LinearLayoutManager(this)
        val adapter =
            RecentBuyAdapter(this, allServiceNames, allServiceImages, allServicePrices, allServiceQuantities)
        rv.adapter = adapter
    }
}
