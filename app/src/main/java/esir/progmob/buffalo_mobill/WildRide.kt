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
import android.widget.Toast

class WildRide : ComponentActivity(), SensorEventListener  {
        private lateinit var sensorManager: SensorManager
        private lateinit var accelerometer: Sensor
        private lateinit var countDownTimer: CountDownTimer
        private var totalPoints = 0

        private lateinit var rodeo: ImageView
        private var rotRight: Boolean = true //pour savoir si on tourne à droite ou gauche
        private var selfIncline: Int = 0 //0 milieu, -1 = gauche, 1 = droite
        private val context = this


    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.wild_ride)

            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

            rodeo = findViewById(R.id.rodeoView)

            startRotationGame()
        }

        private fun startRotationGame() {
            countDownTimer = object : CountDownTimer(20000, 2000) { // 20 secondes, rotation toutes les 2 secondes
                override fun onTick(millisUntilFinished: Long) {
                    rotateImage()
                }

                override fun onFinish() {
                    // Le jeu est terminé
                    Toast.makeText(context,"Jeu terminé! Points totaux: $totalPoints",Toast.LENGTH_SHORT).show()
                }
            }
            countDownTimer.start()
        }

        private fun rotateImage() {
            // Rotation aléatoire vers la droite ou la gauche
            rotRight = if (Math.random() < 0.5) true else false
            val rotation = if (rotRight) 45f else -45f

            // Créer une animation de rotation de 45 degrés
            val rotateAnimation = RotateAnimation(0f, rotation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotateAnimation.duration = 1500 // Durée de l'animation: 1,5 seconde
            rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    if ((rotRight && selfIncline == 1) || (!rotRight && selfIncline == -1)){
                        totalPoints++
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
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
                selfIncline = -1
                println("Incliné vers la gauche!")
            }
            // Si incliné vers la gauche
            else if (inclination < -7) {
                selfIncline = 1
                println("Incliné vers la droite!")
            }
        }
    }