package esir.progmob.buffalo_mobill

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.activity.ComponentActivity
import kotlin.random.Random

class PricklyPicking : ComponentActivity() {

    // gestion des images
    private lateinit var prickle1 : ImageView
    private lateinit var prickle2 : ImageView
    private lateinit var prickle3 : ImageView
    private lateinit var prickle4 : ImageView
    private lateinit var prickle5 : ImageView
    private var prickles_list : MutableList<ImageView> = mutableListOf()
    private var screenWidth = 0
    private var screenHeight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prickly_picking)
        // Récupération des images
        prickle1 = findViewById(R.id.prickle1)
        prickle2 = findViewById(R.id.prickle2)
        prickle3 = findViewById(R.id.prickle3)
        prickle4 = findViewById(R.id.prickle4)
        prickle5 = findViewById(R.id.prickle5)
        // Ajout des images dans la liste des épines
        prickles_list.add(prickle1)
        prickles_list.add(prickle2)
        prickles_list.add(prickle3)
        prickles_list.add(prickle4)
        prickles_list.add(prickle5)
        // Initialisation de la taille de l'écran
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        initImages(); // on initialise la position des épines
    }

    private fun initImages() {
        for (prickle : ImageView in prickles_list) {
            val x = Random.nextInt(screenWidth - 50)
            val y = Random.nextInt(screenHeight - 50)
            prickle.x = x.toFloat()
            prickle.y = y.toFloat()
        }
    }
}