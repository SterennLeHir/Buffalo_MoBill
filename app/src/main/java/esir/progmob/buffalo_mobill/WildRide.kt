package esir.progmob.buffalo_mobill

import androidx.activity.ComponentActivity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
class WildRide : ComponentActivity(), SensorEventListener  {
        private lateinit var sensorManager: SensorManager
        private lateinit var accelerometer: Sensor
        private lateinit var rodeo: ImageView
        private lateinit var countDownTimer: CountDownTimer
        private var rotationCount = 0
        private var totalPoints = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.wild_ride)

            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

            rodeo = findViewById(R.id.rodeoView)

            startRotationGame()
        }

        private fun startRotationGame() {
            countDownTimer = object : CountDownTimer(30000, 6000) { // 30 secondes, rotation toutes les 6 secondes
                override fun onTick(millisUntilFinished: Long) {
                    rotateImage()
                }

                override fun onFinish() {
                    // Le jeu est terminé
                    println("Jeu terminé! Points totaux: $totalPoints")
                }
            }
            countDownTimer.start()
        }

        private fun rotateImage() {
            // Rotation aléatoire vers la droite ou la gauche
            val rotation = if (Math.random() < 0.5) 45f else -45f

            // Créer une animation de rotation de 90 degrés vers la droite
            val rotateAnimation = RotateAnimation(0f, rotation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotateAnimation.duration = 1000 // Durée de l'animation: 1 seconde
            // Démarrer l'animation sur l'image
            rodeo.startAnimation(rotateAnimation)
        }

        override fun onResume() {
            super.onResume()
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        override fun onPause() {
            super.onPause()
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            // Vérifier si le téléphone est penché vers la droite ou la gauche
            val inclination = event?.values?.get(0) ?: return

            // Si incliné vers la droite
            if (inclination > 7) {
                println("Incliné vers la gauche! Points: $totalPoints")
            }
            // Si incliné vers la gauche
            else if (inclination < -7) {
                println("Incliné vers la droite! Points: $totalPoints")
            }
        }
    }