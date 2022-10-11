package com.tdi.tmaps

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.tdi.tmaps.databinding.ActivitySettingBinding
import com.tdi.tmaps.databinding.ActivityTmapStyleBinding

class TMapStyle : AppCompatActivity() {
    private lateinit var binding: ActivityTmapStyleBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferences2: SharedPreferences
    private lateinit var editor2: SharedPreferences.Editor
    private lateinit var preferences3: SharedPreferences
    private lateinit var editor3: SharedPreferences.Editor
    private lateinit var preferences4: SharedPreferences
    private lateinit var editor4: SharedPreferences.Editor
    private lateinit var preferences7: SharedPreferences
    private lateinit var editor7: SharedPreferences.Editor
    private lateinit var preferences8: SharedPreferences
    private lateinit var editor8: SharedPreferences.Editor
    private lateinit var pref_icon: SharedPreferences
    private lateinit var edit_icon: SharedPreferences.Editor
    private lateinit var prefMap: SharedPreferences
    private lateinit var editMap: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTmapStyleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("rideMode", MODE_PRIVATE)
        editor = preferences.edit()
        preferences2 = getSharedPreferences("state", MODE_PRIVATE)
        editor2 = preferences2.edit()
        preferences3 = getSharedPreferences("state_track", MODE_PRIVATE)
        editor3 = preferences3.edit()
        preferences4 = getSharedPreferences("live", MODE_PRIVATE)
        editor4 = preferences4.edit()
        preferences7 = getSharedPreferences("acc_switch", MODE_PRIVATE)
        editor7 = preferences7.edit()
        preferences8 = getSharedPreferences("sS", MODE_PRIVATE)
        editor8 = preferences8.edit()
        pref_icon = getSharedPreferences("icon", MODE_PRIVATE)
        edit_icon = pref_icon.edit()
        prefMap = getSharedPreferences("map", MODE_PRIVATE)
        editMap = prefMap.edit()

        binding.RideMode.isChecked = preferences2.getBoolean("switchState",true)
        binding.TrackMode.isChecked = preferences3.getBoolean("switchTrack",true)
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
        val accuracyOptions = resources.getStringArray(R.array.acc_options)

        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, accuracyOptions)

        binding.accStatus.adapter = adapter
        binding.accStatus.setSelection(getPersistedItem());
        binding.accStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0 -> {
                        editor7.putString("accStatus", "High")
                        editor7.apply()
                        binding.accState.text = resources.getString(R.string.high_acc)
                    }
                    1 -> {
                        editor7.putString("accStatus", "Balanced")
                        editor7.apply()
                        binding.accState.text = resources.getString(R.string.balanced_acc)
                    }
                    2 -> {
                        editor7.putString("accStatus", "Low")
                        editor7.apply()
                        binding.accState.text = resources.getString(R.string.low_acc)
                    }
                }
                setPersistedItem(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                p0!!.selectedItem
                editor7.putString("accStatus","High")
                editor7.apply()
                binding.accState.text = resources.getString(R.string.high_acc)
            }

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