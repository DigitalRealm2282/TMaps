package com.tdi.tmaps

import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tdi.tmaps.BuildConfig.VERSION_NAME
import com.tdi.tmaps.databinding.ActivitySettingBinding
import com.tdi.tmaps.utils.Common


class SettingActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySettingBinding
    private lateinit var preferences5: SharedPreferences
    private lateinit var editor5: SharedPreferences.Editor
    private lateinit var preferences6: SharedPreferences
    private lateinit var editor6: SharedPreferences.Editor
    private lateinit var userInfo: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userInfo = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
        preferences5 = getSharedPreferences("rem", MODE_PRIVATE)
        editor5 = preferences5.edit()
        preferences6 = getSharedPreferences("rem_switch", MODE_PRIVATE)
        editor6 = preferences6.edit()

        binding.txtUserEmail.text = Common.loggedUser!!.email
        binding.txtUserId.text = Common.loggedUser!!.uid

        binding.remember.isChecked = preferences6.getBoolean("remSwitch",true)

        binding.mapSett.setOnClickListener {
            val intent = Intent(this@SettingActivity,TMapStyle::class.java)
            startActivity(intent)
        }

        binding.remember.setOnClickListener {
            if (binding.remember.isChecked) {
                editor5.putBoolean("rememberMe", true)
                editor6.putBoolean("remSwitch",true)
                binding.remember.isChecked = true
                editor5.apply()
                editor6.apply()
            } else {
                editor5.putBoolean("rememberMe", false)
                editor6.putBoolean("remSwitch",false)
                binding.remember.isChecked = false
                editor5.apply()
                editor6.apply()

            }
        }


        binding.appInfo.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("TMap")
            alertDialog.setMessage("TMap "+" Version "+VERSION_NAME+"\nMade by Touch digital industries\n2022 - "+ Calendar.getInstance().get(Calendar.YEAR)+"\nTMap requires background location to function\nYour friends can track you even when not subscribed")
            alertDialog.setNegativeButton("Close"){DialogInterface,_ -> DialogInterface.dismiss()}
            alertDialog.show()
        }

        binding.inviteFriend.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.tdi.tmaps")
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent,"Share via:"))
        }

        binding.passCheck.setOnClickListener {
            val intent = Intent(this@SettingActivity, PrivacyActivity::class.java)
            startActivity(intent)
        }


    }


}