package com.example.darzividnew.Fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.darzividnew.Adapter.BuyAgainAdapter
import com.example.darzividnew.Model.OrderDetails
import com.example.darzividnew.RecentOrderItems
import com.example.darzividnew.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        // Initialize Firebase auth
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase database
        database = FirebaseDatabase.getInstance()

        // Retrieve and display the User's Order History
        retrieveBuyHistory()

        // recent buy Button Click
        binding.recentbuyitem.setOnClickListener {
            seeItemsRecentBuy()
        }
        binding.receivedButton.setOnClickListener {

            updateOrderStatus()
        }
        return binding.root
    }

    private fun updateOrderStatus() {
        val itemPushKey = listOfOrderItem[0].itemPushKey
        val completeOrderReference = database.reference.child("CompletedOrder").child(itemPushKey!!)
        completeOrderReference.child("paymentReceived").setValue(true)
    }

    // Function to see recent buy
    private fun seeItemsRecentBuy() {
        listOfOrderItem.firstOrNull()?.let { recentBuy ->
            val intent = Intent(requireContext(), RecentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem",listOfOrderItem)
            startActivity(intent)
        }
    }

    // Function to retrieve items from the user's order history
    private fun retrieveBuyHistory() {
        userId = auth.currentUser?.uid ?: ""

        val buyItemReference: DatabaseReference =
            database.reference.child("user").child(userId).child("BuyHistory")
        val sortingQuery = buyItemReference.orderByChild("currentTime")

        sortingQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (buySnapshot in snapshot.children) {
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                    buyHistoryItem?.let {
                        listOfOrderItem.add(it)
                    }
                }

                listOfOrderItem.reverse()

                if (listOfOrderItem.isNotEmpty()) {
                    // Display the most recent order details
                    setDataInRecentBuyItem()
                    // Set up the RecyclerView with previous order details
                    setPreviousBuyItemsRecyclerView()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

    // Function to display the most recent order details
    private fun setDataInRecentBuyItem() {
        val recentOrderItem = listOfOrderItem.firstOrNull()
        recentOrderItem?.let {
            with(binding) {
                buyAgainserviceName.text = it.serviceNames?.firstOrNull() ?: ""
                buyAgainservicePrice.text = it.servicePrices?.firstOrNull() ?: ""
                val image = it.serviceImages?.firstOrNull() ?: ""
                Glide.with(requireContext()).load(image).into(buyAgainserviceImage)

                val isOrderIsAccepted = listOfOrderItem[0].orderAccepted
                Log.d("TAG", "setDataInRecentBuyItem: $isOrderIsAccepted")
                if (isOrderIsAccepted){
                    orderdStutus.background.setTint(Color.GREEN)
                    receivedButton.visibility = View.VISIBLE
                }
            }
        }
    }

    // Function to set up the RecyclerView with previous order details
    private fun setPreviousBuyItemsRecyclerView() {
        val buyAgainserviceName = mutableListOf<String>()
        val buyAgainservicePrice = mutableListOf<String>()
        val buyAgainserviceImage = mutableListOf<String>()

        for (i in 1 until listOfOrderItem.size) {
            listOfOrderItem[i].serviceNames?.firstOrNull()?.let {
                buyAgainserviceName.add(it)
                listOfOrderItem[i].servicePrices?.firstOrNull()?.let {
                    buyAgainservicePrice.add(it)
                    listOfOrderItem[i].serviceImages?.firstOrNull()?.let {
                        buyAgainserviceImage.add(it)
                    }
                }
            }
        }

        val rv = binding.BuyAgainRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
        buyAgainAdapter = BuyAgainAdapter(
            buyAgainserviceName,
            buyAgainservicePrice,
            buyAgainserviceImage,
            requireContext()
        )
        rv.adapter = buyAgainAdapter
    }
}