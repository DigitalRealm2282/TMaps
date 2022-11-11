package com.tdi.tmaps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.*
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tdi.tmaps.databinding.ActivityLoginBinding
import com.tdi.tmaps.model.User
import com.tdi.tmaps.utils.Common.TOKENS
import com.tdi.tmaps.utils.Common.USER_INFO
import com.tdi.tmaps.utils.Common.USER_UID_SAVE_KEY
import com.tdi.tmaps.utils.Common.loggedUser
import io.paperdb.Paper

class LoginActivity : AppCompatActivity() {

    lateinit var userInfo: DatabaseReference
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var binding: ActivityLoginBinding
    //private lateinit var preferences: SharedPreferences
//    companion object{
//        private const val MY_REQ_CODE = 0
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //preferences = getSharedPreferences("rem", MODE_PRIVATE)

        userInfo = FirebaseDatabase.getInstance().getReference(USER_INFO)
        Paper.init(this)

        providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        if (isOnline(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                disclosure()
            } else {
                showSignInOption()
            }
        } else {
            Toast.makeText(this, "Check internet connection", Toast.LENGTH_SHORT).show()
            binding.imageTouch.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    disclosure()
                } else {
                    showSignInOption()
                }
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

//    private fun resetPassword(email: String): Task<Void> {
//        return FirebaseAuth.getInstance().sendPasswordResetEmail(email)
//            .addOnCompleteListener { t ->
//                if (t.isSuccessful) {
//                    Toast.makeText(this@LoginActivity, "Mail sent", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { t -> Toast.makeText(this@LoginActivity,t.message,Toast.LENGTH_SHORT).show() }
//    }

    private fun disclosure() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("GET STARTED")
        alert.setMessage("TMap collects location data to enable live tracking even when the app is closed or not in use.")
        alert.setPositiveButton("Ok") { _, _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getPermissionApi()
            } else {
                getPermission()
            }
        }
        alert.setNegativeButton("Cancel") { DialogInterface, _ ->
            DialogInterface.dismiss()
            finishAndRemoveTask()
        }
        alert.show()
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (grantResults.contains(PackageManager.PERMISSION_DENIED)){
//            Toast.makeText(this@LoginActivity,"Permission are needed for this app to work properly",Toast.LENGTH_SHORT).show()
//            showSignInOption()
//        }else{
//            showSignInOption()
//        }
//    }
    private fun getPermission() {
        Dexter.withContext(this@LoginActivity)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    showSignInOption()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    Toast.makeText(
                        this@LoginActivity,
                        "$p1 Permission are needed for this app to work properly",
                        Toast.LENGTH_SHORT
                    ).show()
                    p1?.continuePermissionRequest()
                }
            }).check()
