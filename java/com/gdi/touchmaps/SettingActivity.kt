package com.gdi.touchmaps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gdi.touchmaps.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth

class SettingActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.delete.setOnClickListener {
            AlertDialog.Builder(this)
            .setTitle("Touch Maps")
            .setMessage("Confirm")
            .setPositiveButton("Yes") {_,_ ->
                FirebaseAuth.getInstance().currentUser?.delete()
                    ?.addOnSuccessListener { Toast.makeText(this,"User deleted",Toast.LENGTH_SHORT).show() }
                    ?.addOnFailureListener { T -> Toast.makeText(this,T.message,Toast.LENGTH_SHORT).show() }}
            .setNegativeButton("No"){DialogInterface,_ -> DialogInterface.dismiss()}
            .show() }
        binding.signOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Touch Maps")
                .setMessage("Confirm")
                .setPositiveButton("Yes") {_,_ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this,LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("No"){DialogInterface,_ -> DialogInterface.dismiss()}
                .show()
        }
    }
}