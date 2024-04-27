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
                        Log.d("", "Jeu terminé")
                        Toast.makeText(context, "Jeu terminé!", Toast.LENGTH_SHORT)//s'affiche pas
                    }
                }
            }
        }
        return true
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
}