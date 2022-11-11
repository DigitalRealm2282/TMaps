package com.tdi.tmaps

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tdi.tmaps.databinding.ActivityAboutBinding
import com.tdi.tmaps.databinding.ActivityMainBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var prefBG : SharedPreferences
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefBG = getSharedPreferences("BG", MODE_PRIVATE)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        checkBG()

    }

    override fun onResume() {
        super.onResume()
        checkBG()
    }

    private fun checkBG() {

        if (prefBG.getString("background", "normal")=="normal"){
            binding.aboutBg.background = resources.getDrawable(R.mipmap.bg,null)
        }else if (prefBG.getString("background", "leaf")=="leaf"){
            binding.aboutBg.background = resources.getDrawable(R.mipmap.greenleafbg,null)
        }else if (prefBG.getString("background", "car")=="car"){
            binding.aboutBg.background = resources.getDrawable(R.mipmap.car,null)
        }else if (prefBG.getString("background", "green")=="green"){
            binding.aboutBg.background = resources.getDrawable(R.mipmap.planegreenbg,null)
        }else{
            binding.aboutBg.background = resources.getDrawable(R.mipmap.planegreenbg,null)
        }
    }

}