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
    private lateinit var preferences3: SharedPreferences
    private lateinit var editor3: SharedPreferences.Editor
    private lateinit var preferences4: SharedPreferences
    private lateinit var editor4: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("rideMode", MODE_PRIVATE)
        editor = preferences.edit()
        preferences2 = getSharedPreferences("state", MODE_PRIVATE)
        editor2 = preferences2.edit()
        preferences3 = getSharedPreferences("state_track", MODE_PRIVATE)
        editor3 = preferences3.edit()
        preferences4 = getSharedPreferences("live", MODE_PRIVATE)
        editor4 = preferences4.edit()

        binding.changer.visibility = View.GONE

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
        binding.TrackMode.isChecked = preferences3.getBoolean("switchTrack",true)

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

        binding.TrackMode.setOnClickListener {
            if (binding.TrackMode.isChecked) {
                editor3.putBoolean("switchTrack", true)
                editor4.putBoolean("liveMode",true)
                binding.TrackMode.isChecked = true
                editor3.commit()
                editor4.apply()
            } else {
                editor3.putBoolean("switchTrack", false)
                editor4.putBoolean("liveMode",false)
                binding.TrackMode.isChecked = false
                editor3.commit()
                editor4.apply()

            }
        }

        binding.appInfo.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("TMap")
            alertDialog.setMessage("TMap "+" Version "+BuildConfig.VERSION_NAME+"\nMade by Touch digital industries\n2022 - "+ Calendar.getInstance().get(Calendar.YEAR)+"\nTMap requires background location to function\nYour friends can track you even when not subscribed")
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

        binding.passCheck.setOnClickListener {
            if (binding.changer.visibility == View.VISIBLE){
                binding.changer.visibility = View.GONE
            }else{
                binding.changer.visibility = View.VISIBLE
            }
        }

        binding.changePass.setOnClickListener {
            if (binding.newPass.text!!.isNotEmpty()) {
                FirebaseAuth.getInstance().currentUser?.updatePassword(binding.newPass.text.toString())
                    ?.addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            Toast.makeText(
                                this@SettingActivity,
                                "Password updated",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    ?.addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            exception.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }else{
                Toast.makeText(
                    this,
                    "Write new password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}