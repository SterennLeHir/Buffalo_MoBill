package esir.progmob.buffalo_mobill

import android.animation.ObjectAnimator
import android.graphics.Point
import android.os.Bundle
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
    private var score = 0

    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var parentView : RelativeLayout


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
                    val distance = sqrt((abs(lastX - x) + abs(lastY - y)).pow(2))
                    val angle = calculateAngle(lastX,lastY,x,y)
                    val angleMinToHave = if(view.rotation  + 170 >= 360) view.rotation  + 170 - 360 else view.rotation  + 170
                    val angleMaxToHave = if(view.rotation + 190 >= 360) view.rotation  + 170 - 360 else view.rotation + 190
                    val isGoodAngle = angle in angleMinToHave..angleMaxToHave
                    if(distance >= seuilDist && isGoodAngle){
                        isDragging = true
                    }
                    /*
                    Log.d("points", "$lastX $lastY $x $y ")
                    */
                    Log.d("angle", ""+ calculateAngle(lastX,lastY,x,y))


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
                        Log.d("", "Jeu terminé")
                        Toast.makeText(context, "Jeu terminé!", Toast.LENGTH_SHORT)//s'affiche pas
                    }
                }
            }
        }
        return true
    }

    /*
    private inner class PrickleGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val threshold = 200
        private var initialX = 0f
        private var initialY = 0f
        private var distance = 0
        override fun onDown(e: MotionEvent): Boolean {
            Log.d("", "onDown")
            initialX = currentPrickle.x
            initialY = currentPrickle.y
            distance = 0
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            Log.d("", "onScroll $distanceX $distanceY")
            if(distance >= threshold){

            }
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            return true
        }
    }

     */



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

        prickle.setOnTouchListener{ view, event ->
            handleTouchEvent(view, event)
        }

        /*
        //Sa rotation
        val Pmid = Point(screenWidth/2, screenHeight/2)
        val P = Point(prickle.x.toInt(), prickle.y.toInt())
        prickle.rotation = angleBetweenPoints(Pmid, P).toFloat() //empirique
        Log.d("", "" + angleBetweenPoints(Pmid, P).toFloat())
         */
        //Son on click listener



        /*
        val onTouchListener = object : View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                currentPrickle = view
                if (event.type==ACTION_UP)
                return gest.onTouchEvent(event)
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        // Reset the velocity tracker back to its initial state.
                        mVelocityTracker?.clear()
                        // If necessary, retrieve a new VelocityTracker object to watch
                        // the velocity of a motion.
                        mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
                        // Add a user's movement to the tracker.
                        mVelocityTracker?.addMovement(event)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mVelocityTracker?.apply {
                            val pointerId: Int = event.getPointerId(event.actionIndex)
                            addMovement(event)
                            // When you want to determine the velocity, call
                            // computeCurrentVelocity(). Then, call getXVelocity() and
                            // getYVelocity() to retrieve the velocity for each pointer
                            // ID.
                            computeCurrentVelocity(1000)
                            // Log velocity of pixels per second. It's best practice to
                            // use VelocityTrackerCompat where possible.
                            Log.d("Velo", "X velocity: ${getXVelocity(pointerId)}")
                            Log.d("Velo", "Y velocity: ${getYVelocity(pointerId)}")
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Return a VelocityTracker object back to be re-used by others.
                        mVelocityTracker?.recycle()
                        mVelocityTracker = null
                    }
                }
                return true
            }
        }
        prickle.setOnTouchListener(onTouchListener)*/
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
}