package com.tdi.tmaps

import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var preferences5: SharedPreferences
    private lateinit var editor5: SharedPreferences.Editor
    private lateinit var preferences6: SharedPreferences
    private lateinit var editor6: SharedPreferences.Editor
    private lateinit var preferences7: SharedPreferences
    private lateinit var editor7: SharedPreferences.Editor
    private lateinit var preferences8: SharedPreferences
    private lateinit var editor8: SharedPreferences.Editor
    private lateinit var pref_icon: SharedPreferences
    private lateinit var edit_icon: SharedPreferences.Editor
    private lateinit var prefMap: SharedPreferences
    private lateinit var editMap: SharedPreferences.Editor
    private lateinit var userInfo: DatabaseReference
    private var publicLocation:DatabaseReference = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userInfo = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
        preferences = getSharedPreferences("rideMode", MODE_PRIVATE)
        editor = preferences.edit()
        preferences2 = getSharedPreferences("state", MODE_PRIVATE)
        editor2 = preferences2.edit()
        preferences3 = getSharedPreferences("state_track", MODE_PRIVATE)
        editor3 = preferences3.edit()
        preferences4 = getSharedPreferences("live", MODE_PRIVATE)
        editor4 = preferences4.edit()
        preferences5 = getSharedPreferences("rem", MODE_PRIVATE)
        editor5 = preferences5.edit()
        preferences6 = getSharedPreferences("rem_switch", MODE_PRIVATE)
        editor6 = preferences6.edit()
        preferences7 = getSharedPreferences("acc_switch", MODE_PRIVATE)
        editor7 = preferences7.edit()
        preferences8 = getSharedPreferences("sS", MODE_PRIVATE)
        editor8 = preferences8.edit()
        pref_icon = getSharedPreferences("icon", MODE_PRIVATE)
        edit_icon = pref_icon.edit()
        prefMap = getSharedPreferences("map", MODE_PRIVATE)
        editMap = prefMap.edit()


        val accuracyOptions = resources.getStringArray(R.array.acc_options)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        binding.changer.visibility = View.GONE


        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, accuracyOptions)

        binding.accStatus.adapter = adapter
        binding.accStatus.setSelection(getPersistedItem());
        binding.accStatus.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0 -> {
                        editor7.putString("accStatus", "High")
                        editor7.apply()
                        Toast.makeText(this@SettingActivity,"High Accuracy",Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        editor7.putString("accStatus", "Balanced")
                        editor7.apply()
                        Toast.makeText(this@SettingActivity,"Balanced Accuracy",Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        editor7.putString("accStatus", "Low")
                        editor7.apply()
                        Toast.makeText(this@SettingActivity,"Low Accuracy",Toast.LENGTH_SHORT).show()
                    }
                }
                setPersistedItem(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                p0!!.selectedItem
                editor7.putString("accStatus","High")
                editor7.apply()
            }

        }


        binding.delete.setOnClickListener {
            AlertDialog.Builder(this)
            .setTitle("TMap")
            .setMessage("Confirm\nAll your data will be deleted")
            .setPositiveButton("Yes") {_,_ ->
                FirebaseDatabase.getInstance().getReference(Common.TOKENS).child(firebaseUser!!.uid)
                    .removeValue()
                userInfo.child(Common.loggedUser!!.uid!!)
                    .removeValue()
                publicLocation.child(Common.loggedUser!!.uid!!).removeValue()

                FirebaseAuth.getInstance().currentUser?.delete()
                    ?.addOnSuccessListener {
                        Toast.makeText(this,"All user data deleted",Toast.LENGTH_SHORT).show()
                        editor5.putBoolean("rememberMe", false)
                        editor6.putBoolean("remSwitch",false)
                        editor5.apply()
                        editor6.apply()
                        binding.remember.isChecked = false
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        finish()
                    }
                    ?.addOnFailureListener { T -> Toast.makeText(this,T.message,Toast.LENGTH_SHORT).show() }

            }
            .setNegativeButton("No"){DialogInterface,_ -> DialogInterface.dismiss()}
            .show()
        }
        binding.signOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("TMap")
                .setMessage("Confirm")
                .setPositiveButton("Yes") {_,_ ->
                    FirebaseAuth.getInstance().signOut()
                    editor5.putBoolean("rememberMe", false)
                    editor6.putBoolean("remSwitch",false)
                    editor5.apply()
                    editor6.apply()
                    binding.remember.isChecked = false
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

//                    startActivity(Intent(this,LoginActivity::class.java))
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
        binding.remember.isChecked = preferences6.getBoolean("remSwitch",true)
        binding.MapMode.isChecked = prefMap.getBoolean("mapStyle",true)


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

        binding.MapMode.setOnClickListener {
            if (binding.MapMode.isChecked) {
                editMap.putBoolean("mapStyle", true)
                binding.MapMode.isChecked = true
                editMap.apply()
            } else {
                editMap.putBoolean("mapStyle", false)
                binding.MapMode.isChecked = false
                editMap.apply()
            }
        }

        binding.appInfo.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("TMap")
            alertDialog.setMessage("TMap "+" Version "+BuildConfig.VERSION_NAME+"\nMade by Touch digital industries\n2022 - "+ Calendar.getInstance().get(Calendar.YEAR)+"\nTMap requires background location to function\nYour friends can track you even when not subscribed")
            alertDialog.setNegativeButton("Close"){DialogInterface,_ -> DialogInterface.dismiss()}
            alertDialog.show()
        }

        if (pref_icon.getString("iconStyle","car")=="car") binding.carRadio.isChecked = true
        else binding.motoRadio.isChecked = true

        binding.carRadio.setOnClickListener {
            if (binding.motoRadio.isChecked)
                binding.motoRadio.isChecked = false
            edit_icon.putString("iconStyle", "car")
            edit_icon.apply()
        }
        binding.motoRadio.setOnClickListener {
            if (binding.carRadio.isChecked)
                binding.carRadio.isChecked = false
            edit_icon.putString("iconStyle", "bike")
            edit_icon.apply()
        }
        binding.inviteFriend.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.tdi.tmaps")
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

    private fun getPersistedItem(): Int {
        val keyName = "sS"
        return preferences8.getInt(keyName, 0)
    }

    private fun setPersistedItem(position: Int) {
        val keyName = "sS"
        editor8.putInt(keyName, position)
            .apply()
    }


}