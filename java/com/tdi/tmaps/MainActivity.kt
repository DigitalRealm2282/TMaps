package com.tdi.tmaps

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
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
import com.android.billingclient.api.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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
import com.tdi.tmaps.BuildConfig.VERSION_NAME
import com.tdi.tmaps.databinding.ActivityMainBinding
import com.tdi.tmaps.iInterface.IRecyclerItemClickListener
import com.tdi.tmaps.model.User
import com.tdi.tmaps.service.MyLocationReceiver
import com.tdi.tmaps.utils.Common
import com.tdi.tmaps.viewHolder.IFirebaseLoadDone
import com.tdi.tmaps.viewHolder.UserViewHolder
import com.tdi.tmaps.viewHolder.WrapContentLinearLayoutManager
import dmax.dialog.SpotsDialog
import java.util.*

class MainActivity : AppCompatActivity(), IFirebaseLoadDone {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var adapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    private var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    //var suggestList:List<String> = ArrayList()
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var billingClient: BillingClient
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferences2: SharedPreferences
    private lateinit var editor2: SharedPreferences.Editor
    private lateinit var preferences3: SharedPreferences
    private lateinit var editor3: SharedPreferences.Editor
    private lateinit var resource: Resources
    private lateinit var prefCurrentLang: SharedPreferences
    private lateinit var prefBG :SharedPreferences
    private var loading : SpotsDialog ?= null
    var context: Context? = null
    private var mRewardedAd: RewardedAd? = null
    var text = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        resource = resources

        checkLang()
        checkGps()
        checkSubscription()
        //user changeable values preferences
        preferences = getSharedPreferences("sub", MODE_PRIVATE)
        editor = preferences.edit()
        preferences2 = getSharedPreferences("live", MODE_PRIVATE)
        editor2 = preferences2.edit()
        preferences3 = getSharedPreferences("acc_switch", MODE_PRIVATE)
        editor3 = preferences3.edit()
        prefBG = getSharedPreferences("BG", MODE_PRIVATE)

        checkBG()

        loading = SpotsDialog(this,resource.getString(R.string.loading),R.style.Custom)
        loading?.setCancelable(true)
        loading?.show()

        setSupportActionBar(binding.appBarMain.toolbar)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.appBarMain.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val header = navView.getHeaderView(0)
        val userEmail = header.findViewById<View>(R.id.user_email) as TextView
        userEmail.text = Common.loggedUser!!.email!!

        val premUser = header.findViewById<View>(R.id.prem) as TextView

        binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

//        //user changeable values preferences
//        preferences = getSharedPreferences("sub", MODE_PRIVATE)
//        editor = preferences.edit()
//        preferences2 = getSharedPreferences("live", MODE_PRIVATE)
//        editor2 = preferences2.edit()
//        preferences3 = getSharedPreferences("acc_switch", MODE_PRIVATE)
//        editor3 = preferences3.edit()
//        prefBG = getSharedPreferences("BG", MODE_PRIVATE)

        if (!preferences.getBoolean("isBought", false)) {
            premUser.text = "TMap"
        } else {
            premUser.text = "TMap Premium"
            premUser.setTextColor(resources.getColor(R.color.golden, null))
        }

        binding.appBarMain.fab.setOnClickListener {
            startActivity(Intent(this@MainActivity, PeopleActivity::class.java))
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.peopleActivity, R.id.friend_request, R.id.Map, R.id.subscribe, R.id.settings
            ),
            drawerLayout)
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.peopleActivity, R.id.friend_request, R.id.Map, R.id.subscribe, R.id.settings, R.id.arMap
//            ),
//            drawerLayout
//        )

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.peopleActivity -> {
                    startActivity(Intent(this, PeopleActivity::class.java))
                }
                R.id.friend_request -> {
                    startActivity(Intent(this, FriendRequestActivity::class.java))
                }
                R.id.Map -> {
                    checkSubscription()
                    if (!preferences.getBoolean("isBought", false)) {
                        Toast.makeText(this, resource.getString(R.string.sub), Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this@MainActivity, MapsActivity::class.java)
                        startActivity(intent)
                    }
                }
                R.id.settings -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                }
                R.id.subscribe -> {
                    startActivity(Intent(this, SubActivity::class.java))
                }
