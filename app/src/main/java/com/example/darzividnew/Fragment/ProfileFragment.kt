package com.example.darzividnew.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.darzividnew.LoginActivity
import com.example.darzividnew.Model.UserModel
import com.example.darzividnew.StartActivity
import com.example.darzividnew.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private var isEditing = false  // Track edit mode

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        setUserData()

        binding.apply {
            // Disable fields initially
            toggleEditMode(false)

            editButton.setOnClickListener {
                isEditing = !isEditing
                toggleEditMode(isEditing)
            }

            saveInfoButton.setOnClickListener {
                saveProfile()
            }
        }

       binding.logoutbtn.setOnClickListener {

           auth.signOut()
           val intent = Intent(requireContext(), StartActivity::class.java)
           startActivity(intent)
           requireActivity().finish()



       }
        return binding.root


    }

    private fun toggleEditMode(enable: Boolean) {
        binding.apply {
            name.isEnabled = enable
            email.isEnabled = enable
            address.isEnabled = enable
            phone.isEnabled = enable

            if (enable) {
                name.requestFocus() // Set focus when editing
                editButton.text = "Cancel"
                saveInfoButton.visibility = View.VISIBLE  // Show Save Button
            } else {
                editButton.text = "Edit"
                saveInfoButton.visibility = View.GONE  // Hide Save Button
            }
        }
    }

    private fun saveProfile() {
        val name = binding.name.text.toString()
        val email = binding.email.text.toString()
        val address = binding.address.text.toString()
        val phone = binding.phone.text.toString()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("user").child(userId)
            val userData = hashMapOf(
                "name" to name,
                "address" to address,
                "email" to email,
                "phone" to phone
            )

            userReference.setValue(userData).addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated Successfully 😊", Toast.LENGTH_SHORT).show()
                toggleEditMode(false)  // Disable editing after saving
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Profile Update Failed 😒", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if (userProfile != null) {
                            binding.apply {
                                name.setText(userProfile.name)
                                address.setText(userProfile.address)
                                email.setText(userProfile.email)
                                phone.setText(userProfile.phone)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load profile 😔", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
