package com.example.finalyearprojectdm

data class PurchaseLink(
    val purchaseLinkId: String,
    val providerId: String,
    val partnerSuppliedProvider: Provider,
    val currency: String,
    val totalPrice: Double,
    val url: String
)
