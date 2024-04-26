package esir.progmob.buffalo_mobill

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MilkMaster : ComponentActivity() {
    private lateinit var lait: View
    private var compteurLait = 0
    private val MAX_LAIT = 30
    private var fini = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.milk_master)
        // Son de lait qui coule
        val mediaPlayer : MediaPlayer = MediaPlayer.create(this, R.raw.lait);
        val piesView: ImageView = findViewById(R.id.pies)
        val seauView: ImageView = findViewById(R.id.seau)
        var waiting = false
        piesView.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                Toast.makeText(this, "Vous allez trop vite et brusquez la vache", Toast.LENGTH_SHORT).show()
                waiting = true
                piesView.isClickable = false
                Handler(Looper.getMainLooper()).postDelayed({
                    waiting = false
                    piesView.isClickable = true
                }, 2000) // Délai de 2 secondes
            } else if (compteurLait < MAX_LAIT && !waiting) { // il faut attendre la fin du son pour pouvoir cliquer à nouveau mais ne marche pas
                compteurLait++
                mediaPlayer.start()
            } else if (compteurLait >= MAX_LAIT && !fini){
                Toast.makeText(this, "C'est fini !", Toast.LENGTH_SHORT).show()
                fini = true
                seauView.setImageDrawable(resources.getDrawable(R.drawable.seau_rempli))
            }
        }
    }

    private fun updateLaitView() {
        val totalHeight = (findViewById<ImageView>(R.id.seau)).height
        val newHeight = totalHeight * compteurLait / MAX_LAIT
        lait.layoutParams.height = newHeight
        lait.requestLayout()
    }
}