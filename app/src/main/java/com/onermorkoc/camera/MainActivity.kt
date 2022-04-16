package com.onermorkoc.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter()
    }

    fun adapter(){
        val adapter = viewpaper2_adapter(supportFragmentManager, lifecycle)
        viewpaper2_id.adapter = adapter
        TabLayoutMediator(tabLayout_id,viewpaper2_id){tab,possedion->
            when(possedion){
                0->{
                   tab.text="Camera"
                }
                1->{
                    tab.text="Video"
                }
            }
        }.attach()
    }
}