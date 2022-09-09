package com.gdi.touchmaps

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.gdi.touchmaps.utils.Common.TOKENS
import com.gdi.touchmaps.utils.Common.USER_INFO
import com.gdi.touchmaps.utils.Common.USER_UID_SAVE_KEY
import com.gdi.touchmaps.utils.Common.loggedUser
import com.gdi.touchmaps.model.User
import com.gdi.touchmaps.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.installations.FirebaseInstallations
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.paperdb.Paper

class LoginActivity : AppCompatActivity() {

    lateinit var userInfo: DatabaseReference
    private lateinit var providers:List<AuthUI.IdpConfig>
    private lateinit var binding:ActivityLoginBinding

//    companion object{
//        const val MY_CODE=177
//    }
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


        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
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


        userInfo.orderByKey()
            .equalTo(firebaseUser!!.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity,error.message,Toast.LENGTH_SHORT).show()
                }


                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.value==null){
                        //user not exist
                        if (!snapshot.child(firebaseUser.uid).exists()) {
                            loggedUser = User(firebaseUser.uid, firebaseUser.email!!)
                            //add user to database
                            userInfo.child(loggedUser!!.uid!!)
                                .setValue(loggedUser)
                                //.addOnSuccessListener(this@LoginActivity) { Toast.makeText(this@LoginActivity,"Data uploaded",Toast.LENGTH_SHORT).show() }
                                //.addOnFailureListener(this@LoginActivity){ Toast.makeText(this@LoginActivity,"Failed Uploading Data",Toast.LENGTH_SHORT).show()}
                        }

                    } else {
                        //user exist
                        loggedUser = snapshot.child(firebaseUser.uid)
                            .getValue(User::class.java)!!
                        //Toast.makeText(this@LoginActivity,"User exist",Toast.LENGTH_SHORT).show()
                    }

                    //save uid to storage to update location on kill mode
                    Paper.book().write(USER_UID_SAVE_KEY, loggedUser!!.uid.toString())
                    updateToken(firebaseUser)
                    setupUI()


                }


            })


    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MY_CODE){
//
//            val firebaseUser = FirebaseAuth.getInstance().currentUser
//            //FirebaseAuthUIActivityResultContract()
//
////            FirebaseDatabase.getInstance().getReference(USER_INFO)
////                .child("email")
////                .setValue(firebaseUser?.email)
////
//
//            userInfo.orderByKey()
//                .equalTo(firebaseUser!!.uid)
//                .addListenerForSingleValueEvent(object: ValueEventListener {
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Toast.makeText(this@LoginActivity,error.message,Toast.LENGTH_SHORT).show()
//                    }
//
//
//                    override fun onDataChange(snapshot: DataSnapshot) {
//
//                        if (snapshot.value==null){
//                            //user not exist
//                            if (!snapshot.child(firebaseUser.uid).exists()) {
//                                loggedUser = User(firebaseUser.uid, firebaseUser.email!!)
//                                //add user to database
//                                userInfo.child(loggedUser!!.uid!!)
//                                    .setValue(loggedUser)
//                                    .addOnSuccessListener(this@LoginActivity) { Toast.makeText(this@LoginActivity,"Data uploaded",Toast.LENGTH_SHORT).show() }
//                                    .addOnFailureListener(this@LoginActivity){ Toast.makeText(this@LoginActivity,"Failed Uploading Data",Toast.LENGTH_SHORT).show()}
//                            }
//
//                        } else {
//                            //user exist
//                            loggedUser = snapshot.child(firebaseUser.uid)
//                                .getValue(User::class.java)!!
//                            Toast.makeText(this@LoginActivity,"User exist",Toast.LENGTH_SHORT).show()
//                        }
//
//                        //save uid to storage to update location on kill mode
//                        Paper.book().write(USER_UID_SAVE_KEY, loggedUser?.uid.toString())
//                        updateToken(firebaseUser)
//                        setupUI()
//
//
//                    }
//
//
//                })
//
//
//        }
//    }

    private fun showSignInOption() {

//        startActivityForResult(
//            AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setIsSmartLockEnabled(false)
//                .setAlwaysShowSignInMethodScreen(true)
//                .setLogo(R.mipmap.ic_launcher_round)
//                .setAvailableProviders(providers)
//                .build(),MY_CODE)
        getAction.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAlwaysShowSignInMethodScreen(false)
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
        FirebaseInstallations.getInstance().getToken(false)
            .addOnSuccessListener { instanceIdResult ->
                tokens.child(firebaseUser.uid)
                    .setValue(instanceIdResult.token)

                Toast.makeText(this, loggedUser?.email+" logged in", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e -> Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()}
    }
}