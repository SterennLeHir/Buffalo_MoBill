package esir.progmob.buffalo_mobill

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import kotlin.random.Random

private const val DEBUG_TAG = "Gestures"

class CowCatcher : ComponentActivity(){
    private lateinit var lasso: ImageView
    private lateinit var gest: GestureDetector
    private lateinit var cows: ArrayList<ImageView>
    private var screenWidth = 0
    private var screenHeight = 0 //PAS BEAAAAAAAAAAAAAAAAU
    override fun onCreate(savedInstanceState: Bundle?) {
        //Constructeur et récupération du layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cow_catcher)

        //Métrique de l'écran pour placer les objets graphiques
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

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

        // Créer une instance de GestureDetector avec notre OnGestureListener
        gest = GestureDetector(this, LassoGestureListener())
        lasso.setOnTouchListener { _, event ->
            // Transmettre les événements tactiles au GestureDetector
            gest.onTouchEvent(event)
        }
        generateCows()
    }
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

            // Définir la durée de l'animation
            animation.duration = 1000 // 1 seconde

            // Définir l'interpolateur pour une accélération/décélération
            animation.interpolator = AccelerateDecelerateInterpolator()

            // Démarrer l'animation sur la vue
            lasso.startAnimation(animation)
            return true
        }
    }

    private fun createCowView(context: Context): ImageView {
        val cowView = ImageView(context)
        cowView.setImageResource(R.drawable.bandit) // Remplacez "votre_image" par le nom de votre image
        // Générez des positions aléatoires pour les images
        cowView.x = Random.nextInt(screenWidth - cowView.width).toFloat()
        cowView.y = Random.nextInt(screenHeight - cowView.height).toFloat()

        return cowView
    }
    private fun generateCows(){
        val parentView = findViewById<LinearLayout>(R.id.cowParent)
        val nCows = 3 // Nombre de vaches
        cows = ArrayList<ImageView>()

        for (i in 0 until nCows) {
            val cowView = createCowView(this) //on instancie une nouvelle vache
            cows.add(cowView) //on ajoute la vache à notre liste de vaches
            parentView.addView(cowView) //on ajoute la vache à la vue parente
            Log.d("vache", "vache " + i + " x : " + cowView.x +" y : " + cowView.y)
        }
    }
}