package esir.progmob.buffalo_mobill

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.IntentFilter
import android.os.Bundle
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
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }
        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.d("CONNEXION", "Could not close the connect socket", e)
            }
        }
    }

    private fun manageMyConnectedSocket(it: BluetoothSocket) {
        Log.d("CONNEXION", "fonction de transfert de données")
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstantState : Bundle?) {
        super.onCreate(savedInstantState)
        setContentView(R.layout.bluetooth_server)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }
}