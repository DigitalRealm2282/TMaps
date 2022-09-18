package com.tdi.tmaps

import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.tdi.tmaps.databinding.ActivitySettingBinding
import com.tdi.tmaps.utils.Common

class SettingActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySettingBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var preferences2: SharedPreferences
    private lateinit var editor2: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("rideMode", MODE_PRIVATE)
        editor = preferences.edit()

        preferences2 = getSharedPreferences("state", MODE_PRIVATE)
        editor2 = preferences2.edit()

        binding.delete.setOnClickListener {
            AlertDialog.Builder(this)
            .setTitle("TMap")
            .setMessage("Confirm")
            .setPositiveButton("Yes") {_,_ ->
                FirebaseAuth.getInstance().currentUser?.delete()
                    ?.addOnSuccessListener { Toast.makeText(this,"User deleted",Toast.LENGTH_SHORT).show() }
                    ?.addOnFailureListener { T -> Toast.makeText(this,T.message,Toast.LENGTH_SHORT).show() }

                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No"){DialogInterface,_ -> DialogInterface.dismiss()}
            .show() }
        binding.signOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("TMap")
                .setMessage("Confirm")
                .setPositiveButton("Yes") {_,_ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this,LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("No"){DialogInterface,_ -> DialogInterface.dismiss()}
                .show()
        }

        binding.userInfo.setOnClickListener {view ->
            val snack = Snackbar.make(view,"ID: "+Common.loggedUser!!.uid,Snackbar.LENGTH_INDEFINITE)
                .setAction("Close"){  }

            snack.show()
        }

        binding.RideMode.isChecked = preferences2.getBoolean("switchState",true)

        binding.RideMode.setOnClickListener {
            if (binding.RideMode.isChecked) {
                editor.putBoolean("ttsMode", true)
                editor2.putBoolean("switchState", true)
                binding.RideMode.isChecked = true
                editor.commit()
                editor2.commit()
            } else {
                editor.putBoolean("ttsMode", false)
                editor2.putBoolean("switchState", false)
                binding.RideMode.isChecked = false
                editor.commit()
                editor2.commit()
            }
        }

        binding.appInfo.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("TMap")
            alertDialog.setMessage("TMap "+" Version 1.12\nMade by Touch digital industries\n2022 - "+ Calendar.getInstance().get(Calendar.YEAR)+"\nTMap requires background location to function")
            alertDialog.setNegativeButton("Close"){DialogInterface,_ -> DialogInterface.dismiss()}
            alertDialog.show()
        }

        binding.inviteFriend.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/dev?id=6288862063586070667")
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent,"Share via:"))
        }

        binding.verify.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
            FirebaseAuth.getInstance().currentUser?.reload()
            if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified){
                binding.verify.visibility = View.GONE
            }else{
                Toast.makeText(this,"Verify please",Toast.LENGTH_SHORT).show()
            }
        }
    }
}