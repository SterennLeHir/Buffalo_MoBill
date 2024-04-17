package esir.progmob.buffalo_mobill

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.activity.ComponentActivity
import android.os.Handler

class DataExchange(private val activity : ComponentActivity, private val socket: BluetoothSocket, private val handler : Handler) : Thread() {

    private val inputStream = socket.inputStream
    private val outputStream = socket.outputStream

    override fun run() {
        Log.d("DATAEXCHANGE","DataExchange thread launched");
        while (true) {
            if (activity.isFinishing) {
                Log.d("DATAEXCHANGE", "Activity is finishing or destroyed");
                return
            }
            try {
                val available = inputStream.available()
                if (available != 0) {
                    Log.d("DATAEXCHANGE", "Available: $available")
                    Log.d("DATAEXCHANGE", "On continue")
                    val bytes = ByteArray(available)
                    Log.d("DATAEXCHANGE", "Reading")
                    inputStream.read(bytes, 0, available)
                    val incomingMessage = String(bytes)
                    Log.d("DATAEXCHANGE", "InputStream: $incomingMessage")
                    handler.obtainMessage(0, incomingMessage).sendToTarget()
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
}