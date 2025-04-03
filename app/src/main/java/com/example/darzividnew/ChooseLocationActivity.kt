package com.example.darzividnew

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.darzividnew.databinding.ActivityChooseLocationBinding

class ChooseLocationActivity : AppCompatActivity() {
    private val binding: ActivityChooseLocationBinding by lazy {
        ActivityChooseLocationBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val locationList = arrayOf("Ahmedabad", "Amreli", "Anand", "Bharuch",
            "Bhavnagar", "Bhuj", "Botad", "Dahod",
            "Gandhidham", "Gandhinagar", "Godhra",
            "Jamnagar", "Junagadh", "Morbi", "Nadiad",
            "Navsari", "Palanpur", "Porbandar", "Rajkot",
            "Surat", "Surendranagar", "Vadodara", "Valsad", "Vapi")
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1,locationList)
        val autoCompleteTextView = binding.listOfLocation
        autoCompleteTextView.setAdapter(adapter)

        binding.listOfLocation.setOnItemClickListener { adapterView, view, i, l ->
            var selectedLocation = adapterView.getItemAtPosition(i).toString()
            selectedLocation = binding.listOfLocation.text.toString()

            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("location", selectedLocation)
            startActivity(intent)
        }


    }
}