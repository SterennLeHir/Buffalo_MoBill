package esir.progmob.buffalo_mobill

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.activity.ComponentActivity
private const val DEBUG_TAG = "Gestures"

class CowCatcher : ComponentActivity(){
    private lateinit var lasso: ImageView
    private lateinit var gest: GestureDetector
    override fun onCreate(savedInstanceState: Bundle?) {
        //Constructeur et récupération du layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cow_catcher)

        //Métrique de l'écran pour placer les objets graphiques
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

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

    fun generateCows(){
        
    }
}