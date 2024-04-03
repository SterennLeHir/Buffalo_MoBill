package esir.progmob.buffalo_mobill

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.IntentFilter
import android.location.LocationManager
import android.provider.Settings

class MultiplayersConnexion : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    //private var answer: Boolean = false // vérifie si l'utilisateur a accepté ou rejeté les permissions
    private var bluetoothAccepted = false
    private var localisationAccepted = false
    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_BLUETOOTH = 1001
    }

    private var liste: MutableList<BluetoothDevice> = mutableListOf()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d("BLUETOOTH", "device: " + device.toString())
                if (device != null) {
                    if (!liste.contains(device)) {
                        liste.add(device)
                        // updateList()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        Log.d("BLUETOOTH", "début de onCreate")
        // On vérifie que l'utilisateur a le bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            finish()
        }

        Log.d("BLUETOOTH", "bluetooth supporté")
        checkBluetoothPermissions()
    }
    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("BLUETOOTH", "Pas de permissions API >= 31")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_REQUEST_BLUETOOTH
                )
                Log.d("BLUETOOTH", "Permissions demandées") // CRASH APRES CA

            } else { // si l'utilisateur a déjà accordé les permissions
                //answer = true
                enablingBluetooth()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("BLUETOOTH", "Pas de permissions API < 31")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_REQUEST_BLUETOOTH
                )
                Log.d("BLUETOOTH", "Permissions demandées")
            } else { // si l'utilisateur a déjà accordé les permissions
                //answer = true
                enablingBluetooth()
            }
        }
    }

    override fun onRequestPermissionsResult( // PAS SURE QUE CA FONCTIONNE
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("BLUETOOTH", "réponse de l'utilisateur")
        when (requestCode) {
            PERMISSION_REQUEST_BLUETOOTH -> {
                Log.d("BLUETOOTH", "answer true")
                //answer = true
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAccepted = true
                    Log.d("BLUETOOTH", "bluetooth accepté")
                    enablingBluetooth()
                }
                else{
                    Log.d("BLUETOOTH", "bluetooth pas accepté")
                    //TODO A COMPLETER
                }
            }
            // D'autres cas de requêtes de permissions peuvent être gérés ici si nécessaire
        }
        return
    }



    /*
    fun updateList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            liste.map { d ->
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                d.name + "   " + d.address
            })
        val list = findViewById<ListView>(R.id.liste)
        list.adapter = adapter
    }
    */
    @SuppressLint("MissingPermission")
    fun enablingBluetooth(){
        // On active le bluetooth
        if (!bluetoothAdapter.isEnabled) {
            Log.d("BLUETOOTH", "Activation du bluetooth")
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            // here to request the missing permissions, and then overriding
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
        }
        // On active la localisation
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        if (!isLocationEnabled) {
            val enableLocalisationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(enableLocalisationIntent, 1)
        }

        // tant que le bluetooth et la localisation ne sont pas activé, on
        while (!bluetoothAdapter.isEnabled || !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            ))
        ) {Log.d("BLUETOOTH", "waiting for localisation/bluetooth activation...")}

        // On enregistre le broadcast receiver
        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, discoverDevicesIntent)

        // On lance la recherche des appareils à proximité
        val start = bluetoothAdapter.startDiscovery()
        Log.d("BLUETOOTH", "start discorvery : " + start.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothAccepted) unregisterReceiver(receiver)
    }
}
