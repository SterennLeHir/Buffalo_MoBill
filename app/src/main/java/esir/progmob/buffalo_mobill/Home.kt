package esir.progmob.buffalo_mobill


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity


class Home : ComponentActivity() {

    object Music {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //On applique le layout de home
        setContentView(R.layout.home)

        // On ajoute la musique
        Music.mediaPlayer = MediaPlayer.create(this, R.raw.music)
        Music.mediaPlayer?.start()
        //On définit les boutons
        val buttonSolo = findViewById<Button>(R.id.solo)
        val buttonMulti = findViewById<Button>(R.id.multi)

        //On définit leurs listeners
        buttonSolo.setOnClickListener{
            val intent = Intent(this, GameList::class.java)
            Music.mediaPlayer?.stop()
            startActivity(intent)
        }
        buttonMulti.setOnClickListener{
            val intent = Intent(this, Multiplayer::class.java)
            Music.mediaPlayer?.stop()
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Music.mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        Music.mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        Music.mediaPlayer?.stop()
    }

}