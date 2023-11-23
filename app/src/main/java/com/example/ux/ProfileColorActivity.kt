package com.example.ux

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.ux.databinding.ActivityProfileColorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileColorActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileColorBinding
    private var selectedButtonId = -1 // 현재 선택된 배경 버튼
    lateinit var myUid: String
    private var bgColor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileColorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun colorChoice() {
        val buttonIds = arrayOf(
            R.id.bg1, R.id.bg2, R.id.bg3, R.id.bg4, R.id.bg5, R.id.bg6, R.id.bg7, R.id.bg8, R.id.bg9,
            R.id.bg10, R.id.bg11, R.id.bg12
        )

        for (buttonId in buttonIds) {
            val button = findViewById<ImageButton>(buttonId)
            button.setOnClickListener {
                if (selectedButtonId != buttonId) {
                    setUnselectedBackgrounds()
                    setSelectedBackground(buttonId) // 선택한 버튼 배경 테두리 나타남
                    selectedButtonId = buttonId
                }
            }
        }
    }

    private fun setUnselectedBackgrounds() {
        val buttonIds = arrayOf(
            R.id.bg1, R.id.bg2, R.id.bg3, R.id.bg4, R.id.bg5, R.id.bg6, R.id.bg7, R.id.bg8, R.id.bg9,
            R.id.bg10, R.id.bg11, R.id.bg12
        )

        for (buttonId in buttonIds) {
            val button = findViewById<ImageButton>(buttonId)
            val resourceName = "bg" + resources.getResourceEntryName(buttonId).removePrefix("bg").toInt()
            val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
            button.setBackgroundResource(resourceId)
        }
    }

    private fun setSelectedBackground(selectedId: Int) {
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!

        val button = findViewById<ImageButton>(selectedId)
        val resourceName = "btn_bg" + resources.getResourceEntryName(selectedId).removePrefix("bg").toInt()
        val colorResName = "bg" + resources.getResourceEntryName(selectedId).removePrefix("bg").toInt()
        val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
        button.setBackgroundResource(resourceId)

        val requestBody = mapOf("profileColor" to colorResName)
        bgColor = colorResName

        val colorAttrId = resources.getIdentifier(colorResName, "color", packageName)
        val color = ContextCompat.getColor(this, colorAttrId)
        val drawable = ContextCompat.getDrawable(this, R.drawable.bg_ku)

        drawable?.mutate()?.let {
            val wrappedDrawable = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedDrawable, color)
            binding.profileBG2.background = wrappedDrawable
        }
    }

    private fun init() {
        colorChoice()

        binding.changeButton.setOnClickListener {
            val db = Firebase.database.getReference("moi")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (users in dataSnapshot.children) {
                        val profileBGRef = db.child(myUid).child("profileColor")

                        db.child(myUid).child("profileColor")
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    profileBGRef.setValue(bgColor)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Error handling
                }
            })

            val intent = Intent()
            intent.putExtra("selectedItemId", R.id.third)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}