package com.tdi.tmaps

import android.Manifest
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.aemerse.iap.Security
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import com.tdi.tmaps.Interface.IRecyclerItemClickListener
import com.tdi.tmaps.databinding.ActivityMainBinding
import com.tdi.tmaps.model.User
import com.tdi.tmaps.service.MyLocationReceiver
import com.tdi.tmaps.utils.Common
import com.tdi.tmaps.viewHolder.IFirebaseLoadDone
import com.tdi.tmaps.viewHolder.UserViewHolder
import com.tdi.tmaps.viewHolder.WrapContentLinearLayoutManager
import java.io.IOException


class MainActivity : AppCompatActivity(), IFirebaseLoadDone {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding:ActivityMainBinding
    var adapter: FirebaseRecyclerAdapter<User, UserViewHolder>?=null
    private var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>?=null
    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var billingClient: BillingClient
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    var purchase :Boolean ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView


        setSupportActionBar(binding.appBarMain.toolbar)

        val toggle = ActionBarDrawerToggle(this,drawerLayout,binding.appBarMain.toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val header = navView.getHeaderView(0)
        val userEmail = header.findViewById<View>(R.id.user_email) as TextView
        userEmail.text = Common.loggedUser!!.email!!


        preferences = getSharedPreferences("sub", MODE_PRIVATE)
        editor = preferences.edit()


        binding.appBarMain.fab.setOnClickListener {
            startActivity(Intent(this@MainActivity,PeopleActivity::class.java))
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.peopleActivity, R.id.friend_request, R.id.Map,R.id.subscribe,R.id.settings
            ), drawerLayout
        )

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.peopleActivity -> {
                    startActivity(Intent(this,PeopleActivity::class.java))
                }
                R.id.friend_request -> {
                    startActivity(Intent(this,FriendRequestActivity::class.java))
                }
                R.id.Map -> {
                    if (!preferences.getBoolean("isBought",false)) {
                        Toast.makeText(this, "Subscribe", Toast.LENGTH_SHORT).show()
                    }else{
                        startActivity(Intent(this,MapsActivity::class.java))}}
                R.id.settings -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                }
            }
            true
        }


        val searchBar = binding.appBarMain.mainContent.searchBar
        val friendListRecycler = binding.appBarMain.mainContent.friendListRecycler

        searchBar.setCardViewElevation(10)
        searchBar.addTextChangeListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val suggest = ArrayList<String>()
                for (search in suggestList)
                    if (search.lowercase().contentEquals(searchBar.text.lowercase())) suggest.add(search)

                searchBar.lastSuggestions = suggest

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean){
                if (!enabled)
                {
                    //close search
                    if (adapter !=null)
                        friendListRecycler.adapter = adapter
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

            override fun onButtonClicked(buttonCode: Int) {
                TODO("Not yet implemented")
            }
        })

        friendListRecycler.setHasFixedSize(true)
        val layoutManager = WrapContentLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        friendListRecycler.layoutManager = layoutManager
        friendListRecycler.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))


        iFirebaseLoadDone = this
        loadFriendList()
        loadSearchData()
        updateLocation()


        //Initialize a BillingClient with PurchasesUpdatedListener onCreate method
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult, mutablePurchaseList ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && mutablePurchaseList != null) {
                    for (purchase in mutablePurchaseList) {
                        verifySubPurchase(purchase)
                    }
                }
            }.build()

        //start the connection after initializing the billing client
        establishConnection()
    }


    fun establishConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished( billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts()
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
        val productList = listOf( //Product 1 = index is 0
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("sub_example")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(
            params
        ) { billingResult: BillingResult?, productDetailsList: List<ProductDetails> ->
            // Process the result
            for (productDetails in productDetailsList) {
                if (productDetails.productId == "sub_example") {
                    val subDetails: List<*> =
                        productDetails.subscriptionOfferDetails!!
                    Log.d("testOffer", subDetails[0].toString())
                    //binding.appBarMain.fab.setOnClickListener { launchPurchaseFlow(productDetails) }
                    val sub =findViewById<View>(R.id.subscribe)
                    sub.setOnClickListener { launchPurchaseFlow(productDetails) }

                }
            }
        }

    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        assert(productDetails.subscriptionOfferDetails != null)
        val productDetailsParamsList = listOf<ProductDetailsParams>(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient.launchBillingFlow(this@MainActivity, billingFlowParams)
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
                //user prefs to set premium
                Toast.makeText(this@MainActivity, "You are a premium user now", Toast.LENGTH_SHORT)
                    .show()
                //Setting premium to 1
                // 1 - premium
                // 0 - no premium
                editor.putBoolean("isBought",true)
                //prefs.setPremium(1)
            }
        }
        Log.d(TAG, "Purchase Token: " + purchases.purchaseToken)
        Log.d(TAG, "Purchase Time: " + purchases.purchaseTime)
        Log.d(TAG, "Purchase OrderID: " + purchases.orderId)
    }


    fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient = billingClient
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                checkSubscription()
            }
            override fun onBillingSetupFinished( billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult: BillingResult, list: List<Purchase> ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d("testOffer", list.size.toString() + " size")
                            if (list.isNotEmpty()) {
                                purchase = true
                                editor.putBoolean("isBought",true)
                                editor.apply()

                                // set true to activate premium feature
                                var i = 0
                                for (purchase in list) {
                                    //Here you can manage each product, if you have multiple subscription
                                    Log.d(
                                        "testOffer",
                                        purchase.originalJson
                                    ) // Get to see the order information
                                    Log.d("testOffer", " index$i")
                                    i++
                                }
                            } else {
                                purchase = false
                                editor.putBoolean("isBought",false)
                                editor.apply()
                                // set false to de-activate premium feature
                            }
                        }
                    }
                }
            }
        })
    }


    private fun updateLocation() {
        buildLocationRequest()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {return ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0)}
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent())
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this,MyLocationReceiver::class.java)
        intent.action = MyLocationReceiver.ACTION
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildLocationRequest() {
        locationRequest= LocationRequest.create().apply {
            smallestDisplacement = 10f
            fastestInterval = 3000
            interval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        userList.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children){
                    val user = userSnapshot.getValue(User::class.java)
                    lstUserEmail.add(user!!.email!!)
                }
                iFirebaseLoadDone.onFirebaseLoadUserDone(lstUserEmail)
            }

            override fun onCancelled(error: DatabaseError) {
                iFirebaseLoadDone.onFirebaseLoadFailed(error.message)
            }
        })
    }

    private fun startSearch(search_string:String)
    {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object :FirebaseRecyclerAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user,parent,false)

                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object :IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle("TMap")
                        alertDialog.setMessage(model.email)
                        alertDialog.setPositiveButton("Track") {_,_ ->
                            checkSubscription()
                            if (preferences.getBoolean("isBought",true)) {
                                Common.trackingUser = model
                                startActivity(Intent(this@MainActivity, MapsActivity::class.java))
                            }else {
                                Toast.makeText(this@MainActivity, "Subscribe", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }

                        alertDialog.setNeutralButton("Unfriend"){_,_->
                            deleteUserFromFriendContact(model)
                            deleteFromAcceptList(model)
                        }
                        alertDialog.setNegativeButton("Close"){DialogInterface,_ -> DialogInterface.dismiss()}
                        alertDialog.show()

                    }
                })
            }
        }

        searchAdapter!!.startListening()
        binding.appBarMain.mainContent.friendListRecycler.adapter = searchAdapter
    }

    private fun loadFriendList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object :FirebaseRecyclerAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user,parent,false)

                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object :IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle("TMap")
                        alertDialog.setMessage(model.email)
                        alertDialog.setPositiveButton("Track") { _, _ ->
                            checkSubscription()
                            if (purchase == false){
                                Toast.makeText(this@MainActivity, "Subscribe", Toast.LENGTH_SHORT)
                                    .show()
                            }else{
                                Common.trackingUser = model
                                startActivity(Intent(this@MainActivity, MapsActivity::class.java))}
                        }

                        alertDialog.setNeutralButton("Unfriend"){_,_->
                            deleteUserFromFriendContact(model)
                            deleteFromAcceptList(model)
                        }
                        alertDialog.setNegativeButton("Close"){DialogInterface,_ -> DialogInterface.dismiss()}
                        alertDialog.show()

                    }

                })
            }

        }

        adapter!!.startListening()
        binding.appBarMain.mainContent.friendListRecycler.adapter = adapter
    }


    private fun deleteUserFromFriendContact(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(model.uid!!)
            .child(Common.ACCEPT_LIST)
        acceptList.child(Common.loggedUser!!.uid!!).removeValue()
    }

    private fun deleteFromAcceptList(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)
        acceptList.child(model.uid!!).removeValue()
    }


    override fun onStop() {
        if (adapter != null)
            adapter!!.stopListening()
        if (searchAdapter != null)
            searchAdapter!!.stopListening()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null)
            adapter!!.startListening()
        if (searchAdapter != null)
            searchAdapter!!.startListening()

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        verifySubPurchase(purchase)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        billingClient.endConnection()
        super.onDestroy()
    }

    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        if (lstEmail.size <= 1)
            binding.appBarMain.textView.text = lstEmail.size.toString()+" Friend"
        else
            binding.appBarMain.textView.text = lstEmail.size.toString()+" Friends"

        binding.appBarMain.mainContent.searchBar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

}