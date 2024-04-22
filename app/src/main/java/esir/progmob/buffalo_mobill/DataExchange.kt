package esir.progmob.buffalo_mobill

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.activity.ComponentActivity
import android.os.Handler

class DataExchange(private var handler : Handler?) : Thread() {

    private val inputStream = Multiplayer.SocketHolder.socket!!.inputStream
    private val outputStream = Multiplayer.SocketHolder.socket!!.outputStream
    private var shouldLoop = true

    override fun run() { // écoute et récupération des données
        Log.d("DATAEXCHANGE","DataExchange thread running");
        while (shouldLoop) {
            try {
                val available = inputStream.available()
                if (available != 0) {
                    val bytes = ByteArray(available)
                    inputStream.read(bytes, 0, available)
                    val incomingMessage = String(bytes)
                    Log.d("DATAEXCHANGE", "InputStream: $incomingMessage")
                    handler?.obtainMessage(0, incomingMessage)?.sendToTarget()
                }
            } catch (e: Exception) {
                Log.e("DATAEXCHANGE", "Error receiving data", e)
                break
            } /*finally { à ne pas mettre ici
                Log.d("DATAEXCHANGE", "DataExchange thread ended");
                outputStream.close()
                inputStream.close()
                socket.close()
            }*/
        }
        Log.d("DATAEXCHANGE", "DataExchange thread ended")
    }

    /**
     * Write to the connected OutStream.
     * @param message The bytes to write
     **/
    fun write(message : String) {
        try {
            outputStream.write(message.toByteArray())
            outputStream.flush() // à voir l'utilité
            Log.d("DATAEXCHANGE", "Message sent")
        } catch (e: Exception) {
            Log.e("DATAEXCHANGE", "Error sending data", e)
        }
    }

    fun cancel() {
        shouldLoop = false
    }

    fun setHandler(handler: Handler) {
        Log.d("DATAEXCHANGE", "Handler set")
        this.handler = handler
    }
}