package com.example.darzividnew

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.darzividnew.Fragment.CongratsBottomSheet
import com.example.darzividnew.databinding.ActivityPayOutBinding
import com.example.darzividnew.Model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

class PayOutActivity : AppCompatActivity(), PaymentResultWithDataListener {

    lateinit var binding: ActivityPayOutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private lateinit var totalAmount: String
    private lateinit var serviceItemName: ArrayList<String>
    private lateinit var serviceItemPrice: ArrayList<String>
    private lateinit var serviceItemImage: ArrayList<String>
    private lateinit var serviceItemDescription: ArrayList<String>
    private lateinit var serviceItemIngredient: ArrayList<String>
    private lateinit var serviceItemQuantities: ArrayList<Int>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase and User details
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()

        // Get user details from Firebase
        val intent = intent
        serviceItemName = intent.getStringArrayListExtra("serviceItemName") ?: ArrayList()
        serviceItemPrice = intent.getStringArrayListExtra("serviceItemPrice") ?: ArrayList()
        serviceItemImage = intent.getStringArrayListExtra("serviceItemImage") ?: ArrayList()
        serviceItemDescription = intent.getStringArrayListExtra("serviceItemDescription") ?: ArrayList()
        serviceItemIngredient = intent.getStringArrayListExtra("serviceItemIngredient") ?: ArrayList()
        serviceItemQuantities = intent.getIntegerArrayListExtra("serviceItemQuantities") ?: ArrayList()

        // Calculate and display the total amount
        totalAmount = calculateTotalAmount().toString() +" ₹"
        // binding.totalAmount.isEnabled = false
        binding.totalAmount.setText(totalAmount)
        binding.backeButton.setOnClickListener {
            finish()
        }

        // Set user data
        setUserData()

        // Make the total amount TextView non-editable
        binding.totalAmount.isFocusable = false
        binding.totalAmount.isClickable = false

        // Set back button click listener
        binding.backeButton.setOnClickListener {
            finish()
        }

        // Set place order button click listener
        binding.PlaceMyOrder.setOnClickListener {
            // Get data from text fields
            name = binding.name.text.toString().trim()
            address = binding.address.text.toString().trim()
            phone = binding.phone.text.toString().trim()

            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Please Enter All The Details 😜", Toast.LENGTH_SHORT).show()
            } else {
                // Only initiate payment, order will be placed after successful payment
                makePayment()
            }
        }
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("OrderDetails").push().key
        val orderDetails = OrderDetails(
            userId, name, serviceItemName, serviceItemPrice, serviceItemImage, serviceItemQuantities,
            address, totalAmount, phone, time, itemPushKey, false, false
        )

        // Save order details to Firebase
        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails).addOnSuccessListener {
            // Show confirmation bottom sheet
            val bottomSheetDialog = CongratsBottomSheet()
            bottomSheetDialog.show(supportFragmentManager, "CongratsBottomSheet")

            // Remove items from cart and add to order history
            removeItemFromCart()
            addOrderToHistory(orderDetails)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to order 😒", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addOrderToHistory(orderDetails: OrderDetails) {
        databaseReference.child("user").child(userId).child("BuyHistory").child(orderDetails.itemPushKey!!).setValue(orderDetails)
    }

    private fun removeItemFromCart() {
        val cartItemsReference = databaseReference.child("user").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until serviceItemPrice.size) {
            // Clean price string to ensure we have an integer (remove non-numeric characters)
            val price = serviceItemPrice[i].replace("[^\\d.]".toRegex(), "").toIntOrNull() ?: 0
            val quantity = serviceItemQuantities[i]
            totalAmount += price * quantity
        }
        return totalAmount
    }

    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            userId = user.uid
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val names = snapshot.child("name").getValue(String::class.java) ?: ""
                        val addresses = snapshot.child("address").getValue(String::class.java) ?: ""
                        val phones = snapshot.child("phone").getValue(String::class.java) ?: ""

                        binding.apply {
                            name.setText(names)
                            address.setText(addresses)
                            phone.setText(phones)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun makePayment() {
        Checkout.preload(applicationContext)
        val co = Checkout()
        co.setKeyID("rzp_test_mPu3UZnb8l0E01") // Uncomment and use your actual key

        try {
            val options = JSONObject()
            options.put("name", "DarziVid")
            options.put("description", "Charging for services")
            options.put("image", "http://example.com/image/rzp.jpg")
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")

            // Convert total amount to paise (multiply by 100)
            val amountInPaise = (calculateTotalAmount() * 100).toString()
            options.put("amount", amountInPaise)

            val prefill = JSONObject()
            prefill.put("name", name)
            prefill.put("email", "darzivid@gmail.com")
            prefill.put("contact", phone)

            options.put("prefill", prefill)

            co.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        placeOrder() // Only place order after successful payment
    }

     override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Toast.makeText(this, "Payment Failed: ${p1 ?: "Unknown error"}", Toast.LENGTH_SHORT).show()

    }
}
