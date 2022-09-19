package com.tdi.tmaps

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.tdi.tmaps.utils.Common.TOKENS
import com.tdi.tmaps.utils.Common.USER_INFO
import com.tdi.tmaps.utils.Common.USER_UID_SAVE_KEY
import com.tdi.tmaps.utils.Common.loggedUser
import com.tdi.tmaps.model.User
import com.tdi.tmaps.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.paperdb.Paper

class LoginActivity : AppCompatActivity() {

    lateinit var userInfo: DatabaseReference
    private lateinit var providers:List<AuthUI.IdpConfig>
    private lateinit var binding:ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userInfo = FirebaseDatabase.getInstance().getReference(USER_INFO)
        Paper.init(this)

        providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        binding.startBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                disclosure()
            }else{
                showSignInOption()
            }

        }

        binding.inpt.visibility = View.GONE
        binding.send.visibility = View.GONE
        binding.forgetBtn.setOnClickListener {
            if (binding.inpt.visibility == View.GONE) {
                binding.inpt.visibility = View.VISIBLE
                binding.send.visibility = View.VISIBLE
            }else {
                binding.inpt.visibility = View.GONE
                binding.send.visibility = View.GONE
            }
        }

        binding.send.setOnClickListener {
            if (binding.editInput.text!!.isNotEmpty()) {
                resetPassword(binding.editInput.text.toString())
            }else {Toast.makeText(this,"Write your email",Toast.LENGTH_SHORT).show()}
        }

    }


    private fun resetPassword(email: String): Task<Void> {
        return FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Mail sent", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { t -> Toast.makeText(this@LoginActivity,t.message,Toast.LENGTH_SHORT).show() }
    }

    private fun disclosure() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("GET STARTED")
        alert.setMessage("TMap collects location data to enable live tracking even when the app is closed or not in use.")
        alert.setPositiveButton("Ok"){_,_ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getPermissionApi()
            }else{
                getPermission()
            }
        }
        alert.setNegativeButton("Cancel"){DialogInterface,_ -> DialogInterface.dismiss()}
        alert.show()
    }

        private fun getPermission(){


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
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getPermissionApi(){

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


    private val getAction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser!!.uid != null){
            userInfo.orderByKey()
                .equalTo(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.value == null) {
                            //user not exist
                            if (!snapshot.child(firebaseUser.uid).exists()) {
                                loggedUser = User(firebaseUser.uid, firebaseUser.email!!)
                                firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener {
                                        //add user to database
                                        userInfo.child(loggedUser!!.uid!!)
                                            .setValue(loggedUser)
                                    }
                                firebaseUser.reload()
                                //                            //add user to database
                                //                            userInfo.child(loggedUser!!.uid!!)
                                //                                .setValue(loggedUser)
                            }

                        } else {
                            //user exist
                            loggedUser = snapshot.child(firebaseUser.uid)
                                .getValue(User::class.java)!!

                        }

                        //save uid to storage to update location on kill mode
                        Paper.book().write(USER_UID_SAVE_KEY, loggedUser!!.uid.toString())
                        updateToken(firebaseUser)
                        setupUI()

                    }

                })
        }else{
            Toast.makeText(this@LoginActivity,"Error logging try again",Toast.LENGTH_SHORT).show()
        }

    }


    private fun showSignInOption() {

        getAction.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAlwaysShowSignInMethodScreen(false)
                .setTheme(R.style.Theme_TouchMaps)
                .setLogo(R.mipmap.ic_launcher_round)
                .setAvailableProviders(providers)
                .build()
        )
    }

    private fun setupUI() {
        //after all done navigation home
        startActivity(Intent(this@LoginActivity,MainActivity::class.java))
        finish()

    }

    private fun updateToken(firebaseUser: FirebaseUser) {
        val tokens = FirebaseDatabase.getInstance()
            .getReference(TOKENS)

        //get Token
        //val token = FirebaseMessaging.getInstance().token.result
        FirebaseMessaging.getInstance().token
             .addOnSuccessListener { token ->
                 tokens.child(firebaseUser.uid)
                     .setValue(token)
                Toast.makeText(this, " Welcome back "+loggedUser?.email, Toast.LENGTH_SHORT).show()
             }
             .addOnFailureListener{e -> Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()}


    }
}