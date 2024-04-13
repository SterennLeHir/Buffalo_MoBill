package esir.progmob.buffalo_mobill

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BluetoothPermissions : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    //private var answer: Boolean = false // vérifie si l'utilisateur a accepté ou rejeté les permissions
    private var bluetoothAccepted = false
    private var localisationAccepted = false
    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_BLUETOOTH = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiplayers)
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



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
                    Toast.makeText(this, "Vous ne pouvez pas jouer en multijoueurs", Toast.LENGTH_SHORT).show();
                }
            }
            // D'autres cas de requêtes de permissions peuvent être gérés ici si nécessaire
        }
        return
    }

    @SuppressLint("MissingPermission")
    fun enablingBluetooth() {
        // On active le bluetooth
        if (!bluetoothAdapter.isEnabled) {
            Log.d("BLUETOOTH", "Activation du bluetooth")
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            // here to request the missing permissions, and then overriding
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
        }

        val buttonServer : Button = findViewById<Button>(R.id.server)
        val buttonClient : Button = findViewById<Button>(R.id.client)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        buttonServer.setOnClickListener {
            // TODO différencier les téléphones qui ont besoin de la localisation de ceux qui n'en ont pas besoin
            // On vérifie que la localisation est activée
            // On active la localisation
            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isLocationEnabled) {
                /* intrusif et mache mal avec le retour (va dans les paramètres
                val enableLocalisationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableLocalisationIntent, 1)
                */
                Toast.makeText(this, "Veuillez activer la localisation", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ServerConnexion::class.java)
                startActivity(intent)
                finish()
            }
        }
        buttonClient.setOnClickListener {
            // On vérifie que la localisation est activée
            // On active la localisation
            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isLocationEnabled) {
                /* intrusif et mache mal avec le retour (va dans les paramètres
                val enableLocalisationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableLocalisationIntent, 1)
                */
                Toast.makeText(this, "Veuillez activer la localisation", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ClientConnexion::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}