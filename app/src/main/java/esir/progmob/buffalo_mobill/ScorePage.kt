package esir.progmob.buffalo_mobill

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class ScorePage : ComponentActivity() {
    var handler : Handler? = null
    var myScore : Int = 0
    var theirScore : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_page)
        // On récupère les informations fournies par l'activité précédente
        val isMulti = intent.getBooleanExtra("multi", false)
        val isServer = intent.getBooleanExtra("isServer", false)
        myScore = intent.getIntExtra("score", 0)
        theirScore = intent.getIntExtra("scoreAdversaire", 0)
        // On met à jour l'affichage de votre score
        val myScoreView = findViewById<TextView>(R.id.currentScore)
        myScoreView.text = myScore.toString()
        val theirScoreView = findViewById<TextView>(R.id.otherScore)
        theirScoreView.text = theirScore.toString()

        // On modifie le handler du serveur
        if (isMulti && isServer) {
            //TODO : Ajouter le code pour le serveur
        }
        //Multiplayer.Exchange.dataExchangeServer.setHandler(handler!!)
        // On ajoute un listener au bouton
        val button = findViewById<TextView>(R.id.next)
        button.setOnClickListener {
            if (isMulti && !isServer) {
                //Multiplayer.Exchange.dataExchangeClient.write("ShadyShowdown")
            } else if (isMulti) {
                //Toast.makeText(this, "En attente de l'autre joueur", Toast.LENGTH_SHORT).show()
            }
        }
    }
}