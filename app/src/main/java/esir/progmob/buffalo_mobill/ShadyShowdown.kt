package esir.progmob.buffalo_mobill

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import kotlin.random.Random


class ShadyShowdown : ComponentActivity(), SensorEventListener {

    // éléments graphiques
    private lateinit var bandit : ImageView
    private lateinit var backgroundView : LinearLayout

    // capteurs
    private lateinit var sensorManager : SensorManager
    private var light: Sensor? = null
    private var lux : Float = 0F // On veut une valeur entre 0 et 35000
    private var delta : Float = 0F // tolérance entre la valeur du capteur et celle attendue
    private var init : Boolean = false // dit si on a déjà initialisé la valeur souhaitée
    // pour placer l'image
    private var screenWidth = 0
    private var screenHeight = 0

    private var discover : Boolean = false // si le bandit est visible
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shady_showdown)

        // Initialisation des capteurs
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        // Register sensors
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)

        // Initialisation des éléments graphiques
        bandit = findViewById<ImageView>(R.id.bandit)
        backgroundView = findViewById<LinearLayout>(R.id.background)
        bandit.setOnClickListener {
            if (discover) {
                // Victoire
                // ajouter l'activité de fin
                finish()
            }
        }
        // Initialisation de la taille de l'écran
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        placeImage()
    }

    private fun placeImage() {
        val x = Random.nextInt(screenWidth - 50)
        val y = Random.nextInt(screenHeight - 50)
        bandit.x = x.toFloat()
        bandit.y = y.toFloat()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            if (!init) {
                lux = Random.nextFloat()*lightValue
                delta = 0.1F*lux
                Log.d("SENSOR", "Valeur voulue : $lux")
                Log.d("SENSOR", "Delta voulue : $delta")
                init = true
            } else {
                updateOpacity(lightValue)
                if (!discover) Log.d("SENSOR", lightValue.toString())
            }

        }
    }

    fun updateOpacity(lightValue : Float) {
        if (lux - delta < lightValue && lux + delta > lightValue) {
            bandit.visibility = ImageView.VISIBLE
            discover = true
        }
    }

    override fun onResume() {
        super.onResume()
        // Register sensors
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        // Unregister all listeners
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }

}

