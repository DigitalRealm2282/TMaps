package com.tdi.tmaps

import android.content.ContentValues
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.tdi.tmaps.databinding.ActivitySubBinding

class SubActivity : AppCompatActivity() {

    private lateinit var billingClient: BillingClient
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: ActivitySubBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("sub", MODE_PRIVATE)
        editor = preferences.edit()
        // Initialize a BillingClient with PurchasesUpdatedListener onCreate method
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult, mutablePurchaseList ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && mutablePurchaseList != null) {
                    for (purchase in mutablePurchaseList) {
                        verifySubPurchase(purchase)
                    }
                }
            }.build()

        // start the connection after initializing the billing client
//        checkSubscription()
//        if (preferences.getBoolean("isBought",true)) {
//            Toast.makeText(this, "Already Subscribed", Toast.LENGTH_SHORT).show()
//        }
        establishConnection()
    }

    fun establishConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts()
                    // showProducts2()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection()
            }
        })
    }

    fun showProducts() {
        val productList = listOf( // Product 1 = index is 0
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("sub_example")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(), // Product 2 = index is 1
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("sub_yearly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(
            params
        ) { _: BillingResult?, productDetailsList: List<ProductDetails> ->
            // Process the result
            for (productDetails in productDetailsList) {
                if (productDetails.productId == "sub_example") {
                    // val subDetails: List<*> = productDetails.subscriptionOfferDetails!!
                    // Log.d("testOffer", subDetails[1].toString())
                    binding.card2M.setOnClickListener { launchPurchaseFlow(productDetails) }
                } else if (productDetails.productId == "sub_yearly") {
                    // val subDetails: List<*> = productDetails.subscriptionOfferDetails!!
                    // Log.d("testOffer", subDetails[1].toString())
                    binding.card3Y.setOnClickListener { launchPurchaseFlow(productDetails) }
                }
            }
        }
    }

    private fun launchPurchaseFlow(productDetails: ProductDetails) {
        assert(productDetails.subscriptionOfferDetails != null)
        val productDetailsParamsList = listOf<BillingFlowParams.ProductDetailsParams>(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(this@SubActivity, billingFlowParams)
        // val billingResult = billingClient.launchBillingFlow(this@SubActivity, billingFlowParams)
    }

    private fun verifySubPurchase(purchases: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchases.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams
        ) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // user prefs to set premium
                Toast.makeText(this@SubActivity, "You are a premium user now", Toast.LENGTH_SHORT)
                    .show()
                // Setting premium to 1
                // 1 - premium
                // 0 - no premium
                editor.putBoolean("isBought", true)
                // editor.apply()
                // prefs.setPremium(1)
            }
        }
        Log.d(ContentValues.TAG, "Purchase Token: " + purchases.purchaseToken)
        Log.d(ContentValues.TAG, "Purchase Time: " + purchases.purchaseTime)
        Log.d(ContentValues.TAG, "Purchase OrderID: " + purchases.orderId)
    }

//    fun checkSubscription() {
//        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
//            .setListener { _: BillingResult?, _: List<Purchase?>? -> }
//            .build()
//        val finalBillingClient = billingClient
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingServiceDisconnected() {
//                checkSubscription()
//            }
//            @Suppress("NAME_SHADOWING")
//            override fun onBillingSetupFinished( billingResult: BillingResult) {
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    finalBillingClient.queryPurchasesAsync(
//                        QueryPurchasesParams.newBuilder()
//                            .setProductType(BillingClient.ProductType.SUBS).build()
//                    ) { billingResult: BillingResult, list: List<Purchase> ->
//                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                            Log.d("testOffer", list.size.toString() + " size")
//                            if (list.isNotEmpty()) {
//                                editor.putBoolean("isBought",true)
//                                editor.apply()
//
//                                // set true to activate premium feature
//                                var i = 0
//                                for (purchase in list) {
//                                    //Here you can manage each product, if you have multiple subscription
//                                    Log.d(
//                                        "testOffer",
//                                        purchase.originalJson
//                                    ) // Get to see the order information
//                                    Log.d("testOffer", " index$i")
//                                    i++
//                                }
//                            } else {
//                                editor.putBoolean("isBought",false)
//                                editor.apply()
//                                // set false to de-activate premium feature
//                            }
//                        }
//                    }
//                }
//            }
//        })
//    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}