//                R.id.arMap -> {
//                    if (!preferences.getBoolean("isBought", false)) {
//                        Toast.makeText(this@MainActivity,"Subscribe",Toast.LENGTH_SHORT).show()
//                    } else {
//                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
//                            ActivityCompat.checkSelfPermission(this@MainActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                            && ActivityCompat.checkSelfPermission(this@MainActivity,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                            ActivityCompat.requestPermissions(
//                                this@MainActivity,
//                                arrayOf(
//                                    Manifest.permission.CAMERA,
//                                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                                ),
//                                0
//                            )
//                        }else{
//                            startActivity(Intent(this, PoiBrowserActivity::class.java))
//                        }
//                    }
//                }
                R.id.evMap -> {
                    //AD
//                    initiateAd()
//                    runAd()
                    startActivity(Intent(this, ServiceMapsActivity::class.java))

                }

            }
            true
        }

        val searchBar = binding.appBarMain.mainContent.searchBar
        val friendListRecycler = binding.appBarMain.mainContent.friendListRecycler

        searchBar.setTextColor(R.color.black)
        searchBar.setCardViewElevation(10)
        searchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                val suggest = ArrayList<String>()
//                for (search in suggestList)
//                    if (search.lowercase().contentEquals(searchBar.text.lowercase())) suggest.add(search)
//
//                searchBar.lastSuggestions = suggest
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        searchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {
                if (!enabled) {
                    // close search
                    if (adapter != null)
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
        //val layoutManager = LinearLayoutManager(this@MainActivity)
        val layoutManager = WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        friendListRecycler.layoutManager = layoutManager
        friendListRecycler.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        iFirebaseLoadDone = this
        loadFriendList()
        loadSearchData()
        if (preferences2.getBoolean("liveMode", true))
            updateLocation()

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
    }

    @SuppressLint("VisibleForTests")
    private fun initiateAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,"ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mRewardedAd = null
//                Toast.makeText(this@MainActivity,"Check Internet Connection", Toast.LENGTH_SHORT).show()
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                mRewardedAd = rewardedAd
            }
        })
    }

    private fun runAd() {

        val adDialog = AlertDialog.Builder(this@MainActivity)
        adDialog.setTitle(R.string.app_name)
        adDialog.setMessage("Watch ad & fuel your car")
        adDialog.setPositiveButton(R.string.accept){ _,_ ->
            if (mRewardedAd != null) {
                mRewardedAd?.show(this) {
                    var rewardAmount = it.amount
                    var rewardType = it.type
                    Log.d(TAG, "User earned the reward.")
                    startActivity(Intent(this, ServiceMapsActivity::class.java))
                }
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.")
                Toast.makeText(this@MainActivity,"Check Internet Connection", Toast.LENGTH_SHORT).show()

            }

        }
        adDialog.setNegativeButton(R.string.cancel){d,_ -> d.dismiss() }
        adDialog.show()


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkBG() {
        if (prefBG.getString("background", "normal")=="normal"){
            binding.appBarMain.appMain.background = resources.getDrawable(R.mipmap.bg,null)
            binding.appBarMain.mainContent.contMain.background = resources.getDrawable(R.mipmap.bg,null)
        }else if (prefBG.getString("background", "leaf")=="leaf"){
            binding.appBarMain.appMain.background = resources.getDrawable(R.mipmap.greenleafbg,null)
            binding.appBarMain.mainContent.contMain.background = resources.getDrawable(R.mipmap.greenleafbg,null)
        }else if (prefBG.getString("background", "car")=="car"){
            binding.appBarMain.appMain.background = resources.getDrawable(R.mipmap.car,null)
            binding.appBarMain.mainContent.contMain.background = resources.getDrawable(R.mipmap.car,null)
        }else if (prefBG.getString("background", "green")=="green"){
            binding.appBarMain.appMain.background = resources.getDrawable(R.mipmap.planegreenbg,null)
            binding.appBarMain.mainContent.contMain.background = resources.getDrawable(R.mipmap.planegreenbg,null)
        }else{
            binding.appBarMain.appMain.background = resources.getDrawable(R.mipmap.planegreenbg,null)
            binding.appBarMain.mainContent.contMain.background = resources.getDrawable(R.mipmap.planegreenbg,null)
        }
    }

    private fun checkGps(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            binding.appBarMain.warningButton.setOnClickListener {
                val intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            binding.appBarMain.warningButton.visibility = View.GONE
        }
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
                Toast.makeText(this@MainActivity, "You are a premium user now", Toast.LENGTH_SHORT)
                    .show()
                // Setting premium to 1
                // 1 - premium
                // 0 - no premium
                editor.putBoolean("isBought", true)
                editor.apply()
                // prefs.setPremium(1)
            }
        }
        Log.d(TAG, "Purchase Token: " + purchases.purchaseToken)
        Log.d(TAG, "Purchase Time: " + purchases.purchaseTime)
        Log.d(TAG, "Purchase OrderID: " + purchases.orderId)
    }

    fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { _: BillingResult?, _: List<Purchase?>? -> }
            .build()
        val finalBillingClient = billingClient
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                //checkSubscription()
            }
            @Suppress("NAME_SHADOWING")
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult: BillingResult, list: List<Purchase> ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d("testOffer", list.size.toString() + " size")
                            if (list.isNotEmpty()) {
                                editor.putBoolean("isBought", true)
                                editor.apply()

                                // set true to activate premium feature
                                var i = 0
                                for (purchase in list) {
                                    // Here you can manage each product, if you have multiple subscription
                                    Log.d(
                                        "testOffer",
                                        purchase.originalJson
                                    ) // Get to see the order information
                                    Log.d("testOffer", " index$i")
                                    i++
                                }
                            } else {
                                editor.putBoolean("isBought", false)
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
        if (preferences3.getString("accStatus", "High") == "High") {
            buildLocationRequest()
        } else if (preferences3.getString("accStatus", "Balanced") == "Balanced") {
            buildLocationRequestBalanced()
        } else if (preferences3.getString("accStatus", "Low") == "Low") {
            buildLocationRequestLow()
        } else {
            buildLocationRequest()
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { return ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0) }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MyLocationReceiver::class.java)
        intent.action = MyLocationReceiver.ACTION
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private fun buildLocationRequest() {
//        if (preferences3.getString("accStatus", "High") == "High") {
//            locationRequest = LocationRequest.Builder(5000L)
//                .setMinUpdateDistanceMeters(10F)
//                .setMinUpdateIntervalMillis(3000L)
//                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
//                .setIntervalMillis(5000L)
//                .build()
//        } else if (preferences3.getString("accStatus", "Balanced") == "Balanced") {
//            locationRequest = LocationRequest.Builder(5000L)
//                .setMinUpdateDistanceMeters(10F)
//                .setMinUpdateIntervalMillis(3000L)
//                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
//                .setIntervalMillis(5000L)
//                .build()
//        } else if (preferences3.getString("accStatus", "Low") == "Low") {
//            locationRequest = LocationRequest.Builder(5000L)
//                .setMinUpdateDistanceMeters(10F)
//                .setMinUpdateIntervalMillis(3000L)
//                .setPriority(Priority.PRIORITY_LOW_POWER)
//                .setIntervalMillis(5000L)
//                .build()
//        } else {
//            locationRequest = LocationRequest.Builder(5000L)
//                .setMinUpdateDistanceMeters(10F)
//                .setMinUpdateIntervalMillis(3000L)
//                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
//                .setIntervalMillis(5000L)
//                .build()
//        }
        locationRequest = LocationRequest.Builder(5000L)
            .setMinUpdateDistanceMeters(10F)
            .setMinUpdateIntervalMillis(3000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setIntervalMillis(5000L)
            .build()
    }
    private fun buildLocationRequestBalanced() {
        locationRequest = LocationRequest.Builder(5000L)
            .setMinUpdateDistanceMeters(10F)
            .setMinUpdateIntervalMillis(3000L)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setIntervalMillis(5000L)
            .build()
    }
    private fun buildLocationRequestLow() {
        locationRequest = LocationRequest.Builder(5000L)
            .setMinUpdateDistanceMeters(10F)
            .setMinUpdateIntervalMillis(3000L)
            .setPriority(Priority.PRIORITY_LOW_POWER)
            .setIntervalMillis(5000L)
            .build()
    }

//        private fun checkFriendList(model: User){
//        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
//            .child(Common.loggedUser!!.uid!!)
//            .child(Common.ACCEPT_LIST)
//        //check friend list
//        acceptList.orderByKey().equalTo(model.uid)
//            .addListenerForSingleValueEvent(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.value == null)
//                        Toast.makeText(this@MainActivity,"not in friend list",Toast.LENGTH_SHORT).show()
//                    else
//                        Toast.makeText(this@MainActivity,"Already in friend list",Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(this@MainActivity,error.message,Toast.LENGTH_SHORT).show()
//                }
//            })
//    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        userList.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
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

    private fun startSearch(search_string: String) {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object : FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user, parent, false)

                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object : IRecyclerItemClickListener {
                    override fun onItemClickListener(view: View, position: Int) {
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle("TMap")
                        alertDialog.setMessage(model.email)
                        alertDialog.setPositiveButton(resource.getString(R.string.track)) { _, _ ->
                            checkSubscription()
                            if (!preferences.getBoolean("isBought", false)) {
                                Toast.makeText(this@MainActivity, "Subscribe", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                                Common.trackingUser = model
                                startActivity(intent)
                            }
                        }

                        alertDialog.setNeutralButton(resource.getString(R.string.unfriend)) { _, _ ->
                            deleteUserFromFriendContact(model)
                            deleteFromAcceptList(model)
                        }
                        alertDialog.setNegativeButton(resource.getString(R.string.close)) { DialogInterface, _ -> DialogInterface.dismiss() }
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

        adapter = object : FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user, parent, false)

                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object : IRecyclerItemClickListener {
                    override fun onItemClickListener(view: View, position: Int) {
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle("TMap")
                        alertDialog.setMessage(model.email)
                        alertDialog.setPositiveButton(resource.getString(R.string.track)) { _, _ ->
                            checkSubscription()
                            if (!preferences.getBoolean("isBought", false)) {
                                Toast.makeText(this@MainActivity, "Subscribe", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                                Common.trackingUser = model
                                startActivity(intent)
                            }
                        }

                        alertDialog.setNeutralButton(resource.getString(R.string.unfriend)) { _, _ ->
                            deleteUserFromFriendContact(model)
                            deleteFromAcceptList(model)
                        }
                        alertDialog.setNegativeButton(resource.getString(R.string.close)) { DialogInterface, _ -> DialogInterface.dismiss() }
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

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isGpsEnabled && isNetworkEnabled) {
            binding.appBarMain.warningButton.visibility = View.GONE
        } else {
            binding.appBarMain.warningButton.visibility = View.VISIBLE
            binding.appBarMain.warningButton.setOnClickListener {
                val intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }

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
        checkBG()
        checkLang()
    }

    override fun onDestroy() {
        if (adapter != null)
            adapter!!.stopListening()
        if (searchAdapter != null)
            searchAdapter!!.stopListening()
        billingClient.endConnection()
        super.onDestroy()
    }

    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        loading?.dismiss()
        if(lstEmail.isEmpty())
            binding.appBarMain.textView.visibility = GONE
        else if (lstEmail.size == 1)
            binding.appBarMain.textView.text = lstEmail.size.toString() + " " + resource.getString(R.string.friend)
        else
            binding.appBarMain.textView.text = lstEmail.size.toString() + " " + resource.getString(R.string.friends)

        binding.appBarMain.mainContent.searchBar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        loading?.dismiss()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context.changeLocale(text))
    }

    private fun Context.changeLocale(language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = this.resources.configuration
        config.setLocale(locale)
        return createConfigurationContext(config)
    }

    private fun checkLang() {
        prefCurrentLang = getSharedPreferences("currentLang", MODE_PRIVATE)

        resource = resources
        if (prefCurrentLang.getString("myLang", "en") == "en") {
            context = LocaleHelper.setLocale(this@MainActivity, "en")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "en"
        } else if (prefCurrentLang.getString("myLang", "ar") == "ar") {
            context = LocaleHelper.setLocale(this@MainActivity, "ar")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "ar"
        } else if (prefCurrentLang.getString("myLang", "fr") == "fr") {
            context = LocaleHelper.setLocale(this@MainActivity, "fr")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "fr"
        } else if (prefCurrentLang.getString("myLang", "ja") == "ja") {
            context = LocaleHelper.setLocale(this@MainActivity, "ja")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "ja"
        } else if (prefCurrentLang.getString("myLang", "zh") == "zh") {
            context = LocaleHelper.setLocale(this@MainActivity, "zh")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "zh"
        } else if (prefCurrentLang.getString("myLang", "ms") == "ms") {
            context = LocaleHelper.setLocale(this@MainActivity, "ms")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "ms"
        } else if (prefCurrentLang.getString("myLang", "ru") == "ru") {
            context = LocaleHelper.setLocale(this@MainActivity, "ru")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "ru"
        } else if (prefCurrentLang.getString("myLang", "es") == "es") {
            context = LocaleHelper.setLocale(this@MainActivity, "es")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "es"
        } else if (prefCurrentLang.getString("myLang", "de") == "de") {
            context = LocaleHelper.setLocale(this@MainActivity, "de")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "de"
        } else if (prefCurrentLang.getString("myLang", "it") == "it") {
            context = LocaleHelper.setLocale(this@MainActivity, "it")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "it"
        } else {
            context = LocaleHelper.setLocale(this@MainActivity, "en")
            resource = context!!.resources
            title = resource.getString(R.string.title_activity_main)
            binding.navView.invalidate()
            val res = binding.navView.menu
            val searchName = res.findItem(R.id.peopleActivity)
            searchName.title = resource.getString(R.string.search_home)
            val friendName = res.findItem(R.id.friend_request)
            friendName.title = resource.getString(R.string.friend_req)
            val mapName = res.findItem(R.id.Map)
            mapName.title = resource.getString(R.string.menu_map)
            val subName = res.findItem(R.id.subscribe)
            subName.title = resource.getString(R.string.sub)
            val sysName = res.findItem(R.id.system)
            sysName.title = resource.getString(R.string.system_setting)
            val settingName = res.findItem(R.id.settings)
            settingName.title = resource.getString(R.string.setting_String)
            binding.version.text = resource.getString(R.string.version) + " " + VERSION_NAME

            text = "en"
        }
    }
}