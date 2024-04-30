package esir.progmob.buffalo_mobill

import android.app.Activity
import android.app.AlertDialog
import androidx.activity.ComponentActivity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Vibrator
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isInvisible

class WildRide : ComponentActivity(), SensorEventListener  {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private lateinit var rodeo: ImageView
    private lateinit var bang: ImageView

    //inclinaisons
    private var selfIncline: Int = 0 //0 milieu, -1 = gauche, 1 = droite
    private var rodIncline: Int = 0
    private var fromRotation: Float = 0f
    private var toRotation: Float = 0f //correspond à la valeur de rotation en degree

    private var shake_counter = 0
    private val MAX_SHAKE = 10

    //vibrator
    private var vibrator: Vibrator? = null

    private var dureeAnim = 2000 //durée d'un aller retour

    // scores pour l'affichage une fois le jeu fini
    private var score = 0
    private var scoreAdversaire = 0
    var scoreSent : Boolean = false

    // pour le multijoueur
    private var isServer : Boolean = false
    private var isMulti : Boolean = false
    private var isReady : Boolean = false
    private var isAdversaireReady : Boolean = false
    private var isFinished = false

    private lateinit var alertDialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        // Récupération des informations
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        if (!isMulti) {
            // Affiche la boîte de dialogue lorsque l'activité est créée
            val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame2), "JOUER") {
                startGame()
                alertDialog.dismiss()
            }
            alertDialog = customAlertDialog.create()
            alertDialog.show()
        } else {
            initMulti()
            if (!isServer) {
                val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame2), "JOUER") {
                    isReady = true
                    if (isAdversaireReady) {
                        Multiplayer.Exchange.dataExchangeClient.write("Ready")
                        alertDialog.dismiss()
                        startGame()
                    }
                }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            } else {
                val customAlertDialog =
                    AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame2), "JOUER") {
                        isReady = true
                        Multiplayer.Exchange.dataExchangeServer.write("Ready")
                    }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            }
        }
    }

    private fun initMulti() {
        if (isServer) {
            // Initialisation du nouvel handler pour le thread d'échange de données
            val handlerServer = object :
                Handler(Looper.getMainLooper()) { // quand on reçoit un message on lance l'activité
                override fun handleMessage(msg: Message) {
                    val message = msg.obj.toString()
                    Log.d("DATAEXCHANGE", "[WildRide Server] Message received: " + msg.obj.toString())
                    if (msg.obj.toString().contains("Ready")) {
                        Log.d("DATAEXCHANGE", "[WildRide] On peut lancer le jeu")
                        alertDialog.dismiss()
                        startGame()
                    } else {
                        // Quand on reçoit le score de l'adversaire on peut afficher la page de score
                        isFinished = true
                        scoreAdversaire = msg.obj.toString().toInt()
                        if (!scoreSent) {
                            score = 10 // score quand on gagne
                            Multiplayer.Exchange.dataExchangeServer.write(score.toString()) // On envoie 10 car c'est le score quand on gagne
                            scoreSent = true
                        }
                        val intent = Intent(this@WildRide, ScorePage::class.java)
                        intent.putExtra("score", score)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[WildRide Server] On lance la page de score")
                        startActivityForResult(intent, 1)
                    }
                }
            }
            Log.d("DATAEXCHANGE", "Thread server relaunched")
            Multiplayer.Exchange.dataExchangeServer.cancel()
            Multiplayer.Exchange.dataExchangeServer = DataExchange(handlerServer)
            Multiplayer.Exchange.dataExchangeServer.start()
        } else {
            val handlerClient = object :
                Handler(Looper.getMainLooper()) { // quand on reçoit un message on lance l'activité
                override fun handleMessage(msg: Message) {
                    Log.d("DATAEXCHANGE", "[WildRide Client] Message received: " + msg.obj.toString())
                    if (msg.obj.toString().contains("Ready")) {
                        isAdversaireReady = true
                        if (isReady) {
                            Multiplayer.Exchange.dataExchangeClient.write("Ready")
                            alertDialog.dismiss()
                            startGame()
                        }
                    } else {
                        // Quand on reçoit le score de l'adversaire on peut afficher la page de score
                        scoreAdversaire = msg.obj.toString().toInt()
                        if (!scoreSent) {
                            isFinished = true
                            score = 10 // score quand on gagne
                            Multiplayer.Exchange.dataExchangeClient.write(score.toString())
                            scoreSent = true
                        }
                        val intent = Intent(this@WildRide, ScorePage::class.java)
                        intent.putExtra("score", score)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[WildRide Client] On lance la page de score")
                        startActivityForResult(intent, 1)
                    }
                }
            }
            Log.d("DATAEXCHANGE", "Thread client relaunched")
            Multiplayer.Exchange.dataExchangeClient.cancel()
            Multiplayer.Exchange.dataExchangeClient = DataExchange(handlerClient)
            Multiplayer.Exchange.dataExchangeClient.start()
        } // on met à jour le handler
    }

    private fun startGame() {
        setContentView(R.layout.wild_ride)
        mediaPlayer = MediaPlayer.create(this, R.raw.cri)

        rodeo = findViewById(R.id.rodeoView)
        bang = findViewById(R.id.bangView)

        // vibrateur
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        rodeoShake()
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
                    if (rodIncline != selfIncline){ // si on ne s'oriente pas bien pas assez rapidement
                        gameOver()
                        return
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
                                    gameOver()
                                    return
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
            dureeAnim -= 70 //pour aller de plus en plus vite
        }
        else{
            // Le jeu est terminé
            score = shake_counter
            if (!isMulti) {
                val intent = Intent(this, ScorePage::class.java)
                intent.putExtra("score", score)
                intent.putExtra("isMulti", false)
                startActivityForResult(intent, 1)
            } else {
                if (isServer) {
                    Multiplayer.Exchange.dataExchangeServer.write(score.toString())
                    scoreSent = true
                } else {
                    Multiplayer.Exchange.dataExchangeClient.write(score.toString())
                    scoreSent = true
                }
            }
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

    private fun gameOver(){
        if (!isFinished) {
            mediaPlayer.start()
            vibrator?.vibrate(1000)
            bang.isInvisible = false
            if (isMulti) {
                scoreSent = if (isServer) {
                    Multiplayer.Exchange.dataExchangeServer.write(score.toString())
                    true
                } else {
                    Multiplayer.Exchange.dataExchangeClient.write(score.toString())
                    true
                }
            } else {
                val intent = Intent(this, ScorePage::class.java)
                intent.putExtra("score", score)
                intent.putExtra("game", "WildRide")
                startActivityForResult(intent, 1)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DATAEXCHANGE", "[WildRide] onActivityResult, resultCode : $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            // On récupère les scores
            score = data?.getIntExtra("score", 0) ?: 0
            scoreAdversaire = data?.getIntExtra("scoreAdversaire", 0) ?: 0
            Log.d("DATAEXCHANGE", "score : $score, scoreAdversaire : $scoreAdversaire")
            // On transmet les scores à GameList
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            resultIntent.putExtra("scoreAdversaire", scoreAdversaire)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}