package esir.progmob.buffalo_mobill

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import java.lang.Math.PI
import kotlin.math.atan2
import kotlin.random.Random

class PricklyPicking : ComponentActivity() {

    private val context = this
    private lateinit var gest: GestureDetector
    private lateinit var currentPrickle: View

    private val MAX_PRICKLES = 10
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
        private val threshold = 200
        private var initialX = 0f
        private var initialY = 0f
        override fun onDown(e: MotionEvent): Boolean {
            Log.d("", "onDown")
            initialX = currentPrickle.x
            initialY = currentPrickle.y
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            Log.d("", "onScroll $distanceX $distanceY")
            return true
        }
    }

    private fun createPrickle() {
        //On crée notre image view epine
        val prickle = ImageView(context)

        //On set son image
        prickle.setImageResource(R.drawable.epine)

        //On set sa taille
        prickle.layoutParams = ViewGroup.LayoutParams(200,200)

        //On set sa position
        prickle.x = Random.nextInt(screenWidth/4, 2*screenWidth/4).toFloat()
        prickle.y = Random.nextInt(3*screenHeight/6, 4*screenHeight/6).toFloat() //plus haut que le lasso
        parentView.addView(prickle)

        /*
        //Sa rotation
        val Pmid = Point(screenWidth/2, screenHeight/2)
        val P = Point(prickle.x.toInt(), prickle.y.toInt())
        prickle.rotation = angleBetweenPoints(Pmid, P).toFloat() //empirique
        Log.d("", "" + angleBetweenPoints(Pmid, P).toFloat())
         */
        //Son on click listener
        val onTouchListener = object : View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                currentPrickle = view
                return gest.onTouchEvent(event)
            }
        }
        prickle.setOnTouchListener(onTouchListener)
    }

    fun angleBetweenPoints(point1: Point, point2: Point): Double {
        val deltaY = point2.y - point1.y
        val deltaX = point2.x - point1.x
        var angle = atan2(deltaY.toDouble(), deltaX.toDouble())
        if (angle < 0) {
            angle += 2 * PI // Convertir l'angle en radians positifs
        }
        val angleDegrees = angle * (180 / PI) // Conversion en degrés
        return angleDegrees
    }
}