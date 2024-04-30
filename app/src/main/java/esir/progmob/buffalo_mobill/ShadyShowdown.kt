package esir.progmob.buffalo_mobill

import android.app.Activity
import android.app.AlertDialog
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
    private var lux : Int = 0 // On veut une valeur entre 0 et 35000
    private var delta : Int = 2 // tolérance entre la valeur du capteur et celle attendue
    private var init : Boolean = false // dit si on a déjà initialisé la valeur souhaitée

    // pour placer l'image
    private var screenWidth = 0
    private var screenHeight = 0

    // scores pour l'affichage une fois le jeu fini
    private var score = 0
    private var scoreAdversaire = 0
    var scoreSent : Boolean = false

    // pour le multijoueur
    private var isServer : Boolean = false
    private var isMulti : Boolean = false
    private var isReady : Boolean = false
    private var isAdversaireReady : Boolean = false

    private var discover : Boolean = false // si le bandit est visible
    private var gameBegan: Boolean = false // si le jeu a commencé
    private lateinit var alertDialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shady_showdown)
        // Récupération des informations
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        // Initialisation des capteurs
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        // Register sensors
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)

        // Initialisation des éléments graphiques
        bandit = findViewById<ImageView>(R.id.bandit)
        backgroundView = findViewById<LinearLayout>(R.id.background)

        if (!isMulti) {
            // Affiche la boîte de dialogue lorsque l'activité est créée
            val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame3), "JOUER") {
                startGame()
                alertDialog.dismiss()
                gameBegan = true
            }
            alertDialog = customAlertDialog.create()
            alertDialog.show()
        } else {
            initMulti()
            if (!isServer) {
                val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame3), "JOUER") {
                    isReady = true
                    if (isAdversaireReady) {
                        Multiplayer.Exchange.dataExchangeClient.write("Ready")
                        alertDialog.dismiss()
                        gameBegan = true
                        startGame()
                    }
                }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            } else {
                val customAlertDialog =
                    AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame3), "JOUER") {
                        isReady = true
                        Multiplayer.Exchange.dataExchangeServer.write("Ready")
                    }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            }
        }
    }

    private fun startGame() {
        bandit.setOnClickListener {
            if (discover) {
                // Victoire
                score = 10 // Score du joueur
                if (isMulti) victoryMulti(isServer)
                else {
                    val intent = Intent(this, ScorePage::class.java)
                    intent.putExtra("score", score)
                    intent.putExtra("game", "ShadyShowdown")
                    startActivityForResult(intent, 1)
                }
            }
            gameBegan = true
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
    private fun initMulti() {
        if (isServer) {
            // Initialisation du nouvel handler pour le thread d'échange de données
            val handlerServer = object :
                Handler(Looper.getMainLooper()) { // quand on reçoit un message on lance l'activité
                override fun handleMessage(msg: Message) {
                    val message = msg.obj.toString()
                    Log.d("DATAEXCHANGE", "[ShadyShowdown Server] Message received: " + msg.obj.toString())
                    if (message.contains("lux")) { // On récupère la valeur voulue de la luminosité
                        lux = message.split(":")[1].toInt()
                        Log.d("SENSOR", "[Server] Valeur voulue : $lux")
                    } else if (msg.obj.toString().contains("Ready")) {
                        Log.d("DATAEXCHANGE", "[ShadyShowdown] On peut lancer le jeu")
                        alertDialog.dismiss()
                        gameBegan = true
                        startGame()
                    } else {
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
                        Log.d("DATAEXCHANGE", "[ShadyShowdown Server] On lance la page de score")
                        startActivityForResult(intent, 1)
                    }
                }
            }
            Log.d("DATAEXCHANGE", "Thread server relaunched")
            Multiplayer.Exchange.dataExchangeServer.cancel()
            Multiplayer.Exchange.dataExchangeServer = DataExchange(handlerServer)
            Multiplayer.Exchange.dataExchangeServer.start()
        } // on met à jour le handler
        if (!isServer) {
            val handlerClient = object :
                Handler(Looper.getMainLooper()) { // quand on reçoit un message on lance l'activité
                override fun handleMessage(msg: Message) {
                    Log.d("DATAEXCHANGE", "[ShadyShowdown Client] Message received: " + msg.obj.toString())
                    if (msg.obj.toString().contains("Ready")) {
                        isAdversaireReady = true
                        if (isReady) {
                            Multiplayer.Exchange.dataExchangeClient.write("Ready")
                            alertDialog.dismiss()
                            gameBegan = true
                            startGame()
                        }
                    } else {
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
                        Log.d("DATAEXCHANGE", "[ShadyShowdown Client] On lance la page de score")
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

    private fun placeImage() {
        val x = Random.nextInt(screenWidth - 70)
        val y = Random.nextInt(screenHeight - 70)
        bandit.x = x.toFloat()
        bandit.y = y.toFloat()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0].toInt() // lumière initiale
            if (!init && !isServer && gameBegan) {
                lux = Random.nextInt(lightValue) + 1 // On veut une valeur entre 1 et la luminosité actuelle
                Log.d("SENSOR", "[Client] Valeur voulue : $lux")
                if (isMulti) {
                    Multiplayer.Exchange.dataExchangeServer.write("lux:$lux") // On envoie la valeur au serveur
                }
                init = true
            } else {
                updateOpacity(lightValue)
                if (!discover) Log.d("SENSOR", lightValue.toString())
            }
        }
    }

    private fun updateOpacity(lightValue : Int) {
        if (lux - delta < lightValue && lightValue < lux + delta) {
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
        Log.d("DATAEXCHANGE", "[ShadyShowdown] onActivityResult, resultCode : $resultCode")
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

