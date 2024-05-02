package esir.progmob.buffalo_mobill

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlin.random.Random


private const val DEBUG_TAG = "Gestures"

class CowCatcher : ComponentActivity(){

    private lateinit var parentView: FrameLayout
    private lateinit var lasso: ImageView
    private lateinit var cow: ImageView
    private lateinit var gest: GestureDetector
    private var screenWidth = 0
    private var screenHeight = 0
    private var context = this

    // scores pour l'affichage une fois le jeu fini
    private var score = 0
    private var scoreAdversaire = 0
    var scoreSent : Boolean = false

    // pour le multijoueur
    private var isServer : Boolean = false
    private var isMulti : Boolean = false
    private var isReady : Boolean = false
    private var isAdversaireReady : Boolean = false

    private lateinit var alertDialog : AlertDialog
    private lateinit var mediaPlayer : MediaPlayer

    //timer
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var timer : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        //Constructeur et récupération du layout
        super.onCreate(savedInstanceState)
        // Récupération des informations
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        if (!isMulti) {
            // Affiche la boîte de dialogue lorsque l'activité est créée
            val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame5), "JOUER") {
                startGame()
                alertDialog.dismiss()
            }
            alertDialog = customAlertDialog.create()
            alertDialog.show()
        } else {
            initMulti()
            if (!isServer) {
                val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame5), "JOUER") {
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
                    AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame5), "JOUER") {
                        isReady = true
                        Multiplayer.Exchange.dataExchangeServer.write("Ready")
                    }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            }
        }
    }

    private fun startGame() {
        setContentView(R.layout.cow_catcher)
        //Métrique de l'écran pour placer les objets graphiques
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        //Récupérer le parent
        parentView = findViewById<FrameLayout>(R.id.cowParent)
        // Ajout de la musique
        mediaPlayer = MediaPlayer.create(this, R.raw.cow_catcher)
        mediaPlayer.start()
        //Récupérer et placer le lasso au bon endroit
        lasso = findViewById(R.id.lassoView)
        lasso.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                lasso.x = screenWidth.toFloat()/2 - lasso.width.toFloat()/2
                lasso.y = (4*screenHeight.toFloat()/5) - lasso.height.toFloat()/2
                // Supprimer le listener pour éviter les appels multiples
                lasso.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        cow = findViewById(R.id.cowView)
        changeCowPosition()

        // Créer une instance de GestureDetector avec notre OnGestureListener
        gest = GestureDetector(this, LassoGestureListener())
        lasso.setOnTouchListener { _, event ->
            // Transmettre les événements tactiles au GestureDetector
            gest.onTouchEvent(event)
        }

        //Timer
        // Définition du temps en millisecondes (30 secondes)
        var timer = findViewById<TextView>(R.id.timerView)
        val timeInMillis: Long = 20 * 1000

        // Initialisation du CountDownTimer
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Mise à jour de l'affichage du timer chaque seconde
                val seconds = millisUntilFinished / 1000
                timer.text = "Temps restant : $seconds secondes"
            }

            override fun onFinish() {
                // Action à effectuer lorsque le timer arrive à 0
                cow.visibility = View.INVISIBLE
                mediaPlayer.stop()
                if (!isMulti) {
                    val intent = Intent(context, ScorePage::class.java)
                    intent.putExtra("score", score)
                    intent.putExtra("game", "CowCatcher")
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
                timer.text = "Temps écoulé!"
            }
        }

        // Démarrage du CountDownTimer
        countDownTimer.start()
    }
    private fun initMulti() {
        if (isServer) {
            // Initialisation du nouvel handler pour le thread d'échange de données
            val handlerServer = object :
                Handler(Looper.getMainLooper()) { // quand on reçoit un message on lance l'activité
                override fun handleMessage(msg: Message) {
                    val message = msg.obj.toString()
                    Log.d("DATAEXCHANGE", "[CowCatcher Server] Message received: " + msg.obj.toString())
                    if (msg.obj.toString().contains("Ready")) {
                        Log.d("DATAEXCHANGE", "[CowCatcher] On peut lancer le jeu")
                        alertDialog.dismiss()
                        startGame()
                    } else {
                        // Quand on reçoit le score de l'adversaire on peut afficher la page de score
                        scoreAdversaire = msg.obj.toString().toInt()
                        if (!scoreSent) {
                            Multiplayer.Exchange.dataExchangeServer.write(score.toString()) // On envoie 10 car c'est le score quand on gagne
                            scoreSent = true
                        }
                        val intent = Intent(this@CowCatcher, ScorePage::class.java)
                        intent.putExtra("score", score)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[CowCatcher Server] On lance la page de score")
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
                    Log.d("DATAEXCHANGE", "[CowCatcher Client] Message received: " + msg.obj.toString())
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
                        val intent = Intent(this@CowCatcher, ScorePage::class.java)
                        intent.putExtra("score", score)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[CowCatcher Client] On lance la page de score")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DATAEXCHANGE", "[CowCatcher] onActivityResult, resultCode : $resultCode")
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

    // Classe interne pour gérer les gestes de l'utilisateur
    private inner class LassoGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            Log.d(DEBUG_TAG, "onDown")
            return true
        }
        override fun onFling(
            event1: MotionEvent?,
            event2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.d(DEBUG_TAG, "onFling: $velocityX $velocityY")

            // Calculer la distance de déplacement en fonction de la vitesse du geste
            val distanceX = velocityX / 10
            val distanceY = velocityY / 10

            // Créer une animation TranslateAnimation
            val animation = TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, distanceX,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, distanceY
            )

            val targetX = lasso.x + distanceX
            val targetY = lasso.y + distanceY

            // Définir la durée de l'animation
            animation.duration = 1000 // 1 seconde

            // Définir l'interpolateur pour une accélération/décélération
            animation.interpolator = AccelerateDecelerateInterpolator()

            // Ajouter un écouteur à l'animation
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // Action à effectuer au début de l'animation
                }

                override fun onAnimationEnd(animation: Animation?) {
                    val delta = 150
                    Log.d("vache", "fin de l'animation")
                    if(cow.x - delta <= targetX && targetX <= cow.x+delta
                        && cow.y - delta <= targetY && targetY <= cow.y+delta){
                        score++
                        changeCowPosition()
                        Log.d("CowCatcher", "on change de pos la vache")
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    // Action à effectuer lors de la répétition de l'animation
                }
            })

            // Démarrer l'animation sur la vue
            lasso.startAnimation(animation)
            return true
        }
    }

    // Fonction pour convertir des pixels en dp
    fun Context.dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    private fun changeCowPosition() {
        val dpToPxH = this.dpToPx(150f).toInt()
        val dpToPxW = this.dpToPx(150f).toInt()
        val dpToPxSeuil = this.dpToPx(400f).toInt()
        cow.x = Random.nextInt(screenWidth - dpToPxW).toFloat()
        cow.y = Random.nextInt(screenHeight - dpToPxH - dpToPxSeuil).toFloat() //plus haut que le lasso
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrêt du CountDownTimer pour éviter les fuites de mémoire
        countDownTimer.cancel()
        mediaPlayer.stop()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.start()
    }
}