package esir.progmob.buffalo_mobill

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.IOException

class ServerConnexion : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    companion object {
        private const val UUID = "550e8400-e29b-41d4-a716-446655440000"
    }
    @SuppressLint("MissingPermission")
    private inner class AcceptThread(val context: Context) : Thread() { // connexion côté serveur

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
                    manageMyConnectedSocket(it, context)
                    //mmServerSocket?.close()
                    //shouldLoop = false
                    //Looper.prepare()
                    //Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
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

    private fun manageMyConnectedSocket(it: BluetoothSocket, context: Context) {
        Log.d("CONNEXION", "fonction de transfert de données")
        Multiplayer.SocketHolder.socket = it
        val intent = Intent(context, GameList::class.java)
        intent.putExtra("isMulti", true) // on indique à l'activité qu'elle est en mode multijoueurs
        intent.putExtra("isServer", true) // on indique à l'activité qu'elle est le serveur
        context.startActivity(intent)
        finish()
        Log.d("CONNEXION", "finished")
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstantState : Bundle?) {
        super.onCreate(savedInstantState)
        setContentView(R.layout.bluetooth_server)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        askForDiscoverability()
    }

    private fun askForDiscoverability() {
        val requestCode = 1;
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 100)
        }
        startActivityForResult(discoverableIntent, requestCode)
        val thread = AcceptThread(this)
        thread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CONNEXION", "ServerConnexion finished")
    }
}