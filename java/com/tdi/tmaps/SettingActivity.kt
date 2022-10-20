package com.tdi.tmaps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tdi.tmaps.databinding.ActivitySettingBinding
import com.tdi.tmaps.utils.Common
import java.util.*

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var preferences5: SharedPreferences
    private lateinit var editor5: SharedPreferences.Editor
    private lateinit var preferences6: SharedPreferences
    private lateinit var editor6: SharedPreferences.Editor
    private lateinit var prefLang: SharedPreferences
    private lateinit var editLang: SharedPreferences.Editor
    private lateinit var prefCurrentLang: SharedPreferences
    private lateinit var editCurrentLang: SharedPreferences.Editor
    private lateinit var userInfo: DatabaseReference
    private lateinit var resource: Resources
    var context: Context? = null
    var text = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userInfo = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
        preferences5 = getSharedPreferences("rem", MODE_PRIVATE)
        editor5 = preferences5.edit()
        preferences6 = getSharedPreferences("rem_switch", MODE_PRIVATE)
        editor6 = preferences6.edit()
        prefCurrentLang = getSharedPreferences("currentLang", MODE_PRIVATE)
        editCurrentLang = prefCurrentLang.edit()
        prefLang = getSharedPreferences("lang", MODE_PRIVATE)
        editLang = prefLang.edit()

        resource = resources

        if (prefCurrentLang.getString("myLang", "en") == "en") {
            context = LocaleHelper.setLocale(this@SettingActivity, "en")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "ar") == "ar") {
            context = LocaleHelper.setLocale(this@SettingActivity, "ar")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "fr") == "fr") {
            context = LocaleHelper.setLocale(this@SettingActivity, "fr")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "ja") == "ja") {
            context = LocaleHelper.setLocale(this@SettingActivity, "ja")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "zh") == "zh") {
            context = LocaleHelper.setLocale(this@SettingActivity, "zh")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "ms") == "ms") {
            context = LocaleHelper.setLocale(this@SettingActivity, "ms")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "ru") == "ru") {
            context = LocaleHelper.setLocale(this@SettingActivity, "ru")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "es") == "es") {
            context = LocaleHelper.setLocale(this@SettingActivity, "es")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "de") == "de") {
            context = LocaleHelper.setLocale(this@SettingActivity, "de")
            resource = context!!.resources
        } else if (prefCurrentLang.getString("myLang", "it") == "it") {
            context = LocaleHelper.setLocale(this@SettingActivity, "it")
            resource = context!!.resources
        }

        val langOptions = resource.getStringArray(R.array.lang)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, langOptions
        )

        binding.setLang.adapter = adapter
        binding.setLang.setSelection(getPersistedItem())
        binding.setLang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0 -> {
                        editCurrentLang.putString("myLang", "en")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.english)
                        context = LocaleHelper.setLocale(this@SettingActivity, "en")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "en"
                    }
                    1 -> {
                        editCurrentLang.putString("myLang", "fr")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.french)
                        context = LocaleHelper.setLocale(this@SettingActivity, "fr")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "fr"
                    }
                    2 -> {
                        editCurrentLang.putString("myLang", "ar")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.arabic)
                        context = LocaleHelper.setLocale(this@SettingActivity, "ar")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "ar"
                    }
                    3 -> {
                        editCurrentLang.putString("myLang", "ja")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.japanese)
                        context = LocaleHelper.setLocale(this@SettingActivity, "ja")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "ja"
                    }
                    4 -> {
                        editCurrentLang.putString("myLang", "zh")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.chinese)
                        context = LocaleHelper.setLocale(this@SettingActivity, "zh")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "zh"
                    }
                    5 -> {
                        editCurrentLang.putString("myLang", "de")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.german)
                        context = LocaleHelper.setLocale(this@SettingActivity, "de")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "de"
                    }
                    6 -> {
                        editCurrentLang.putString("myLang", "it")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.italian)
                        context = LocaleHelper.setLocale(this@SettingActivity, "it")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "it"
                    }
                    7 -> {
                        editCurrentLang.putString("myLang", "es")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.spanish)
                        context = LocaleHelper.setLocale(this@SettingActivity, "es")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        text = "es"
                    }
                    8 -> {
                        editCurrentLang.putString("myLang", "ru")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.russian)
                        context = LocaleHelper.setLocale(this@SettingActivity, "ru")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "ru"
                    }
                    9 -> {
                        editCurrentLang.putString("myLang", "th")
                        editCurrentLang.apply()
                        binding.currentLang.text = resource.getString(R.string.thailand)
                        context = LocaleHelper.setLocale(this@SettingActivity, "th")
                        resource = context!!.resources
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "th"
                    }
                    10 -> {
                        editCurrentLang.putString("myLang", "ms")
                        editCurrentLang.apply()
                        context = LocaleHelper.setLocale(this@SettingActivity, "ms")
                        resource = context!!.resources
                        binding.currentLang.text = resource.getString(R.string.malay)
                        binding.textAbout.text = resource.getString(R.string.setting_about)
                        binding.textPriv.text = resource.getString(R.string.setting_privacy)
                        binding.textMap.text = resource.getString(R.string.setting_map)
                        binding.rem.text = resource.getString(R.string.remember_me)
                        binding.textInv.text = resource.getString(R.string.setting_invite)
                        binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                        binding.privPrev.text = resource.getString(R.string.privacy_preview)
                        binding.from.text = resource.getString(R.string.from)
                        binding.lang.text = resource.getString(R.string.lang)

                        text = "ms"
                    }
                }
                setPersistedItem(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                p0!!.selectedItem
                editCurrentLang.putString("myLang", "en")
                editCurrentLang.apply()
//                val phoneLang = Locale.getDefault().displayLanguage
//                val locale = applicationContext.resources.configuration.locale.language
//                val locale2 = Resources.getSystem().configuration.locales[0]
                context = LocaleHelper.setLocale(this@SettingActivity, "en")
                resource = context!!.resources
                binding.currentLang.text = resource.getString(R.string.english)
                binding.textAbout.text = resource.getString(R.string.setting_about)
                binding.textPriv.text = resource.getString(R.string.setting_privacy)
                binding.textMap.text = resource.getString(R.string.setting_map)
                binding.rem.text = resource.getString(R.string.remember_me)
                binding.textInv.text = resource.getString(R.string.setting_invite)
                binding.mapSetPreview.text = resource.getString(R.string.maps_interface_review)
                binding.privPrev.text = resource.getString(R.string.privacy_preview)
                binding.from.text = resource.getString(R.string.from)
                binding.lang.text = resource.getString(R.string.lang)

                text = "en"
            }
        }

        binding.txtUserEmail.text = Common.loggedUser!!.email
        binding.txtUserId.text = Common.loggedUser!!.uid

        binding.remember.isChecked = preferences6.getBoolean("remSwitch", true)

        binding.mapSett.setOnClickListener {
            val intent = Intent(this@SettingActivity, TMapStyle::class.java)
            startActivity(intent)
        }

        binding.remember.setOnClickListener {
            if (binding.remember.isChecked) {
                editor5.putBoolean("rememberMe", true)
                editor6.putBoolean("remSwitch", true)
                binding.remember.isChecked = true
                editor5.apply()
                editor6.apply()
            } else {
                editor5.putBoolean("rememberMe", false)
                editor6.putBoolean("remSwitch", false)
                binding.remember.isChecked = false
                editor5.apply()
                editor6.apply()
            }
        }

        binding.appInfo.setOnClickListener {
            val intent = Intent(this@SettingActivity, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.inviteFriend.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.tdi.tmaps")
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, "Share via:"))
        }

        binding.passCheck.setOnClickListener {
            val intent = Intent(this@SettingActivity, PrivacyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getPersistedItem(): Int {
        val keyName = "lang"
        return prefLang.getInt(keyName, 0)
    }

    private fun setPersistedItem(position: Int) {
        val keyName = "lang"
        editLang.putInt(keyName, position).apply()
    }

//    private fun restartAct(){
//        startActivity(intent)
//        finish()
//    }

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
}