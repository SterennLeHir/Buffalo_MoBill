package esir.progmob.buffalo_mobill

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

class GameList : ComponentActivity() {

    private var isMulti : Boolean = false
    private var isServer : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMulti = intent.getBooleanExtra("multi", false)
        isServer = intent.getBooleanExtra("isServer", false)
        Log.d("DATAEXCHANGE", "isServer : $isServer")
        if (isMulti && isServer) { // Si on est en multijoueur
                val handler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        // Traitez le message ici
                        val message = msg.obj.toString()
                        Log.d("DATAEXCHANGE",
                            "[server : gamelist] Message received: " + msg.what.toString() + " " + message)
                        // On lance le jeu correspondant au message reçu choisi par le client
                        when (message) {
                            "CowCatcher" -> {
                                val intent = Intent(this@GameList, CowCatcher::class.java)
                                intent.putExtra("multi", true)
                                intent.putExtra("isServer", true)
                                this@GameList.startActivity(intent)
                            }
                            "QuickQuiz" -> {
                                val intent = Intent(this@GameList, QuickQuiz::class.java)
                                intent.putExtra("multi", true)
                                intent.putExtra("isServer", true)
                                this@GameList.startActivity(intent)
                            }
                            "ShadyShowdown" -> {
                                val intent = Intent(this@GameList, ShadyShowdown::class.java)
                                intent.putExtra("multi", true)
                                intent.putExtra("isServer", true)
                                this@GameList.startActivity(intent)
                                Log.d("DATAEXCHANGE", "ShadyShowdown")
                            }
                            "MilkMaster" -> {
                                val intent = Intent(this@GameList, MilkMaster::class.java)
                                intent.putExtra("multi", true)
                                intent.putExtra("isServer", true)
                                this@GameList.startActivity(intent)
                            }
                            "WildRide" -> {
                                val intent = Intent(this@GameList, WildRide::class.java)
                                intent.putExtra("multi", true)
                                intent.putExtra("isServer", true)
                                this@GameList.startActivity(intent)
                            }
                            "PricklyPicking" -> {
                                val intent = Intent(this@GameList, PricklyPicking::class.java)
                                intent.putExtra("multi", true)
                                intent.putExtra("isServer", true)
                                this@GameList.startActivity(intent)
                            }
                        }
                    }
            }
            Log.d("DATAEXCHANGE", "Serveur set handler")
            Multiplayer.Exchange.dataExchangeServer.setHandler(handler) // On met à jour l'handler du serveur
            Log.d("DATAEXCHANGE", "DataExchange Serveur thread launched")
            Multiplayer.Exchange.dataExchangeServer.start() // On lance le thread d'échange de données
        }
        if (isMulti && !isServer) {
            Log.d("DATAEXCHANGE", "DataExchange Client thread launched")
            Multiplayer.Exchange.dataExchangeClient.start()
        }
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
            if (isMulti && !isServer) {
                Multiplayer.Exchange.dataExchangeClient.write("MilkMaster")
            } else if (isMulti) {
                Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
            }
            game1.putExtra("isServer", isServer)
            game1.putExtra("isMulti", isMulti)
            if (!isServer || !isMulti) {
                startActivity(game1)
            }
        }

        buttonWildRide.setOnClickListener{
            val game2 = Intent(this, WildRide::class.java)
            startActivity(game2)
        }

        buttonShadyShowdown.setOnClickListener{
            val game3 = Intent(this, ShadyShowdown::class.java)
            if (isMulti && !isServer) {
                Log.d("DATAEXCHANGE", "Client envoie le jeu")
                Multiplayer.Exchange.dataExchangeClient.write("ShadyShowdown")
            } else if (isMulti) {
                Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
            }
            game3.putExtra("isServer", isServer)
            game3.putExtra("isMulti", isMulti)
            if (!isServer || !isMulti) {
                startActivity(game3)
            }
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