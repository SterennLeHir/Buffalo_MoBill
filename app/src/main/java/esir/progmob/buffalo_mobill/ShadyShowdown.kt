package esir.progmob.buffalo_mobill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
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

    // scores pour l'affichage une fois le jeu fini
    private var score = 0
    private var scoreAdversaire = 0
    var scoreSent : Boolean = false

    private var discover : Boolean = false // si le bandit est visible
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shady_showdown)
        // Récupération des informations
        val isMulti = intent.getBooleanExtra("isMulti", false)
        val isServer = intent.getBooleanExtra("isServer", false)
        // Initialisation des capteurs
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        // Register sensors
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)

        // Initialisation des éléments graphiques
        bandit = findViewById<ImageView>(R.id.bandit)
        backgroundView = findViewById<LinearLayout>(R.id.background)

        if (isMulti) initMulti(isServer)
        bandit.setOnClickListener {
            if (discover) {
                // Victoire
                score = 10 // Score du joueur
                if (isMulti) victoryMulti(isServer)
                else {
                    val intent = Intent(this, ScorePage::class.java)
                    intent.putExtra("score", score)
                    intent.putExtra("isMulti", false)
                    intent.putExtra("isServer", false)
                    startActivityForResult(intent, 1) // test
                    finish()
                }
            }
        }
        // Initialisation de la taille de l'écran
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        placeImage()
    }

    private fun victoryMulti(isServer: Boolean) {
        if (isServer) { // même handler donc en soit on n'a pas besoin de distinguer
            Log.d("DATAEXCHANGE", "Serveur envoie le score")
            Multiplayer.Exchange.dataExchangeServer.write(score.toString()) // on envoie le score à l'autre joueur
        } else {
            Log.d("DATAEXCHANGE", "Client envoie le score")
            Multiplayer.Exchange.dataExchangeClient.write(score.toString()) // on envoie le score à l'autre joueur
        }
        scoreSent = true
    }

    /**
     * Initialise le thread d'échange de données dans le mode multijoueurs
     */
    private fun initMulti(isServer: Boolean) {
        // Initialisation du nouvel handler pour le thread d'échange de données
        val handlerServer = object :
            Handler(Looper.getMainLooper()) { // quand on reçoit un message on lance l'activité
            override fun handleMessage(msg: Message) {
                Log.d("DATAEXCHANGE", "[ShadyShowdown Server] Message received: " + msg.obj.toString())
                // Quand on reçoit le score de l'adversaire on peut afficher la page de score
                scoreAdversaire = msg.obj.toString().toInt()
                if (!scoreSent) {
                    Multiplayer.Exchange.dataExchangeServer.write(score.toString())
                    scoreSent = true
                }
                val intent = Intent(this@ShadyShowdown, ScorePage::class.java)
                intent.putExtra("score", score)
                intent.putExtra("scoreAdversaire", scoreAdversaire)
                intent.putExtra("isMulti", true)
                intent.putExtra("isServer", isServer)
                Log.d("DATAEXCHANGE", "[Server] On lance la page de score")
                startActivityForResult(intent, 1)
            }
        }
        val handlerClient = object :
            Handler(Looper.getMainLooper()) { // quand on reçoit un message on lance l'activité
            override fun handleMessage(msg: Message) {
                Log.d("DATAEXCHANGE", "[ShadyShowdown Client] Message received: " + msg.obj.toString())
                // Quand on reçoit le score de l'adversaire on peut afficher la page de score
                scoreAdversaire = msg.obj.toString().toInt()
                if (!scoreSent) {
                    Multiplayer.Exchange.dataExchangeClient.write(score.toString())
                    scoreSent = true
                }
                val intent = Intent(this@ShadyShowdown, ScorePage::class.java)
                intent.putExtra("score", score)
                intent.putExtra("scoreAdversaire", scoreAdversaire)
                intent.putExtra("isMulti", true)
                intent.putExtra("isServer", isServer)
                Log.d("DATAEXCHANGE", "[Client] On lance la page de score")
                startActivityForResult(intent, 1)
            }
        }
        if (isServer) {
            Log.d("DATAEXCHANGE", "Thread server relaunched")
            Multiplayer.Exchange.dataExchangeServer.cancel()
            Multiplayer.Exchange.dataExchangeServer = DataExchange(handlerServer)
            Multiplayer.Exchange.dataExchangeServer.start()
        } // on met à jour le handler
        if (!isServer) {
            Log.d("DATAEXCHANGE", "Thread client relaunched")
            Multiplayer.Exchange.dataExchangeClient.cancel()
            Multiplayer.Exchange.dataExchangeClient = DataExchange(handlerClient)
            Multiplayer.Exchange.dataExchangeClient.start()
        } // on met à jour le handler
    }

    private fun placeImage() {
        val x = Random.nextInt(screenWidth - 70)
        val y = Random.nextInt(screenHeight - 70)
        bandit.x = x.toFloat()
        bandit.y = y.toFloat()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0] // lumière initiale
            if (!init) {
                lux = Random.nextFloat()*lightValue + 1 // On veut une valeur entre 1 et la luminosité actuelle
                delta = 2F // à tester
                Log.d("SENSOR", "Valeur voulue : $lux")
                Log.d("SENSOR", "Delta voulue : $delta")
                init = true
            } else {
                updateOpacity(lightValue)
                if (!discover) Log.d("SENSOR", lightValue.toString())
            }

        }
    }

    private fun updateOpacity(lightValue : Float) {
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
        Log.d("DATAEXCHANGE", "ShadyShowdown finished")
        sensorManager.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DATAEXCHANGE", "[ShadyShowdown] onActivityResult")
        Log.d("DATAEXCHANGE", "[ShadyShowdown] onActivityResult, resultCode : $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            Log.d("DATAEXCHANGE", "score :" + data?.getIntExtra("score", 0).toString())
            Log.d("DATAEXCHANGE", "scoreAdversaire :" + data?.getIntExtra("scoreAdversaire", 0).toString())
            score = data?.getIntExtra("score", 0) ?: 0
            scoreAdversaire = data?.getIntExtra("scoreAdversaire", 0) ?: 0
            Log.d("DATAEXCHANGE", "score : $score, scoreAdversaire : $scoreAdversaire")
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            resultIntent.putExtra("scoreAdversaire", scoreAdversaire)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}

