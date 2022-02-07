package com.hxw.camerax_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.hxw.camera_lib.CaptureActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.start_video_page).setOnClickListener {
            CaptureActivity.start(this, true)
        }
        findViewById<Button>(R.id.start_image_page).setOnClickListener {
            CaptureActivity.start(this, false)
        }
    }
}