//    if (ActivityCompat.checkSelfPermission(this@LoginActivity,Manifest.permission.ACCESS_FINE_LOCATION) and
//        ActivityCompat.checkSelfPermission(this@LoginActivity,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//    {
//        ActivityCompat.requestPermissions(this@LoginActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0)
//    }
//
//    val permFine =  ActivityCompat.checkSelfPermission(this@LoginActivity,Manifest.permission.ACCESS_FINE_LOCATION)
//    val permCoarse = ActivityCompat.checkSelfPermission(this@LoginActivity,Manifest.permission.ACCESS_COARSE_LOCATION)
//    onRequestPermissionsResult(0, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), intArrayOf(permCoarse,permFine))
//
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getPermissionApi() {

        Dexter.withContext(this@LoginActivity)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    showSignInOption()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    Toast.makeText(
                        this@LoginActivity,
                        "$p1 Permission are needed for this app to work properly",
                        Toast.LENGTH_SHORT
                    ).show()
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

    private val getAction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        // if(firebaseUser!!.uid != null){
        userInfo.orderByKey()
            .equalTo(firebaseUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.value == null) {
                        // user not exist
                        if (!snapshot.child(firebaseUser.uid).exists()) {
                            loggedUser = User(firebaseUser.uid, firebaseUser.email!!)
                            firebaseUser.sendEmailVerification()
                                .addOnCompleteListener {
                                    //add user to database
                                    userInfo.child(loggedUser!!.uid!!)
                                        .setValue(loggedUser)
                                }
                                .addOnFailureListener {m ->
                                    Toast.makeText(this@LoginActivity,m.message.toString(),Toast.LENGTH_SHORT).show()
                                }
                            // add user to database
                            userInfo.child(loggedUser!!.uid!!)
                                .setValue(loggedUser)
                        }
                    } else {
                        // user exist
                        loggedUser = snapshot.child(firebaseUser.uid)
                            .getValue(User::class.java)!!
                    }

                    // save uid to storage to update location on kill mode
                    Paper.book().write(USER_UID_SAVE_KEY, loggedUser!!.uid.toString())
                    updateToken(firebaseUser)
                    setupUI()
                }
            })
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MY_REQ_CODE){
//
//            val firebaseUser = FirebaseAuth.getInstance().currentUser
//            //if(firebaseUser!!.uid != null){
//            userInfo.orderByKey()
//                .equalTo(firebaseUser!!.uid)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onDataChange(snapshot: DataSnapshot) {
//
//                        if (snapshot.value == null) {
//                            //user not exist
//                            if (!snapshot.child(firebaseUser.uid).exists()) {
//                                loggedUser = User(firebaseUser.uid, firebaseUser.email!!)
//                                userInfo.child(loggedUser!!.uid!!)
//                                    .setValue(loggedUser)
//                            }
//
//                        } else {
//                            //user exist
//                            loggedUser = snapshot.child(firebaseUser.uid)
//                                .getValue(User::class.java)!!
//
//                        }
//
//                        //save uid to storage to update location on kill mode
//                        Paper.book().write(USER_UID_SAVE_KEY, loggedUser!!.uid.toString())
//                        updateToken(firebaseUser)
//                        setupUI()
//
//                    }
//
//                })
//
//        }
//    }

    private fun showSignInOption() {
//        val firebaseUser = FirebaseAuth.getInstance().currentUser
//        if (preferences.getBoolean("rememberMe", true) && firebaseUser != null) {
//            loggedUser = User(firebaseUser.uid, firebaseUser.email!!)
//            updateToken(firebaseUser)
////            Toast.makeText(this@LoginActivity,"remembered",Toast.LENGTH_SHORT).show()
//              setupUI()
//        } else {
//            Toast.makeText(this@LoginActivity,"getAction",Toast.LENGTH_SHORT).show()
        getAction.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAlwaysShowSignInMethodScreen(false)
                .setIsSmartLockEnabled(true)
                // .setTosAndPrivacyPolicyUrls("https://sites.google.com/view/tmap2282/home")
                .setTheme(R.style.Theme_TouchMaps)
                .setLogo(R.mipmap.ic_launcher_round)
                .setAvailableProviders(providers)
                .build()
            )
//        }
    }
// private fun showSignInOption() {
//    val firebaseUser = FirebaseAuth.getInstance().currentUser
//    if (preferences.getBoolean("rememberMe",true) && firebaseUser != null) {
//        loggedUser = User(firebaseUser.uid,firebaseUser.email!!)
//        updateToken(firebaseUser)
//        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
//        finish()
//    }else{
//        startActivityForResult(
//            AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAlwaysShowSignInMethodScreen(true)
//                .setIsSmartLockEnabled(true)
//                //.setTosAndPrivacyPolicyUrls("https://sites.google.com/view/tmap2282/home")
//                .setTheme(R.style.Theme_TouchMaps)
//                .setLogo(R.mipmap.ic_launcher_round)
//                .setAvailableProviders(providers)
//                .build(), MY_REQ_CODE
//        )
//    }
// }

    private fun setupUI() {
        // after all done navigation home
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun updateToken(firebaseUser: FirebaseUser) {
        val tokens = FirebaseDatabase.getInstance()
            .getReference(TOKENS)

        // get Token
        // val token = FirebaseMessaging.getInstance().token.result
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                tokens.child(firebaseUser.uid)
                    .setValue(token)
                //Toast.makeText(this, " Welcome back " + loggedUser?.email, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> Toast.makeText(this, e.message, Toast.LENGTH_LONG).show() }
    }
}