package esir.progmob.buffalo_mobill

import android.content.Intent
import android.content.SharedPreferences
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

    // données récupérées
    private var myScore : Int = 0
    private var theirScore : Int = 0
    private var isMulti : Boolean = false
    private var isServer : Boolean = false
    private var isFinished : Boolean = false
    private var time : Float = 0f
    private var game : String? = null

    // fichier pour conserver les scores d'entraînement
    private val FILENAME = "Scores"
    private lateinit var preferences : SharedPreferences
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences(FILENAME, MODE_PRIVATE)

        // On récupère les informations fournies par l'activité précédente
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        myScore = intent.getIntExtra("score", 0)
        theirScore = intent.getIntExtra("scoreAdversaire", 0)
        isFinished = intent.getBooleanExtra("isFinished", false)
        time = intent.getFloatExtra("time", 0f)
        game = intent.getStringExtra("game")

        // On met à jour l'affichage des scores
        if (isMulti) {
            if (isServer) initMulti() // On modifie le handler du serveur
            if (!isFinished) {
                updateScoreMulti()
            } else {
                partyFinishedMulti()
            }
        } else {
            if (!isFinished) {
                updateScoreSolo()
            } else {
                partyFinishedSolo()
            }
        }
    }

    private fun partyFinishedSolo() {
        // Affichage des éléments graphiques
        setContentView(R.layout.score_page_solo_final)
        mediaPlayer = MediaPlayer.create(this, R.raw.yeehaw)
        mediaPlayer.start()
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

    private fun partyFinishedMulti() {
        setContentView(R.layout.score_page_multi_final)
        val myScoreView = findViewById<TextView>(R.id.currentScore)
        myScoreView.text = myScore.toString()
        val theirScoreView = findViewById<TextView>(R.id.otherScore)
        theirScoreView.text = theirScore.toString()
        val resultView = findViewById<TextView>(R.id.result)
        compareResult(resultView) // On compare les scores
        // On ajoute un listener au bouton
        val button = findViewById<TextView>(R.id.next)
        button.setOnClickListener {
            if (!isServer) {
                Log.d("DATAEXCHANGE", "[ScorePage] Le client envoie Next")
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
                Log.d("DATAEXCHANGE", "[ScorePage serveur] Message received: " + msg.obj.toString())
                resultIntent.putExtra("score", myScore)
                resultIntent.putExtra("scoreAdversaire", theirScore)
                setResult(RESULT_OK, resultIntent)
                Log.d("DATAEXCHANGE", "[ScorePage serveur] On retourne à la page des jeux")
                finish()
            }
        }
        Multiplayer.Exchange.dataExchangeServer.cancel()
        Multiplayer.Exchange.dataExchangeServer = DataExchange(handler)
        Multiplayer.Exchange.dataExchangeServer.start()
    }

    private fun updateScoreMulti() {
        setContentView(R.layout.score_page_multi)
        val myScoreView = findViewById<TextView>(R.id.currentScore)
        myScoreView.text = myScore.toString()
        val theirScoreView = findViewById<TextView>(R.id.otherScore)
        theirScoreView.text = theirScore.toString()
        val resultView = findViewById<TextView>(R.id.result)
        compareResult(resultView)
        // On ajoute un listener au bouton
        val button = findViewById<TextView>(R.id.next)
        button.setOnClickListener {
            if (!isServer) {
                Log.d("DATAEXCHANGE", "[ScorePage] Le client envoie Next")
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

    private fun compareResult(resultView: TextView) {
        if (myScore > theirScore) {
            resultView.text = getString(R.string.won)
            mediaPlayer = MediaPlayer.create(this, R.raw.yeehaw)
            mediaPlayer.start()
        } else if (myScore < theirScore) {
            mediaPlayer = MediaPlayer.create(this, R.raw.first_shoot)
            resultView.text = getString(R.string.lost)
            mediaPlayer.start()
            while (mediaPlayer.isPlaying) {
                // On attend que le son se termine
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.second_shoot)
            mediaPlayer.start()
        } else {
            resultView.text = getString(R.string.equality)
        }
    }

    private fun updateScoreSolo() {
        // Affichage des éléments graphiques
        setContentView(R.layout.score_page_solo)
        val myScoreView = findViewById<TextView>(R.id.score)
        if (game !=null) {
            val gameView = findViewById<TextView>(R.id.game)
            gameView.text = game
            // On montre le meilleur score
            val bestScoreView = findViewById<TextView>(R.id.bestScore)
            if (time != 0f) { // Si le score dépend d'un temps
                val bestScore = preferences.getFloat(game, 1000F)
                val bestScoreTextView = findViewById<TextView>(R.id.bestScoreText)
                bestScoreTextView.text = getString(R.string.best_time)
                val scoreTextView = findViewById<TextView>(R.id.scoreText)
                scoreTextView.text = getString(R.string.time)
                if (time < bestScore) {
                    val editor = preferences.edit()
                    editor.putFloat(game, time)
                    editor.apply()
                    bestScoreView.text = time.toString()
                    myScoreView.text = time.toString()
                } else {
                    bestScoreView.text = bestScore.toString()
                    myScoreView.text = time.toString()
                }

            } else {
                val bestScore = preferences.getInt(game, 0)
                if (myScore > bestScore) { // si on a dépassé le meilleur score
                    val editor = preferences.edit()
                    editor.putInt(game, myScore)
                    editor.apply()
                    bestScoreView.text = myScore.toString()
                } else {
                    bestScoreView.text = bestScore.toString()
                }
                myScoreView.text = myScore.toString()
            }
        }

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