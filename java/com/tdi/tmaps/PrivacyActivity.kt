package com.tdi.tmaps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tdi.tmaps.databinding.ActivityPrivacyBinding
import com.tdi.tmaps.utils.Common
import java.util.*

class PrivacyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyBinding
    private lateinit var userInfo: DatabaseReference
    private var publicLocation: DatabaseReference = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
    private lateinit var preferences5: SharedPreferences
    private lateinit var editor5: SharedPreferences.Editor
    private lateinit var preferences6: SharedPreferences
    private lateinit var editor6: SharedPreferences.Editor
    private lateinit var resource: Resources
    private lateinit var prefCurrentLang: SharedPreferences
    var context: Context? = null
    var text =  ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefCurrentLang = getSharedPreferences("currentLang", MODE_PRIVATE)

        resource = resources
        if (prefCurrentLang.getString("myLang", "en")=="en"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "en")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "en"
        }else if (prefCurrentLang.getString("myLang", "ar")=="ar"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "ar")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "ar"

        }else if (prefCurrentLang.getString("myLang", "fr")=="fr"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "fr")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "fr"

        }else if (prefCurrentLang.getString("myLang", "ja")=="ja"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "ja")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "ja"

        }else if (prefCurrentLang.getString("myLang", "zh")=="zh"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "zh")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "zh"

        }else if (prefCurrentLang.getString("myLang", "ms")=="ms"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "ms")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "ms"

        }else if (prefCurrentLang.getString("myLang", "ru")=="ru"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "ru")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "ru"

        }else if (prefCurrentLang.getString("myLang", "es")=="es"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "es")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "es"

        }else if (prefCurrentLang.getString("myLang", "de")=="de"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "de")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "de"

        }else if (prefCurrentLang.getString("myLang", "it")=="it"){
            context = LocaleHelper.setLocale(this@PrivacyActivity, "it")
            resource = context!!.resources
            binding.delete.text = resource.getString(R.string.setting_delete_user)
            binding.signOut.text = resource.getString(R.string.setting_logout)
            binding.passCheck.text = resource.getString(R.string.setting_pass)
            binding.changePass.text = resource.getString(R.string.setting_ok)
            binding.newPass.hint = resource.getString(R.string.setting_pass)
            text = "it"

        }

        userInfo = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
        preferences5 = getSharedPreferences("rem", MODE_PRIVATE)
        editor5 = preferences5.edit()
        preferences6 = getSharedPreferences("rem_switch", MODE_PRIVATE)
        editor6 = preferences6.edit()

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        binding.changer.visibility = View.GONE


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
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No"){DialogInterface,_ -> DialogInterface.dismiss()}
                .show()
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
                                this@PrivacyActivity,
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

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context.changeLocale(text))
    }

    private fun Context.changeLocale(language:String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = this.resources.configuration
        config.setLocale(locale)
        return createConfigurationContext(config)
    }

}