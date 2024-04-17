package esir.progmob.buffalo_mobill

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import java.io.IOException

class ServerConnexion : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    companion object {
        private const val UUID = "550e8400-e29b-41d4-a716-446655440000"
    }
    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() { // connexion côté serveur

        private var shouldLoop = true
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Buffalo mo-Bill", java.util.UUID.fromString(UUID))
        }
        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e("CONNEXION", "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    //mmServerSocket?.close()
                    //shouldLoop = false
                }
            }
        }
        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            Log.d("CONNEXION", "cancel: Cancelling AcceptThread.")
            try {
                shouldLoop = false
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.d("CONNEXION", "Could not close the connect socket", e)
            }
        }
    }

    private fun manageMyConnectedSocket(it: BluetoothSocket) {
        Log.d("CONNEXION", "fonction de transfert de données")
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                // Traitez le message ici
                Log.d("DATAEXCHANGE", "[server] Message received: " + msg.what.toString() + " " + msg.obj.toString())
            }
        }
        DataExchange(this, it, handler).start()
        Log.d("DATAEXCHANGE", "DataExchange started");
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstantState : Bundle?) {
        super.onCreate(savedInstantState)
        setContentView(R.layout.bluetooth_server)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val requestCode = 1;
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivityForResult(discoverableIntent, requestCode)
        val thread = AcceptThread()
        thread.start()
    }
}