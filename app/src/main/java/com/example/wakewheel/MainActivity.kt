package com.example.wakewheel

import ACTION_DATA_AVAILABLE
import ACTION_GATT_CONNECTED
import ACTION_GATT_DISCONNECTED
import ACTION_GATT_SERVICES_DISCOVERED
import BluetoothLeService
import android.Manifest.permission.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.*

@RuntimePermissions
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var myHeartRate: HeartRate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val heartRate = HeartRate(this)
        myHeartRate = heartRate
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val filter = IntentFilter(ACTION_GATT_CONNECTED)
        filter.addAction(ACTION_DATA_AVAILABLE)
        filter.addAction(ACTION_GATT_DISCONNECTED)
        filter.addAction(ACTION_GATT_SERVICES_DISCOVERED)
        registerReceiver(gattUpdateReceiver, filter)

        register_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_COARSE_LOCATION, BLUETOOTH, BLUETOOTH_ADMIN),
                    0
                )

            } else {
                heartRate.scanForBle()
            }
        }


        connect_gatt.setOnClickListener {
            heartRate.connectGatt()
        }

        face_recognition.setOnClickListener {
            openFaceRecognition()
        }

        notifications_button.setOnClickListener {
            heartRate.service.bluetoothGatt?.services?.filter {
                it.uuid == UUID.fromString(GattAttributes.HEART_RATE_SERVICE)
            }?.let { gattServices ->
                gattServices.firstOrNull()
                    .let { gattService ->
                        gattService?.characteristics
                            ?.filter {
                                it.uuid == UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT)
                            }
                            .let {
                                it?.firstOrNull()
                                    ?.let { it1 -> heartRate.setNotification(it1) }
                            }
                    }
            }

        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    @NeedsPermission(CAMERA)
    fun openFaceRecognition() {
        startActivity(createFaceRecognitionIntent(this))
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private val gattUpdateReceiver = object : BroadcastReceiver() {

        private var bluetoothLeService: BluetoothLeService? = null

        override fun onReceive(context: Context, intent: Intent) {

            if (bluetoothLeService == null) {
                bluetoothLeService = myHeartRate?.service!!
            }

            when (intent.action) {
                ACTION_GATT_CONNECTED -> {
                    println("Gatt connected !! Yupi !! ")
                }
                ACTION_GATT_DISCONNECTED -> {
                    println("Gatt Disconnected :( ")
                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    println("Gatt Services discovered :  ${bluetoothLeService?.bluetoothGatt?.services}")
                }
                ACTION_DATA_AVAILABLE -> {
                    heart_rate.text = intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                }
            }
        }
    }
}
