package esir.progmob.buffalo_mobill

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class ScorePage : ComponentActivity() {
    private var myScore : Int = 0
    private var theirScore : Int = 0
    private var isMulti : Boolean = false
    private var isServer : Boolean = false
    private lateinit var mediaPlayerFirst : MediaPlayer
    private lateinit var mediaPlayerSecond : MediaPlayer
    private lateinit var mediaPlayerWin : MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // On prépare la musique
        mediaPlayerFirst = MediaPlayer.create(this, R.raw.first_shoot)
        mediaPlayerSecond = MediaPlayer.create(this, R.raw.second_shoot)
        mediaPlayerWin = MediaPlayer.create(this, R.raw.yeehaw)
        // On récupère les informations fournies par l'activité précédente
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        myScore = intent.getIntExtra("score", 0)
        theirScore = intent.getIntExtra("scoreAdversaire", 0)

        // On met à jour l'affichage de votre score
        if (isMulti) {
            if (isServer) initMulti() // On modifie le handler du serveur
            updateScoreMulti()
        } else {
            updateScoreSolo()
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

    private fun updateScoreMulti() {
        setContentView(R.layout.score_page_multi)
        val myScoreView = findViewById<TextView>(R.id.currentScore)
        myScoreView.text = myScore.toString()
        val theirScoreView = findViewById<TextView>(R.id.otherScore)
        theirScoreView.text = theirScore.toString()
        val resultView = findViewById<TextView>(R.id.result)
        if (myScore > theirScore) {
            resultView.text = "Vous avez gagné !"
            mediaPlayerWin.start()
        } else if (myScore < theirScore) {
            resultView.text = "Vous avez perdu !"
            mediaPlayerFirst.start()
            while (mediaPlayerFirst.isPlaying) {
            // On attend que le son se termine
            }
            mediaPlayerSecond.start()
        } else {
            resultView.text = "Match nul !"
        }
        // On ajoute un listener au bouton
        val button = findViewById<TextView>(R.id.next)
        button.setOnClickListener {
            Log.d("DATAEXCHANGE", "isMulti : $isMulti, isServer : $isServer")
            if (!isServer) {
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

    private fun updateScoreSolo() {
        setContentView(R.layout.score_page_solo)
        val myScoreView = findViewById<TextView>(R.id.currentScore)
        myScoreView.text = myScore.toString()
        // On ajoute un listener au bouton
        val button = findViewById<TextView>(R.id.next)
        button.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", myScore)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DATAEXCHANGE", "ScorePage destroyed")
    }
}