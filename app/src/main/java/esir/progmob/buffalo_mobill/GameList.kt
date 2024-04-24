package esir.progmob.buffalo_mobill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.net.Socket
import kotlin.random.Random

class GameList : ComponentActivity() {

    private var isMulti : Boolean = false
    private var isServer : Boolean = false
    private var score : Int = 0
    private var scoreAdversaire : Int = 0
    private var numberOfParties : Int = 0
    private val NUMBEROFPARTIESMAX = 2

    // Liste des jeux (pour le mode aléatoire)
    private var gameList : MutableList<String> = mutableListOf()
    private var isRandom : Boolean = false
    private val games : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        Log.d("DATAEXCHANGE", "isServer : $isServer")
        if (isMulti) initMulti()
        setContentView(R.layout.gamelist)

        // Initialisation de la liste des jeux
        gameList = mutableListOf("ShadyShowdown", "QuickQuiz")//mutableListOf("MilkMaster", "WildRide", "ShadyShowdown", "QuickQuiz", "CowCatcher", "PricklyPicking")
        Log.d("DATAEXCHANGE", "GameList created")

        // Liste des boutons de l'activité
        val buttonMilkMaster = findViewById<Button>(R.id.milk_master)
        val buttonWildRide = findViewById<Button>(R.id.wild_ride)
        val buttonShadyShowdown = findViewById<Button>(R.id.shady_showdown)
        val buttonQuickQuiz = findViewById<Button>(R.id.quick_quiz)
        val buttonCowCatcher = findViewById<Button>(R.id.cow_catcher)
        val buttonPricklyPicking = findViewById<Button>(R.id.prickly_picking)
        val randomParty = findViewById<Button>(R.id.random)


        // Change d'activité pour chaque jeu
        buttonMilkMaster.setOnClickListener{
            if (!isRandom) {
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
            } else {
                Toast.makeText(this, "Vous ne pouvez pas choisir le jeu", Toast.LENGTH_LONG).show()
            }
        }

        buttonWildRide.setOnClickListener{
            if (!isRandom) {
                val game2 = Intent(this, WildRide::class.java)
                if (isMulti && !isServer) {
                    Multiplayer.Exchange.dataExchangeClient.write("WildRide")
                } else if (isMulti) {
                    Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
                }
                game2.putExtra("isServer", isServer)
                game2.putExtra("isMulti", isMulti)
                if (!isServer || !isMulti) {
                    startActivityForResult(game2,1)
                }
            } else {
                Toast.makeText(this, "Vous ne pouvez pas choisir le jeu", Toast.LENGTH_LONG).show()
            }
        }

