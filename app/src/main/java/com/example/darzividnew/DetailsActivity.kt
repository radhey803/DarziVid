package com.example.darzividnew

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.darzividnew.Model.CartItems
import com.example.darzividnew.databinding.ActivityDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var serviceName: String? = null
    private var serviceImage: String? = null
    private var serviceDescriptions: String? = null
    private var serviceIngredients: String? = null
    private var servicePrice: String? = null
    private lateinit var auth: FirebaseAuth
    private var selectedSize: String = "Medium" // Default size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Get item details from Intent
        serviceName = intent.getStringExtra("MenuItemName")
        serviceDescriptions = intent.getStringExtra("MenuItemDescription")
        serviceIngredients = intent.getStringExtra("MenuItemIngredients")
        servicePrice = intent.getStringExtra("MenuItemPrice")
        serviceImage = intent.getStringExtra("MenuItemImage")

        // Set values in UI
        binding.apply {
            detailserviceName.text = serviceName
            detailDescription.text = serviceDescriptions
            detailIngredients.text = serviceIngredients
            Glide.with(this@DetailsActivity).load(Uri.parse(serviceImage)).into(detailserviceImage)
        }

        // Set up size spinner
        setupSizeSpinner()

        // Add to cart button click
        binding.addItemButton.setOnClickListener {
            addItemToCart()
        }
    }

    private fun setupSizeSpinner() {
        val sizeOptions = arrayOf("Small", "Medium", "Large", "XL", "XXL")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sizeOptions)
        binding.sizeSpinner.adapter = adapter

        binding.sizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSize = sizeOptions[position] // Get selected size
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSize = "Medium" // Default size if nothing selected
            }
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""

        // Create a cart item object
        val cartItem = CartItems(
            serviceName.toString(),
            servicePrice.toString(),
            serviceDescriptions.toString(),
            serviceImage.toString(),
            1, // Quantity
            selectedSize // Selected size
        )

        // Save data to Firebase
        database.child("user").child(userId).child("CartItems").push().setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item added to cart successfully! 😊", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add item to cart 😒", Toast.LENGTH_SHORT).show()
            }
    }
}
