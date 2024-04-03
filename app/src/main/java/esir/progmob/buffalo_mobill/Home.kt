package esir.progmob.buffalo_mobill

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity


class Home : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        val buttonSolo = findViewById<Button>(R.id.solo)
        val buttonMulti = findViewById<Button>(R.id.multi)
        buttonSolo.setOnClickListener{
            val intent = Intent(this, GameList::class.java)
            startActivity(intent)
        }
    }
}