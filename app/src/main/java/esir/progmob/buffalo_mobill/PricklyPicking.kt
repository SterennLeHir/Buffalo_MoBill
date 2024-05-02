package esir.progmob.buffalo_mobill

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.animation.doOnEnd
import java.lang.Math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class PricklyPicking : ComponentActivity() {

    private val context = this
    //private lateinit var gest: GestureDetector
    private var isDragging = false
    private var lastX = 0f
    private var lastY = 0f
    private val seuilDist = 150


    private val MAX_PRICKLES = 10

    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var parentView : RelativeLayout

    // scores pour l'affichage une fois le jeu fini
    private var score = 0
    private var scoreAdversaire = 0
    var scoreSent : Boolean = false

    // pour le multijoueur
    private var isServer : Boolean = false
    private var isMulti : Boolean = false
    private var isReady : Boolean = false
    private var isAdversaireReady : Boolean = false

    private lateinit var alertDialog : AlertDialog // boîte de dialogue pour les règles du jeux
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Récupération des informations
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        if (!isMulti) {
            // Affiche la boîte de dialogue lorsque l'activité est créée
            val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame6), "JOUER") {
                startGame()
                alertDialog.dismiss()
            }
            alertDialog = customAlertDialog.create()
            alertDialog.show()
        } else {
            initMulti()
            if (!isServer) {
                val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame6), "JOUER") {
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
                    AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame6), "JOUER") {
                        isReady = true
                        Multiplayer.Exchange.dataExchangeServer.write("Ready")
                    }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            }
        }
    }

    private fun startGame() {
        setContentView(R.layout.prickly_picking)
        // Initialisation de la taille de l'écran
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        parentView = findViewById<RelativeLayout>(R.id.cowboyParentView)

        //Listener gesture
        //gest = GestureDetector(this, PrickleGestureListener())

        //On instancie nos épines
        for (i in 0 until MAX_PRICKLES) {
            createPrickle()
        }
    }

    private fun handleTouchEvent(view: View, event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                lastX = x
                lastY = y
                Log.d("", "onDown $lastX $lastY")
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) {
                    val distance = sqrt((abs(lastX - x) + abs(lastY - y)).pow(2)) //distance entre doigt et epine
                    val angle = calculateAngle(lastX,lastY,x,y) //angle entre doigt et epine

                    val rotation = if(view.rotation <= 0f) view.rotation + 360f else view.rotation//rotation de l'épine
                    val angleMinToHave = if(rotation  + 170f >= 360f) rotation  + 170f - 360f else rotation  + 170f
                    val angleMaxToHave = if(rotation + 190f >= 360f) rotation  + 190f - 360f else rotation + 190f
                    val isGoodAngle = angle in angleMinToHave..angleMaxToHave

                    //Log.d("", "angle $angle rot $rotation min $angleMinToHave max $angleMaxToHave")

                    if(distance >= seuilDist && isGoodAngle){
                        isDragging = true
                    }
                    /*
                    Log.d("points", "$lastX $lastY $x $y ")
                    Log.d("angle", ""+ calculateAngle(lastX,lastY,x,y))
                    */

                }
                else{ //on déplace la vue
                    // Lorsque l'utilisateur déplace le doigt, calculez le décalage et déplacez la vue
                    val deltaX = x - lastX
                    val deltaY = y - lastY

                    view.translationX += deltaX
                    view.translationY += deltaY
                    // Mettez à jour les dernières coordonnées
                    lastX = x
                    lastY = y
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    val animator = ObjectAnimator.ofFloat(view, "translationY", view.y, 2000f) //fait tomber l'épine
                    animator.duration = 500
                    animator.start()
                    animator.doOnEnd {
                        parentView.removeView(view) //on tue la vue
                    }
                    isDragging = false
                    score++
                    if(score >= MAX_PRICKLES){
                        if (!isMulti) {
                            val intent = Intent(this, ScorePage::class.java)
                            intent.putExtra("score", score)
                            intent.putExtra("game", "PricklyPicking")
                            startActivityForResult(intent, 1)
                        } else {
                            scoreSent = if (isServer) {
                                Multiplayer.Exchange.dataExchangeServer.write(score.toString())
                                true
                            } else {
                                Multiplayer.Exchange.dataExchangeClient.write(score.toString())
                                true
                            }
                        }
                    }
                }
            }
        }
        return true
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
                        scoreAdversaire = msg.obj.toString().toInt()
                        if (!scoreSent) {
                            Multiplayer.Exchange.dataExchangeServer.write(score.toString()) // On envoie 10 car c'est le score quand on gagne
                            scoreSent = true
                        }
                        val intent = Intent(this@PricklyPicking, ScorePage::class.java)
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
                            Multiplayer.Exchange.dataExchangeClient.write(score.toString())
                            scoreSent = true
                        }
                        val intent = Intent(this@PricklyPicking, ScorePage::class.java)
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

    private fun createPrickle() {
        //On crée notre image view epine
        val prickle = ImageView(context)

        //On set son image
        prickle.setImageResource(R.drawable.epine)

        //On set sa taille
        prickle.layoutParams = ViewGroup.LayoutParams(200, 200)

        //On set sa position
        prickle.x = Random.nextInt(screenWidth / 4, 2 * screenWidth / 4).toFloat()
        prickle.y = Random.nextInt(3 * screenHeight / 6, 4 * screenHeight / 6)
            .toFloat() //plus haut que le lasso
        parentView.addView(prickle)

        prickle.setOnTouchListener { view, event ->
            handleTouchEvent(view, event)
        }

        //Sa rotation
        prickle.rotation = calculateAngle(prickle.x, prickle.y,screenWidth / 3f, screenHeight / 2f) //empirique
    }
    fun calculateAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        // Calcul de la différence en x et en y entre les deux points
        val deltaX = x2 - x1
        val deltaY = y2 - y1

        // Utilisation de la fonction atan2 pour obtenir l'angle en radians
        val angleRadians = atan2(deltaY, deltaX)

        // Conversion de l'angle en radians en degrés
        var angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()
        angleDegrees = angleDegrees + 90 //Pour décaler l'origine 0°
        // Ajustement de l'angle pour qu'il soit compris entre 0 et 360 degrés
        if (angleDegrees < 0) {
            angleDegrees += 360f
        }

        return angleDegrees
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DATAEXCHANGE", "[PricklyPicking] onActivityResult, resultCode : $resultCode")
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