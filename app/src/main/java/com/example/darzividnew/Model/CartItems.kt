package com.example.darzividnew.Model

data class CartItems(
    var   serviceName: String ?=null,
    var   servicePrice: String ?=null,
    var   serviceDescription: String ?=null,
    var   serviceImage: String ?=null,
    var   serviceQuantity: Int ?=null,
    var   servicengredients:String?= null
)
