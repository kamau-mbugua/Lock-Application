package com.kelvin.demoapplication

import android.app.AppOpsManager
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_CODE_OVERLAY = 101
    private val REQUEST_CODE_USAGE_STATS = 102
    var tabsArrayDefault = arrayOf(
        "Home", "Start"
    )




    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.kelvin.demoapplication.APP_BLOCKED") {
                val remainingTime = intent.getLongExtra("remaining_time", 0L)
                Toast.makeText(
                    this@MainActivity,
                    "App is blocked for $remainingTime milliseconds",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        registerServiceReceiver()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpPermissions()
        setUpAdapters()
    }

    private fun setUpAdapters() {
        val adapter = MainViewPagerDefaultAdapter(
            supportFragmentManager,
            lifecycle,
        )
        findViewById<ViewPager2>(R.id.viewPager).adapter = adapter

        TabLayoutMediator( findViewById<TabLayout>(R.id.tabLayout),  findViewById<ViewPager2>(R.id.viewPager)) { tab, position ->
            tab.text = tabsArrayDefault[position]
        }.attach()
    }


    private fun replaceFragment(
        fragment: Fragment, @IdRes containerViewId: Int, addToStack: Boolean
    ) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        if (addToStack) {
            fragmentTransaction.add(containerViewId, fragment)
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        } else {
            fragmentTransaction.replace(containerViewId, fragment)
        }
        // fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.commit()
    }

    private fun registerServiceReceiver() {
        registerReceiver(receiver, IntentFilter("com.kelvin.demoapplication.APP_BLOCKED"))
    }

    private fun setUpPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, REQUEST_CODE_OVERLAY)
        }

        Log.e("MainActivityonCreate", "onCreate ${hasUsageStatsPermission(this)}")

        if (!hasUsageStatsPermission(this)) {
            requestUsageStatsPermission(this)
        }
    }










    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                val intent = Intent(this, AppBlockerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                Toast.makeText(
                    this,
                    "Overlay permission granted. Please select the duration again.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Overlay permission is needed to block apps.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == REQUEST_CODE_USAGE_STATS) {
            requestUsageStatsPermission(this)
        }
    }

    override fun onClick(v: View?) {
    }
}