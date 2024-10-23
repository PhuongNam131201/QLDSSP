package com.example.qldssp.Activity

import android.content.Intent
import android.os.Bundle
import com.example.qldssp.databinding.ActivityIntro2Binding

class IntroActivity2 : BaseActivity2() {
    private lateinit var binding: ActivityIntro2Binding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityIntro2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}