        buttonShadyShowdown.setOnClickListener{
            if (!isRandom) {
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
                    startActivityForResult(game3, 1)
                }
            } else {
                Toast.makeText(this, "Vous ne pouvez pas choisir le jeu", Toast.LENGTH_LONG).show()
            }
        }

        buttonQuickQuiz.setOnClickListener{
            if (!isRandom) {
                val game4 = Intent(this, QuickQuiz::class.java)
                if (isMulti && !isServer) {
                    Multiplayer.Exchange.dataExchangeClient.write("QuickQuiz")
                } else if (isMulti) {
                    Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
                }
                game4.putExtra("isServer", isServer)
                game4.putExtra("isMulti", isMulti)
                if (!isServer || !isMulti) {
                    startActivityForResult(game4, 1)
                }
            } else {
                Toast.makeText(this, "Vous ne pouvez pas choisir le jeu", Toast.LENGTH_LONG).show()
            }
        }

        buttonCowCatcher.setOnClickListener{
            if (!isRandom) {
                val game5 = Intent(this, CowCatcher::class.java)
                if (isMulti && !isServer) {
                    Multiplayer.Exchange.dataExchangeClient.write("CowCatcher")
                } else if (isMulti) {
                    Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
                }
                game5.putExtra("isServer", isServer)
                game5.putExtra("isMulti", isMulti)
                if (!isServer || !isMulti) {
                    startActivityForResult(game5,1)
                }
            } else {
                Toast.makeText(this, "Vous ne pouvez pas choisir le jeu", Toast.LENGTH_LONG).show()
            }
        }

        buttonPricklyPicking.setOnClickListener{
            if (!isRandom) {
                val game6 = Intent(this, PricklyPicking::class.java)
                if (isMulti && !isServer) {
                    Multiplayer.Exchange.dataExchangeClient.write("PricklyPicking")
                } else if (isMulti) {
                    Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
                }
                game6.putExtra("isServer", isServer)
                game6.putExtra("isMulti", isMulti)
                if (!isServer || !isMulti) {
                    startActivityForResult(game6,1)
                }
            } else {
                Toast.makeText(this, "Vous ne pouvez pas choisir le jeu", Toast.LENGTH_LONG).show()
            }
        }

        randomParty.setOnClickListener{
            createRandomParty()
        }
    }

    private fun createRandomParty() {
        this.isRandom = true
        if (!isServer) {
            for (i in 0..<NUMBEROFPARTIESMAX) { // on sélectionne 2 jeux aléatoires
                val random = Random.nextInt(gameList.size) // choisit le jeu aléatoirement
                Log.d("DATAEXCHANGE", "Random position : $random")
                games.add(gameList[random])
                gameList.removeAt(random)
            }
            Log.d("DATAEXCHANGE", "Random games : " + games[0] + " " + games[1])
            val randomGame = Intent(this, Class.forName("esir.progmob.buffalo_mobill." + games[numberOfParties]))
            randomGame.putExtra("isServer", isServer)
            randomGame.putExtra("isMulti", isMulti)
            if (isMulti) Multiplayer.Exchange.dataExchangeClient.write(games[numberOfParties]) // On indique au serveur le jeu choisi
            startActivityForResult(randomGame, 1)
        } else {
            Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initMulti() {
        if (isServer) {
            val handler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    // Traitez le message ici
                    val message = msg.obj.toString()
                    Log.d(
                        "DATAEXCHANGE",
                        "[server : gamelist] Message received: " + msg.what.toString() + " " + message
                    )
                    // On lance le jeu correspondant au message reçu choisi par le client
                    when (message) {
                        "CowCatcher" -> {
                            val intent = Intent(this@GameList, CowCatcher::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivity(intent)
                        }

                        "QuickQuiz" -> {
                            val intent = Intent(this@GameList, QuickQuiz::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivityForResult(intent, 1)
                        }

                        "ShadyShowdown" -> {
                            val intent = Intent(this@GameList, ShadyShowdown::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivityForResult(intent, 1)
                            Log.d("DATAEXCHANGE", "ShadyShowdown")
                        }

                        "MilkMaster" -> {
                            val intent = Intent(this@GameList, MilkMaster::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivity(intent)
                        }

                        "WildRide" -> {
                            val intent = Intent(this@GameList, WildRide::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivity(intent)
                        }

                        "PricklyPicking" -> {
                            val intent = Intent(this@GameList, PricklyPicking::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivity(intent)
                        }
                    }
                }
            }
            Log.d("DATAEXCHANGE", "Serveur set handler")
            Multiplayer.Exchange.dataExchangeServer.setHandler(handler) // On met à jour l'handler du serveur
            Log.d("DATAEXCHANGE", "DataExchange Serveur thread launched")
        }
        if (numberOfParties == 0) {
            Log.d("DATAEXCHANGE", "[GameList] Initialisation")
            if (isServer) Multiplayer.Exchange.dataExchangeServer.start() // On lance le thread d'échange de données du serveur
            else Multiplayer.Exchange.dataExchangeClient.start() // On lance le thread d'échange de données du client
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DATAEXCHANGE", "GameList finished")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DATAEXCHANGE", "[GameList] onActivityResult, resultCode : $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            Log.d("DATAEXCHANGE", "score :" + data?.getIntExtra("score", 0).toString())
            Log.d("DATAEXCHANGE", "scoreAdversaire :" + data?.getIntExtra("scoreAdversaire", 0).toString())
            score += data?.getIntExtra("score", 0) ?: 0
            scoreAdversaire += data?.getIntExtra("scoreAdversaire", 0) ?: 0
            Log.d("DATAEXCHANGE", "score : $score, scoreAdversaire : $scoreAdversaire")
            if (isRandom && !isServer) {
                val randomGame = Intent(this, Class.forName("esir.progmob.buffalo_mobill." + games[numberOfParties]))
                randomGame.putExtra("isServer", isServer)
                randomGame.putExtra("isMulti", isMulti)
                if (isMulti) {
                    Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show()
                    Thread.sleep(5000)
                    Multiplayer.Exchange.dataExchangeClient.write(games[numberOfParties])
                } // On indique au serveur le jeu choisi
                startActivityForResult(randomGame, 1)
            }
        }
    }

    override fun onRestart() { // appelée après la fin de la page de score
        super.onRestart()
        numberOfParties++
        Log.d("DATAEXCHANGE", "GameList restarted")
        Log.d("DATAEXCHANGE", "isServer : $isServer, isMulti : $isMulti")
        if (isMulti) initMulti()
        if (numberOfParties == NUMBEROFPARTIESMAX) {
            val intent = Intent(this, ScorePage::class.java)
            intent.putExtra("score", score)
            intent.putExtra("scoreAdversaire", scoreAdversaire)
            startActivityForResult(intent, 1)
            partyFinished()
        }
    }

    private fun partyFinished() {
        Log.d("DATAEXCHANGE", "Party finished")
        // TODO on arrête le multijoueur (fermeture de socket)
        finish()
    }
}