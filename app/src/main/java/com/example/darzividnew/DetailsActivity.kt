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
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.darzividnew.Fragment.CartFragment
import com.example.darzividnew.Model.CartItems
import com.example.darzividnew.databinding.ActivityDetailsBinding
import com.example.darzividnew.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.razorpay.PaymentResultWithDataListener

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
     var serviceName: String? = null
     var serviceImage: String? = null
     var serviceDescriptions: String? = null
     var serviceIngredients: String? = null
     var servicePrice: String? = null
     lateinit var sizeSpinner: Spinner
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
     var selectedSize: String = "Medium" // Default size

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

        binding.buyNow.setOnClickListener {
            buynow()
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
            selectedSize.toString() // Selected size
        )

        // Save data to Firebase
        database.child("user").child(userId).child("CartItems").push().setValue(cartItem)
            .addOnSuccessListener {
               openCart()
                Toast.makeText(this, "Item added to cart successfully!", Toast.LENGTH_SHORT).show()

            }

            .addOnFailureListener {
                Toast.makeText(this, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
            }
    }
}

public fun DetailsActivity.buynow() {

    val intent = Intent(this, PayOutActivity::class.java)

    // Send details of this single item
    intent.putStringArrayListExtra("serviceItemName", arrayListOf(serviceName ?: ""))
    intent.putStringArrayListExtra("serviceItemPrice", arrayListOf(servicePrice ?: "0"))
    intent.putStringArrayListExtra("serviceItemImage", arrayListOf(serviceImage ?: ""))
    intent.putStringArrayListExtra("serviceItemDescription", arrayListOf(serviceDescriptions ?: ""))
    intent.putStringArrayListExtra("serviceItemIngredient", arrayListOf(serviceIngredients ?: ""))
    intent.putIntegerArrayListExtra("serviceItemQuantities", arrayListOf(1)) // Default quantity 1

    startActivity(intent)


}

private fun DetailsActivity.openCart() {
    val fragment = CartFragment()

    // Optional: Add to back stack so user can press back
    supportFragmentManager.beginTransaction()
        .replace(android.R.id.content, fragment) // or your specific container
        .addToBackStack(null)
        .commit()
}
