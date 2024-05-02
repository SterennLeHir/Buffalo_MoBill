package esir.progmob.buffalo_mobill

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

abstract class Game : ComponentActivity() {

    // scores pour l'affichage une fois le jeu fini
    protected var score = 0
    protected var scoreAdversaire : Int = 0
    protected var scoreSent : Boolean = false

    // pour le multijoueur
    protected var isServer : Boolean = false
    protected var isMulti : Boolean = false
    protected var isReady : Boolean = false
    protected var isAdversaireReady : Boolean = false
    protected lateinit var alertDialog : AlertDialog // boîte de dialogue pour les règles du jeux
    protected lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isServer = intent.getBooleanExtra("isServer", false)
        isMulti = intent.getBooleanExtra("isMulti", false)
        isReady = intent.getBooleanExtra("isReady", false)
        isAdversaireReady = intent.getBooleanExtra("isAdversaireReady", false)
    }

    abstract fun startGame()

    abstract fun initMulti()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // On récupère les scores
            score = data?.getIntExtra("score", 0) ?: 0
            scoreAdversaire = data?.getIntExtra("scoreAdversaire", 0) ?: 0
            Log.d("DATAEXCHANGE", "score : $score, scoreAdversaire : $scoreAdversaire")
            // On transmet les scores à GameList
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            resultIntent.putExtra("scoreAdversaire", scoreAdversaire)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // do nothing
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

}