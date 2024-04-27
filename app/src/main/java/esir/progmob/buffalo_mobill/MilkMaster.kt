package esir.progmob.buffalo_mobill

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MilkMaster : ComponentActivity() {

    private var compteurLait = 0
    private val MAX_LAIT = 10
    private var fini = false
    private var isMilking = false // indique que le joueur est en train de traire la vache
    private lateinit var alertDialog : AlertDialog

    // scores pour l'affichage une fois le jeu fini
    private var score = 0
    private var scoreAdversaire = 0
    var scoreSent : Boolean = false

    // pour le multijoueur
    private var isServer : Boolean = false
    private var isMulti : Boolean = false
    private var isReady : Boolean = false
    private var isAdversaireReady : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.milk_master)
        // Récupération des informations
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)

        if (!isMulti) {
            // Affiche la boîte de dialogue lorsque l'activité est créée
            val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame1), "JOUER") {
                startGame()
                alertDialog.dismiss()
            }
            alertDialog = customAlertDialog.create()
            alertDialog.show()
        } else {
            initMulti()
            if (!isServer) {
                val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame1), "JOUER") {
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
                    AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame1), "JOUER") {
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
                    Log.d("DATAEXCHANGE", "[ShadyShowdown Server] Message received: " + msg.obj.toString())
                    if (msg.obj.toString().contains("Ready")) {
                        Log.d("DATAEXCHANGE", "[ShadyShowdown] On peut lancer le jeu")
                        alertDialog.dismiss()
                        startGame()
                    } else {
                        // Quand on reçoit le score de l'adversaire on peut afficher la page de score
                        scoreAdversaire = msg.obj.toString().toInt()
                        if (!scoreSent) {
                            Multiplayer.Exchange.dataExchangeServer.write(compteurLait.toString())
                            scoreSent = true
                        }
                        val intent = Intent(this@MilkMaster, ScorePage::class.java)
                        intent.putExtra("score", compteurLait)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[MilkMaster Server] On lance la page de score")
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
                    Log.d("DATAEXCHANGE", "[MilkMaster Client] Message received: " + msg.obj.toString())
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
                            Multiplayer.Exchange.dataExchangeClient.write(compteurLait.toString())
                            scoreSent = true
                        }
                        val intent = Intent(this@MilkMaster, ScorePage::class.java)
                        intent.putExtra("score", compteurLait)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[MilkMaster Client] On lance la page de score")
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

    @SuppressLint("ClickableViewAccessibility")
    private fun startGame() {
        val mediaPlayerMilk : MediaPlayer = MediaPlayer.create(this, R.raw.lait) // Son de lait qui coule
        val mediaPlayerCow : MediaPlayer = MediaPlayer.create(this, R.raw.meuh) // Son de la vache énervée
        val piesView: ImageView = findViewById(R.id.pies)
        val seauView: ImageView = findViewById(R.id.seau)
        var waiting = false
        piesView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // L'utilisateur a appuyé sur l'écran
                    Log.d("SENSOR", "ACTION_DOWN")
                    if (mediaPlayerMilk.isPlaying) {
                        mediaPlayerCow.start()
                        Toast.makeText(this, "Vous allez trop vite et brusquez la vache", Toast.LENGTH_SHORT).show()
                        waiting = true
                        piesView.isActivated = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            waiting = false
                            mediaPlayerCow.pause()
                        }, 2000) // Délai de 2 secondes
                    } else {
                        true
                    }

                }
                MotionEvent.ACTION_MOVE -> {
                    // L'utilisateur a déplacé son doigt sur l'écran
                    Log.d("SENSOR", "ACTION_MOVE")
                    if (!waiting) isMilking = true
                    if (!mediaPlayerMilk.isPlaying && !waiting) {
                        mediaPlayerMilk.start()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // L'utilisateur a levé son doigt de l'écran
                    Log.d("SENSOR", "ACTION_UP")
                    if (compteurLait < MAX_LAIT && !waiting && isMilking) { // il faut attendre la fin du son pour pouvoir cliquer à nouveau mais ne marche pas
                        compteurLait++
                        mediaPlayerMilk.start()
                    } else if (compteurLait >= MAX_LAIT && !fini){
                        Toast.makeText(this, "C'est fini !", Toast.LENGTH_SHORT).show()
                        fini = true
                        seauView.setImageDrawable(resources.getDrawable(R.drawable.seau_rempli))
                        if (isMulti) {
                            if (isServer) {
                                Multiplayer.Exchange.dataExchangeServer.write(compteurLait.toString())
                                scoreSent = true
                            } else {
                                Multiplayer.Exchange.dataExchangeClient.write(compteurLait.toString())
                                scoreSent = true
                            }
                        } else {
                            Log.d("DATAEXCHANGE", "[MilkMaster Solo] On lance la page de score")
                            val intent = Intent(this, ScorePage::class.java)
                            intent.putExtra("score", compteurLait)
                            intent.putExtra("isMulti", false)
                            startActivityForResult(intent, 1)
                        }
                    }
                    isMilking = false
                    true
                }
                else -> false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DATAEXCHANGE", "[MilkMaster] onActivityResult, resultCode : $resultCode")
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