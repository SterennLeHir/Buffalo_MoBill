package esir.progmob.buffalo_mobill

import android.os.Bundle
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

        lait = findViewById(R.id.laitView)
        val pieButton: Button = findViewById(R.id.pieButton)

        pieButton.setOnClickListener {
            if (compteurLait < MAX_LAIT) {
                compteurLait++
                updateLaitView()
            }
            else if(!fini){
                Toast.makeText(this, "C'est fini !", Toast.LENGTH_SHORT).show()
                fini = true
            }
        }
    }

    private fun updateLaitView() {
        val totalHeight = (findViewById<ImageView>(R.id.seauView)).height
        val newHeight = totalHeight * compteurLait / MAX_LAIT
        lait.layoutParams.height = newHeight
        lait.requestLayout()
    }
}