package esir.progmob.buffalo_mobill

import android.app.Activity
import android.app.AlertDialog
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
    private var isReady : Boolean = false
    private var isAdversaireReady : Boolean = false
    private var score : Int = 0
    private var scoreAdversaire : Int = 0
    private var numberOfParties : Int = 0
    private val NUMBEROFPARTIESMAX = 3

    // Liste des jeux (pour le mode aléatoire)
    private var gameList : MutableList<String> = mutableListOf()
    private var isRandom : Boolean = false
    private val games : MutableList<String> = mutableListOf()
    private lateinit var nextGame : String

    private lateinit var alertDialog : AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)
        Log.d("DATAEXCHANGE", "isServer : $isServer")
        if (isMulti) initMulti()
        setContentView(R.layout.gamelist)

        // Initialisation de la liste des jeux
        gameList = mutableListOf("ShadyShowdown", "QuickQuiz", "MilkMaster", "WildRide", "CowCatcher")
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
                    startActivityForResult(game1, 1)
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
            if (!isServer) {
                if (isMulti) Multiplayer.Exchange.dataExchangeClient.write("Random")
                numberOfParties = 0
                createRandomParty()
            } else {
                Toast.makeText(this, "L'autre joueur choisit le jeu", Toast.LENGTH_SHORT).show()
            }
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
            Log.d("DATAEXCHANGE", "Random games : " + games[0] + " " + games[1] + " " + games[2])
            nextGame = games[numberOfParties]
            val randomGame = Intent(this, Class.forName("esir.progmob.buffalo_mobill.$nextGame"))
            randomGame.putExtra("isServer", isServer)
            randomGame.putExtra("isMulti", isMulti)
            if (isMulti) {
                val customAlertDialog = AlertDialogCustom(this, "PROCHAIN JEU", "Le jeu va commencer", "PRÊT") {
                    isReady = true
                    Log.d("DATAEXCHANGE", "Le client est prêt : $isReady et le serveur est prêt : $isAdversaireReady")
                    if (isAdversaireReady) {
                        Multiplayer.Exchange.dataExchangeClient.write("Ready")
                        alertDialog.dismiss()
                        Multiplayer.Exchange.dataExchangeClient.write(nextGame)
                        startActivityForResult(randomGame, 1)
                    }
                }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            } else {
                val customAlertDialog = AlertDialogCustom(this, "PROCHAIN JEU", "Le jeu va commencer", "PRÊT") {
                    alertDialog.dismiss()
                    startActivityForResult(randomGame, 1)
                }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            }
        } else { // dans le cas du serveur
            val customAlertDialog = AlertDialogCustom(this, "PROCHAIN JEU", "Le jeu va commencer", "PRÊT") {
                isReady = true
                Multiplayer.Exchange.dataExchangeServer.write("Ready")
            }
            alertDialog = customAlertDialog.create()
            alertDialog.show()
        }
    }

    private fun initMulti() {
        if (isServer) {
            val handlerServer = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    // Traitez le message ici
                    val message = msg.obj.toString()
                    Log.d("DATAEXCHANGE", "[GameList Server] Message received: " + msg.what.toString() + " " + message)
                    when (message) {
                        "Random" -> {
                            numberOfParties = 0
                            isRandom = true
                            createRandomParty()
                        }
                        "Ready" -> {
                            if (isReady) {
                                alertDialog.dismiss()
                            }
                        }
                        "CowCatcher" -> {
                            val intent = Intent(this@GameList, CowCatcher::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivityForResult(intent, 1)
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
                            this@GameList.startActivityForResult(intent, 1)
                        }

                        "WildRide" -> {
                            val intent = Intent(this@GameList, WildRide::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivityForResult(intent, 1)
                        }

                        "PricklyPicking" -> {
                            val intent = Intent(this@GameList, PricklyPicking::class.java)
                            intent.putExtra("isMulti", true)
                            intent.putExtra("isServer", true)
                            this@GameList.startActivityForResult(intent, 1)
                        }
                    }
                }
            }
            Log.d("DATAEXCHANGE", "Serveur set handler")
            Multiplayer.Exchange.dataExchangeServer.setHandler(handlerServer) // On met à jour l'handler du serveur
            Log.d("DATAEXCHANGE", "DataExchange Serveur thread launched")
        } else {
            val handlerClient = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    // Traitez le message ici
                    val message = msg.obj.toString()
                    Log.d("DATAEXCHANGE", "[GameList Client] Message received: " + msg.what.toString() + " " + message)
                    if (message == "Ready") {
                        isAdversaireReady = true
                        if (isReady) {
                            val randomGame = Intent(this@GameList, Class.forName("esir.progmob.buffalo_mobill.$nextGame"))
                            randomGame.putExtra("isServer", isServer)
                            randomGame.putExtra("isMulti", isMulti)
                            Multiplayer.Exchange.dataExchangeClient.write("Ready")
                            alertDialog.dismiss()
                            Multiplayer.Exchange.dataExchangeClient.write(nextGame)
                            startActivityForResult(randomGame, 1)
                        }
                    }
                }
            }
            Log.d("DATAEXCHANGE", "Client set handler")
            Multiplayer.Exchange.dataExchangeClient.setHandler(handlerClient) // On met à jour l'handler du serveur
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
        if(requestCode == 2) { // La page de score finale s'est terminée
            partyFinished()
        } else if (resultCode == Activity.RESULT_OK) {
            score += data?.getIntExtra("score", 0) ?: 0
            scoreAdversaire += data?.getIntExtra("scoreAdversaire", 0) ?: 0
            Log.d("DATAEXCHANGE", "score : $score, scoreAdversaire : $scoreAdversaire")
            Log.d("DATAEXCHANGE", "numberOfParties : $numberOfParties")
            if (numberOfParties == NUMBEROFPARTIESMAX) {
                val intent = Intent(this, ScorePage::class.java)
                intent.putExtra("score", score)
                intent.putExtra("scoreAdversaire", scoreAdversaire)
                intent.putExtra("isMulti", isMulti)
                intent.putExtra("isServer", isServer)
                startActivityForResult(intent, 2)
            } else if (isRandom && !isServer) {
                Log.d("DATAEXCHANGE", "Chosing next random game")
                nextGame = games[numberOfParties]
                val randomGame = Intent(this, Class.forName("esir.progmob.buffalo_mobill.$nextGame"))
                randomGame.putExtra("isServer", isServer)
                randomGame.putExtra("isMulti", isMulti)
                if (isMulti) {
                    Log.d("DATAEXCHANGE", "Affichage de la boîte de dialogue")
                    val customAlertDialog = AlertDialogCustom(this, "PROCHAIN JEU", "Le prochain jeu est : $nextGame", "PRÊT") {
                        Multiplayer.Exchange.dataExchangeClient.write(nextGame) // On indique au serveur le jeu choisi
                        alertDialog.dismiss()
                        startActivityForResult(randomGame, 1)
                    }
                    alertDialog = customAlertDialog.create()
                    alertDialog.show()
                } else {
                    val customAlertDialog = AlertDialogCustom(this, "PROCHAIN JEU", "Le prochain jeu est : $nextGame", "PRÊT") {
                        alertDialog.dismiss()
                        startActivityForResult(randomGame, 1)
                    }
                    alertDialog = customAlertDialog.create()
                    alertDialog.show()
                }
            }
        }
    }

    override fun onRestart() { // appelée après la fin de la page de score
        isReady = false
        isAdversaireReady = false
        super.onRestart()
        numberOfParties++
        Log.d("DATAEXCHANGE", "GameList restarted")
        if (isMulti) initMulti()
    }

    private fun partyFinished() {
        Log.d("DATAEXCHANGE", "Party finished")
        // TODO on arrête le multijoueur (fermeture de socket)
        if (isServer) {
            Multiplayer.Exchange.dataExchangeServer.cancel()
            Multiplayer.Exchange.dataExchangeServer.getInputStream().close()
            Multiplayer.Exchange.dataExchangeServer.getOutputStream().close()
            Multiplayer.SocketHolder.socket?.close()
            Multiplayer.SocketHolder.socket = null
        } else {
            Multiplayer.Exchange.dataExchangeClient.cancel()
        }
        finish()
    }
}