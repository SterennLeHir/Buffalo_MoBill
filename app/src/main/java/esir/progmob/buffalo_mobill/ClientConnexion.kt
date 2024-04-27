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
import android.bluetooth.BluetoothSocket
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.AdapterView
import android.widget.Toast
import java.io.IOException

class ClientConnexion : ComponentActivity() {

    // liste affichant les devices à proximité
    private var devicesList: MutableList<BluetoothDevice> = mutableListOf()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var mediaPlayer : MediaPlayer

    companion object {
        private const val UUID = "550e8400-e29b-41d4-a716-446655440000"
    }

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
    private inner class ConnectThread(device: BluetoothDevice, val context: Context) : Thread() { // connexion côté client

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID))
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Log.d("CONNEXION", "try to connect")
                try {
                    socket.connect()
                } catch (e : IOException) {
                    e.printStackTrace()
                }

                Log.d("CONNEXION", "connected")
                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket)
                //Looper.prepare()
                //Toast.makeText(context,"Connected", Toast.LENGTH_SHORT).show()
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            Log.d("CONNEXION", "cancel connexion")
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("CONNEXION", "Could not close the client socket", e)
            }
        }
    }

    private fun manageMyConnectedSocket(it: BluetoothSocket) {
        Log.d("CONNEXION", "fonction de transfert de données")
        Multiplayer.SocketHolder.socket = it
        Multiplayer.Exchange.dataExchangeClient = DataExchange(null)
        val intent = Intent(this, GameList::class.java)
        intent.putExtra("isMulti", true) // on indique à l'activité qu'elle est en mode multijoueurs
        intent.putExtra("isServer", false) // on indique à l'activité qu'elle est le client
        mediaPlayer.stop()
        startActivity(intent);
        finish()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstantState : Bundle?) {
        super.onCreate(savedInstantState)
        setContentView(R.layout.bluetooth_client)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mediaPlayer = MediaPlayer.create(this, R.raw.duel)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        // On crée le listener sur la liste
        val listView = findViewById<ListView>(R.id.liste)
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> //on récupère la HashMap contenant les infos de notre item (titre, description, img)
            val device = parent.getItemAtPosition(position) as String
            val parts = device.split(",")
            Log.d("CONNEXION", "name =" + parts[0])
            Log.d("CONNEXION", "adress =" + parts[1])
            val selectedDevice = devicesList.find { it.address == parts[1] } // parts[1] c'est l'adresse du device
            Log.d("CONNEXION", device.toString())
            if (selectedDevice != null) { // pas possible que ce soit nul
                val connectThread = ConnectThread(selectedDevice, this)
                connectThread.start()
                Log.d("CONNEXION", "thread de connexion lancé")
            }
            Log.d("CONNEXION", (selectedDevice == null).toString())
        }
        // On enregistre le broadcast receiver
        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, discoverDevicesIntent)
        // On lance la recherche des appareils à proximité
        val start = bluetoothAdapter.startDiscovery()
        Log.d("BLUETOOTH", "[client] start discovery : $start")
    }

    fun updateList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            devicesList.map { d ->
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                d.name + "," + d.address
            })
        val list = findViewById<ListView>(R.id.liste)
        list.adapter = adapter
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        bluetoothAdapter.cancelDiscovery()
        Log.d("CONNEXION", "ClientConnexion finished")
    }
}