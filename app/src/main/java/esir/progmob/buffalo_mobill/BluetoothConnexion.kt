package esir.progmob.buffalo_mobill

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import android.Manifest
class BluetoothConnexion : ComponentActivity() {

    // liste affichant les devices à proximité
    private var devicesList: MutableList<BluetoothDevice> = mutableListOf()
    private lateinit var bluetoothAdapter: BluetoothAdapter

    // Brodcast receiver pour la méthode startDiscovery()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d("BLUETOOTH", "device: " + device.toString())
                if (device != null) {
                    if (!devicesList.contains(device)) {
                        devicesList.add(device)
                        updateList()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstantState : Bundle?) {
        super.onCreate(savedInstantState)
        setContentView(R.layout.bluetooth_client)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // On enregistre le broadcast receiver
        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, discoverDevicesIntent)
        // On lance la recherche des appareils à proximité
        val start = bluetoothAdapter.startDiscovery()
        Log.d("BLUETOOTH", "[client] start discovery : " + start.toString())

    }

    fun updateList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            devicesList.map { d ->
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                d.name + "   " + d.address
            })
        val list = findViewById<ListView>(R.id.liste)
        list.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}