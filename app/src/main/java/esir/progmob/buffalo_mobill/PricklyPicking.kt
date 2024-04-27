package esir.progmob.buffalo_mobill

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import kotlin.random.Random

class PricklyPicking : ComponentActivity() {

    private val context = this
    private lateinit var gest: GestureDetector

    private val MAX_PRICKLES = 5
    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var parentView : RelativeLayout

    //valeurs pour extraire l'épine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prickly_picking)
        // Initialisation de la taille de l'écran
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        parentView = findViewById<RelativeLayout>(R.id.cowboyParentView)

        //Listener gesture
        gest = GestureDetector(this, PrickleGestureListener())

        //On instancie nos épines
        for (i in 0 until MAX_PRICKLES) {
            createPrickle()
        }
    }

    private inner class PrickleGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            Log.d("", "onDown")
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            Log.d("", "onScroll $e1 $e2 $distanceX $distanceY")
            return true
        }
    }

    private fun createPrickle() {
        //On crée notre image view epine
        val prickle = ImageView(context)

        //On set son image
        prickle.setImageResource(R.drawable.epine)

        //On set sa position
        prickle.x = Random.nextInt(screenWidth/3, 2*screenWidth/3).toFloat()
        prickle.y = Random.nextInt(3*screenHeight/6, 4*screenHeight/6).toFloat() //plus haut que le lasso
        parentView.addView(prickle)

        //Sa rotation
        prickle.rotation = Random.nextInt(0, 180).toFloat()

        //Son on click listener

        prickle.setOnTouchListener { _, event ->
            // Transmettre les événements tactiles au GestureDetector
            gest.onTouchEvent(event)
        }
    }
}