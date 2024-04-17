package esir.progmob.buffalo_mobill

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity


class Home : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //On applique le layout de home
        setContentView(R.layout.home)

        //On définit les boutons
        val buttonSolo = findViewById<Button>(R.id.solo)
        val buttonMulti = findViewById<Button>(R.id.multi)

        //On définit leurs listeners
        buttonSolo.setOnClickListener{
            val intent = Intent(this, GameList::class.java)
            startActivity(intent)
        }
        buttonMulti.setOnClickListener{
            val intent = Intent(this, Multiplayer::class.java)
            startActivity(intent)
        }
    }
}