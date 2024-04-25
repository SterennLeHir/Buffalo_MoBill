package esir.progmob.buffalo_mobill

import androidx.activity.ComponentActivity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.Toast
import kotlin.random.Random

class WildRide : ComponentActivity(), SensorEventListener  {
        private lateinit var sensorManager: SensorManager
        private lateinit var accelerometer: Sensor
        private lateinit var countDownTimer: CountDownTimer

        private lateinit var rodeo: ImageView

        //inclinaisons
        private var selfIncline: Int = 0 //0 milieu, -1 = gauche, 1 = droite
        private var rodIncline: Int = 0
        private var fromRotation: Float = 0f
        private var toRotation: Float = 0f //correspond à la valeur de rotation en degree
        private var aller: Boolean = true

        private val context = this

        private var shake_counter = 0
        private val MAX_SHAKE = 5

        //vibrator
        private var vibrator: Vibrator? = null

        private var dureeAnim = 2000 //durée d'un aller retour


    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.wild_ride)

            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

            rodeo = findViewById(R.id.rodeoView)
            // vibrateur
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            startRotationGame()
        }

        private fun startRotationGame() {
            rodeoShake()
            /*
            countDownTimer = object : CountDownTimer(30000, 2000) { // 30 secondes, rotation toutes les 2 secondes
                override fun onTick(millisUntilFinished: Long) {
                    rodeoShake()
                }

                override fun onFinish() {
                    // Le jeu est terminé
                    Toast.makeText(context,"Jeu terminé!",Toast.LENGTH_SHORT).show()
                }
            }
            countDownTimer.start()

             */
        }

        private fun rodeoShake() {
            if(shake_counter < MAX_SHAKE){
                rodIncline = if (Math.random() < 0.5) 1 else -1 //une chance sur 2 d'aller à droite ou gauche
                val randomRot = 45f//Random.nextInt(15, 45).toFloat() //valeur aléatoire
                fromRotation = 0f
                toRotation = if (rodIncline==1) randomRot else -randomRot

                // Créer une animation de rotation
                val rotateAnimation = RotateAnimation(fromRotation, toRotation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotateAnimation.duration = dureeAnim.toLong()/2
                rotateAnimation.fillAfter = true // Garder l'image à sa position finale après l'animation
                rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        if (rodIncline != selfIncline){ //si on ne s'oriente pas bien pas assez rapidement
                            //countDownTimer.cancel()
                            Toast.makeText(context, "PERDUUUUUUU", Toast.LENGTH_SHORT)//ne s'affiche pas
                            //on vibre
                            vibrator?.vibrate(1000)
                        }
                        else{
                            // Créer une nouvelle animation de retour
                            rodIncline = 0
                            fromRotation = toRotation
                            toRotation = 0f //on va dans l'autre sens
                            val rotateAnimation = RotateAnimation(fromRotation, toRotation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                            rotateAnimation.duration = dureeAnim.toLong()/2
                            rotateAnimation.fillAfter = true // Garder l'image à sa position finale après l'animation
                            rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {}

                                override fun onAnimationEnd(animation: Animation?) {
                                    if (rodIncline != selfIncline){ //si on ne s'oriente pas bien pas assez rapidement
                                        //countDownTimer.cancel()
                                        Toast.makeText(context, "PERDUUUUUUU", Toast.LENGTH_SHORT)//ne s'affiche pas
                                        //on vibre
                                        vibrator?.vibrate(1000)
                                    }
                                    else{
                                        shake_counter++ //on a réussit à survivre au shake
                                        rodeoShake()
                                    }
                                }

                                override fun onAnimationRepeat(animation: Animation?) {}
                            })
                            // Démarrer l'animation sur l'image
                            rodeo.startAnimation(rotateAnimation)
                        }
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })

                // Démarrer l'animation sur l'image
                rodeo.startAnimation(rotateAnimation)
                dureeAnim = dureeAnim - 25 //pour aller de plus en plus vite
            }
            else{
                // Le jeu est terminé
                Toast.makeText(context,"Jeu terminé!",Toast.LENGTH_SHORT).show()
            }
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
                Log.d("","Incliné vers la gauche!")
            }
            // Si incliné vers la gauche
            else if (inclination < -7) {
                selfIncline = 1
                Log.d("","Incliné vers la droite!")
            }
            else{
                selfIncline = 0
                Log.d("","Pas incliné!")
            }
        }
    }