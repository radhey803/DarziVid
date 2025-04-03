package com.example.darzividnew.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.darzividnew.Adapter.CartAdapter
import com.example.darzividnew.Model.CartItems
import com.example.darzividnew.PayOutActivity
import com.example.darzividnew.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var serviceNames: MutableList<String>
    private lateinit var servicePrices: MutableList<String>
    private lateinit var serviceDescriptions: MutableList<String>
    private lateinit var serviceImagesUri: MutableList<String>
    private lateinit var serviceIngredients: MutableList<String>
    private lateinit var quantity: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        retrieveCartItems()

        binding.proceedButton.setOnClickListener {
            // get order items details before proceeding to check out
            getOrderItemsDetail()
        }
        return binding.root
    }

    private fun getOrderItemsDetail() {

        val orderIdReference: DatabaseReference = database.reference.child("user").child(userId).child("CartItems")

        val serviceName = mutableListOf<String>()
        val servicePrice = mutableListOf<String>()
        val serviceImage = mutableListOf<String>()
        val serviceDescription = mutableListOf<String>()
        val serviceIngredient = mutableListOf<String>()
        // get items Quantities
        val serviceQuantities = cartAdapter.getUpdatedItemsQuantities()

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (serviceSnapshot in snapshot.children) {
                    // get the cartItems to respective List
                    val orderItems = serviceSnapshot.getValue(CartItems::class.java)
                    // add items details in to list
                    orderItems?.serviceName?.let { serviceName.add(it) }
                    orderItems?.servicePrice?.let { servicePrice.add(it) }
                    orderItems?.serviceDescription?.let { serviceDescription.add(it) }
                    orderItems?.serviceImage?.let { serviceImage.add(it) }
                    orderItems?.servicengredients?.let { serviceIngredient.add(it) }
                }
                orderNow(
                    serviceName,
                    servicePrice,
                    serviceDescription,
                    serviceImage,
                    serviceIngredient,
                    serviceQuantities
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(), "Order making Failed. Please Tray Again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun orderNow(
        serviceName: MutableList<String>,
        servicePrice: MutableList<String>,
        serviceDescription: MutableList<String>,
        serviceImage: MutableList<String>,
        serviceIngredient: MutableList<String>,
        serviceQuantities: MutableList<Int>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putExtra("serviceItemName", serviceName as ArrayList<String>)
            intent.putExtra("serviceItemPrice", servicePrice as ArrayList<String>)
            intent.putExtra("serviceItemImage", serviceImage as ArrayList<String>)
            intent.putExtra("serviceItemDescription", serviceDescription as ArrayList<String>)
            intent.putExtra("serviceItemIngredient", serviceIngredient as ArrayList<String>)
            intent.putExtra("serviceItemQuantities", serviceQuantities as ArrayList<Int>)
            startActivity(intent)
        }

    }

    private fun retrieveCartItems() {

        // database reference to the Firebase
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""
        val serviceReference: DatabaseReference =
            database.reference.child("user").child(userId).child("CartItems")
        // list to store cart items
        serviceNames = mutableListOf()
        servicePrices = mutableListOf()
        serviceDescriptions = mutableListOf()
        serviceImagesUri = mutableListOf()
        serviceIngredients = mutableListOf()
        quantity = mutableListOf()

        // fetch data from the database
        serviceReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (serviceSnapshot in snapshot.children) {
                    // get the cartItems object from the child node
                    val cartItems = serviceSnapshot.getValue(CartItems::class.java)

                    // add cart items details to the list
                    cartItems?.serviceName?.let { serviceNames.add(it) }
                    cartItems?.servicePrice?.let { servicePrices.add(it) }
                    cartItems?.serviceDescription?.let { serviceDescriptions.add(it) }
                    cartItems?.serviceImage?.let { serviceImagesUri.add(it) }
                    cartItems?.serviceQuantity?.let { quantity.add(it) }
                    cartItems?.servicengredients?.let { serviceIngredients.add(it) }
                }

                setAdapter()
            }

            private fun setAdapter() {
                cartAdapter= CartAdapter(
                    requireContext(),
                    serviceNames,
                    servicePrices,
                    serviceDescriptions,
                    serviceImagesUri,
                    quantity,
                    serviceIngredients
                )
                binding.cartRecyclerView.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.cartRecyclerView.adapter = cartAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "data not fetch", Toast.LENGTH_SHORT).show()
            }

        })
    }

    companion object {
    }
}