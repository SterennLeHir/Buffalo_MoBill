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
    var myScore : Int = 0
    var theirScore : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_page)
        // On récupère les informations fournies par l'activité précédente
        val isMulti = intent.getBooleanExtra("isMulti", false)
        val isServer = intent.getBooleanExtra("isServer", false)
        Log.d("DATAEXCHANGE", "isServer : $isServer")
        myScore = intent.getIntExtra("score", 0)
        theirScore = intent.getIntExtra("scoreAdversaire", 0)

        // On met à jour l'affichage de votre score
        updateScore()
        // On modifie le handler du serveur
        if (isMulti && isServer) initMulti()
        // On ajoute un listener au bouton
        val button = findViewById<TextView>(R.id.next)
        button.setOnClickListener {
            Log.d("DATAEXCHANGE", "isMulti : $isMulti, isServer : $isServer")
            if (!isMulti) {
                val resultIntent = Intent()
                resultIntent.putExtra("score", myScore)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else if (!isServer) {
                Log.d("DATAEXCHANGE", "Le client envoie Next")
                Multiplayer.Exchange.dataExchangeClient.write("Next")
                val resultIntent = Intent()
                resultIntent.putExtra("score", myScore)
                resultIntent.putExtra("scoreAdversaire", theirScore)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "En attente de l'autre joueur", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initMulti() {
        val handler = object : Handler(Looper.getMainLooper()) { // côté serveur
            override fun handleMessage(msg: Message) {
                // Le message reçu indique qu'on doit retourner au menu des jeux
                val resultIntent = Intent()
                Log.d("DATAEXCHANGE", "[Serveur] Message received: " + msg.obj.toString())
                Log.d("DATAEXCHANGE", "score : $myScore, scoreAdversaire : $theirScore")
                resultIntent.putExtra("score", myScore)
                resultIntent.putExtra("scoreAdversaire", theirScore)
                setResult(RESULT_OK, resultIntent)
                Log.d("DATAEXCHANGE", "[Serveur] On retourne à la page des jeux")
                finish()
            }
        }
        Multiplayer.Exchange.dataExchangeServer.setHandler(handler)
    }

    private fun updateScore() {
        val myScoreView = findViewById<TextView>(R.id.currentScore)
        myScoreView.text = myScore.toString()
        val theirScoreView = findViewById<TextView>(R.id.otherScore)
        theirScoreView.text = theirScore.toString()
        val resultView = findViewById<TextView>(R.id.result)
        if (myScore > theirScore) {
            resultView.text = "Vous avez gagné !"
        } else if (myScore < theirScore) {
            resultView.text = "Vous avez perdu !"
        } else {
            resultView.text = "Match nul !"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DATAEXCHANGE", "ScorePage destroyed")
    }
}