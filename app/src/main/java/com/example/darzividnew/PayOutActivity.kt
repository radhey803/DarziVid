package com.example.darzividnew

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.example.darzividnew.Fragment.NotificationBottomFragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.darzividnew.Fragment.CongratsBottomSheet
import com.example.darzividnew.Model.OrderDetails
import com.example.darzividnew.databinding.ActivityPayOutBinding
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

    // Flags for missing profile info
    private var isNameMissing = false
    private var isAddressMissing = false
    private var isPhoneMissing = false

    // Payment method selection
    private var selectedPaymentMethod: String = "COD"

    override fun onCreate(savedInstanceState: Bundle?) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()

        val intent = intent
        serviceItemName = intent.getStringArrayListExtra("serviceItemName") ?: ArrayList()
        serviceItemPrice = intent.getStringArrayListExtra("serviceItemPrice") ?: ArrayList()
        serviceItemImage = intent.getStringArrayListExtra("serviceItemImage") ?: ArrayList()
        serviceItemDescription = intent.getStringArrayListExtra("serviceItemDescription") ?: ArrayList()
        serviceItemIngredient = intent.getStringArrayListExtra("serviceItemIngredient") ?: ArrayList()
        serviceItemQuantities = intent.getIntegerArrayListExtra("serviceItemQuantities") ?: ArrayList()

        totalAmount = calculateTotalAmount().toString() + " ₹"
        binding.totalAmount.setText(totalAmount)
        binding.totalAmount.isFocusable = false
        binding.totalAmount.isClickable = false

        setUserData()

        // Handle payment method selection
        binding.paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = when (checkedId) {
               R.id.radioCashOnDelivery -> "COD"
                R.id.radioOnlinePayment -> "OTHER"
                else -> "OTHER"
            }
        }

        binding.PlaceMyOrder.setOnClickListener {
            name = binding.name.text.toString().trim()
            address = binding.address.text.toString().trim()
            phone = binding.phone.text.toString().trim()

            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Please Enter All The Details ", Toast.LENGTH_SHORT).show()
            } else {
                if (selectedPaymentMethod == "COD") {
                    Toast.makeText(this, "Order Placed with Cash on Delivery", Toast.LENGTH_SHORT).show()
                    placeOrder()
                } else {
                    makePayment()
                }
            }
        }
    }

    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            userId = user.uid
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val names = snapshot.child("name").getValue(String::class.java)
                        val addresses = snapshot.child("address").getValue(String::class.java)
                        val phones = snapshot.child("phone").getValue(String::class.java)

                        binding.apply {
                            name.setText(names ?: "")
                            address.setText(addresses ?: "")
                            phone.setText(phones ?: "")
                        }

                        isNameMissing = names.isNullOrBlank()
                        isAddressMissing = addresses.isNullOrBlank()
                        isPhoneMissing = phones.isNullOrBlank()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until serviceItemPrice.size) {
            val price = serviceItemPrice[i].replace("[^\\d.]".toRegex(), "").toIntOrNull() ?: 0
            val quantity = serviceItemQuantities[i]
            totalAmount += price * quantity
        }
        return totalAmount
    }

    private fun makePayment() {
        Checkout.preload(applicationContext)
        val co = Checkout()
        co.setKeyID("rzp_test_mPu3UZnb8l0E01")

        try {
            val options = JSONObject()
            options.put("name", "DarziVid")
            options.put("description", "Charging for services")
            options.put("image", "http://example.com/image/rzp.jpg")
            options.put("theme.color", "#284F78")
            options.put("currency", "INR")
            options.put("amount", (calculateTotalAmount() * 100).toString())

            val prefill = JSONObject()
            prefill.put("name", name)
            prefill.put("email", "darzivid@gmail.com")
            prefill.put("contact", phone)

            options.put("prefill", prefill)

            co.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        placeOrder()
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Toast.makeText(this, "Payment Failed: ${p1 ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("OrderDetails").push().key

        val orderDetails = OrderDetails(
            userId, name, serviceItemName, serviceItemPrice, serviceItemImage, serviceItemQuantities,
            address, totalAmount, phone, time, itemPushKey, false, false
        )

        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails).addOnSuccessListener {

            val bottomSheetDialog = CongratsBottomSheet()
            bottomSheetDialog.show(supportFragmentManager, "CongratsBottomSheet")
            showNotification("Order Placed ✅", "Your order was placed successfully.", true)






            removeItemFromCart()
            addOrderToHistory(orderDetails)

            if (isNameMissing || isAddressMissing || isPhoneMissing) {
                val updatedUserData = hashMapOf<String, Any>(
                    "name" to name,
                    "address" to address,
                    "phone" to phone
                )

                val userRef = databaseReference.child("user").child(userId)
                userRef.updateChildren(updatedUserData).addOnSuccessListener {

                    Toast.makeText(this, "Profile updated during checkout", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Profile update failed ", Toast.LENGTH_SHORT).show()
                }
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to order ", Toast.LENGTH_SHORT).show()
            showNotification("Order Failed ❌", "Something went wrong. Please try again.", false)

        }
    }

    private fun addOrderToHistory(orderDetails: OrderDetails) {
        databaseReference.child("user").child(userId).child("BuyHistory").child(orderDetails.itemPushKey!!)
            .setValue(orderDetails)
    }

    private fun removeItemFromCart() {
        val cartItemsReference = databaseReference.child("user").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }
}

@SuppressLint("MissingPermission")
private fun PayOutActivity.showNotification(title: String, message: String, isSuccess: Boolean) {
    val channelId = "order_channel_id"
    val channelName = "Order Notifications"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(if (isSuccess) R.drawable.right else R.drawable.sademoji) // Provide suitable icons
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(this)) {
        notify(System.currentTimeMillis().toInt(), builder.build())
    }


}
