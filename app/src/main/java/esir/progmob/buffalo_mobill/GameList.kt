package esir.progmob.buffalo_mobill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity

class GameList : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gamelist)

        // Liste des boutons de l'activité
        val buttonMilkMaster = findViewById<Button>(R.id.milk_master)
        val buttonWildRide = findViewById<Button>(R.id.wild_ride)
        val buttonShadyShowdown = findViewById<Button>(R.id.shady_showdown)
        val buttonQuickQuiz = findViewById<Button>(R.id.quick_quiz)
        val buttonCowCatcher = findViewById<Button>(R.id.cow_catcher)
        val buttonPricklyPicking = findViewById<Button>(R.id.prickly_picking)

        // Change d'activité pour chaque jeu
        buttonMilkMaster.setOnClickListener{
            val game1 = Intent(this, MilkMaster::class.java)
            startActivity(game1)
        }
        buttonWildRide.setOnClickListener{
            val game2 = Intent(this, WildRide::class.java)
            startActivity(game2)
        }
        buttonShadyShowdown.setOnClickListener{
            val game3 = Intent(this, ShadyShowdown::class.java)
            startActivity(game3)
        }
        buttonQuickQuiz.setOnClickListener{
            val game4 = Intent(this, QuickQuiz::class.java)
            startActivity(game4)
        }
        buttonCowCatcher.setOnClickListener{
            val game5 = Intent(this, CowCatcher::class.java)
            startActivity(game5)
        }
        buttonPricklyPicking.setOnClickListener{
            val game6 = Intent(this, PricklyPicking::class.java)
            startActivity(game6)
        }
    }
}