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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlin.random.Random

class QuickQuiz : ComponentActivity() {

    // listes des questions
    private var questions : MutableList<String> = mutableListOf(
        "Quel est le nom du cheval de Lucky Luke ?",
        "Combien il y a t'il de BD Lucky Luke ?",
        "Quel est le plus grand des frères Dalton ?",
        "Quels sont les prénoms des frères Dalton ?"
    )

    // listes des propositions
    private var choices :  MutableList<List<String>> = mutableListOf(
        listOf(
            "Joly Jumper",
            "Jolie Jumper",
            "Jollie Jumper",
            "Jolly Jumper",
        ),
        listOf(
            "85",
            "80",
            "75",
            "70"
        ),
        listOf(
            "Jack",
            "Joe",
            "William",
            "Averell"
        ),
        listOf(
            "Jack, John, William et Averell",
            "Jack, Joe, Wilfried et Averell",
            "Jack, Joe, William et Alfred",
            "Jack, Joe, William et Averell"
        )

    )

    // liste des réponses correctes
    private var answers : MutableList<String> = mutableListOf(
        "Jolly Jumper",
        "85",
        "Averell",
        "Jack, Joe, William et Averell"
    )

    private var isAnswered : Boolean = false // indique si le joueur a répondu ou non
    private var numberOfQuestions : Int = 3 // nombre de questions à poser
    private var questionNumber : Int = -1 // numéro de la question posée

    // éléments graphiques
    private lateinit var choice1 : Button
    private lateinit var choice2 : Button
    private lateinit var choice3 : Button
    private lateinit var choice4 : Button
    private lateinit var next : Button
    private lateinit var scoreView : TextView
    private lateinit var questionView : TextView

    // données échangées
    private var isMulti : Boolean = false
    private var isServer : Boolean = false
    private var score : Int = 0
    private var scoreAdversaire : Int = 0
    private var isAdversaireAnswered : Boolean = false // indique si l'adversaire a répondu ou non
    private var scoreSent : Boolean = false // indique si le score a été envoyé

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quick_quiz)

        // Récupération des données fournies
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)

        // Message affiché pour expliquer les règles du jeu
        if (!isMulti) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Règles du jeu")
            builder.setMessage("Ici, vous pouvez écrire les règles de votre jeu.")
            builder.setPositiveButton("Jouer") { dialog, which ->
                // Le jeu se lance quand le joueur clique sur "Jouer"
                startGame()
            }
            val dialog = builder.create()
            // Afficher la boîte de dialogue lorsque l'activité est créée
            dialog.show()
        } else {
            // TODO : afficher les règles du jeu en multi
            Log.d("DATAEXCHANGE", "[QuickQuiz] Mode multijoueurs")
            initMulti()
            // Le jeu se lance directement en multi
            startGame()
        }
    }

    /**
     * Initialise les handler des DataExchange pour le mode multijoueurs
     */
    private fun initMulti() {
        if (isServer) {
            Log.d("DATAEXCHANGE", "[QuickQuiz Server] DataExchange launched")
            val handlerServer = object : Handler(Looper.getMainLooper()) { // pour recevoir le numéro de la question
                override fun handleMessage(msg: Message) {
                    Log.d("DATAEXCHANGE", "[QuickQuiz Server] Message received: " + msg.obj.toString())
                    if (msg.obj.toString().contains("score")) {
                        Log.d("DATAEXCHANGE", "[Server] Réception du score de l'adversaire")
                        scoreAdversaire = msg.obj.toString().split(":")[1].toInt()
                        if (!scoreSent) {
                            Log.d("DATAEXCHANGE", "[Server] Envoi du score en retour")
                            Multiplayer.Exchange.dataExchangeServer.write("score:$score")
                            scoreSent = true
                        }
                        val intent = Intent(this@QuickQuiz, ScorePage::class.java)
                        intent.putExtra("score", score)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[Server] On lance la page de score")
                        startActivityForResult(intent, 1)
                    }
                    else {
                        Log.d("DATAEXCHANGE", "[Server] Mise à jour de la question")
                        // On met à jour les éléments graphiques de la question
                        deleteOldQuestion()
                        questionNumber = msg.obj.toString().toInt()
                        setQuestion()
                    }
                }
            }
            Multiplayer.Exchange.dataExchangeServer.cancel()
            Multiplayer.Exchange.dataExchangeServer = DataExchange(handlerServer)
            Multiplayer.Exchange.dataExchangeServer.start()
        } else {
            Log.d("DATAEXCHANGE", "[QuickQuiz Client] DataExchange launched")
            val handlerClient = object : Handler(Looper.getMainLooper()) { // pour savoir quand le serveur a terminé
                override fun handleMessage(msg: Message) {
                    Log.d("DATAEXCHANGE", "[QuickQuiz Client] Message received: " + msg.obj.toString())
                    if (msg.obj.toString().contains("score")) {
                        Log.d("DATAEXCHANGE", "[Server] Réception du score de l'adversaire")
                        scoreAdversaire = msg.obj.toString().split(":")[1].toInt()
                        if (!scoreSent) {
                            Log.d("DATAEXCHANGE", "[Client] Envoi du score en retour")
                            Multiplayer.Exchange.dataExchangeClient.write("score:$score")
                            scoreSent = true
                        }
                        val intent = Intent(this@QuickQuiz, ScorePage::class.java)
                        intent.putExtra("score", score)
                        intent.putExtra("scoreAdversaire", scoreAdversaire)
                        intent.putExtra("isMulti", true)
                        intent.putExtra("isServer", isServer)
                        Log.d("DATAEXCHANGE", "[Client] On lance la page de score")
                        startActivityForResult(intent, 1)
                    } else {
                        // On met à jour la variable isAdversaireAnswered
                        isAdversaireAnswered = true
                        if (isAnswered) { // Si le joueur a déjà répondu, on passe à la question suivante
                            if (numberOfQuestions != 0) {
                                // On peut passer à la suite
                                Log.d("DATAEXCHANGE", "[Client] On peut passer à la question suivante car on a eu le retour")
                                nextQuestion()
                            } else {
                                Log.d("DATAEXCHANGE", "[QuickQuiz] Client envoie le score")
                                Multiplayer.Exchange.dataExchangeClient.write("score:$score")
                                scoreSent = true
                            }
                        }
                    }

                }
            }
            Multiplayer.Exchange.dataExchangeClient.cancel()
            Multiplayer.Exchange.dataExchangeClient = DataExchange(handlerClient)
            Multiplayer.Exchange.dataExchangeClient.start()
        }
    }

    private fun startGame() {
        // Initialisation des éléments graphiques
        questionView = findViewById(R.id.question)
        scoreView = findViewById(R.id.score)
        choice1 = findViewById(R.id.choice1)
        choice2 = findViewById(R.id.choice2)
        choice3 = findViewById(R.id.choice3)
        choice4 = findViewById(R.id.choice4)
        next = findViewById(R.id.next)

        // Initialisation des actions liées au clic sur les boutons

        choice1.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            if (!isAnswered) {
                val goodAnswer = checkAnswer((choice1.text.toString()))
                updateScore(goodAnswer)
                if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
                isAnswered = true
            }
            if (isMulti && isServer) { // On indique au client qu'on a répondu
                Multiplayer.Exchange.dataExchangeServer.write("Answered")
            }
        }

        choice2.setOnClickListener { // on pourra changer le fond en rouge ou vert en fonction de la réponse
            if (!isAnswered) {
                val goodAnswer = checkAnswer((choice2.text.toString()))
                updateScore(goodAnswer)
                if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
                isAnswered = true
            }
            if (isMulti && isServer) {
                if (numberOfQuestions != 0) {
                    Multiplayer.Exchange.dataExchangeServer.write("Answered")
                }
            }
        }

        choice3.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            if (!isAnswered) {
                val goodAnswer = checkAnswer((choice3.text.toString()))
                updateScore(goodAnswer)
                if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
                isAnswered = true
            }
            if (isMulti && isServer) {
                Multiplayer.Exchange.dataExchangeServer.write("Answered")
            }
        }

        choice4.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            if (!isAnswered) {
                val goodAnswer = checkAnswer((choice4.text.toString()))
                updateScore(goodAnswer)
                if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
                isAnswered = true
            }
            if (isMulti && isServer) {
                Multiplayer.Exchange.dataExchangeServer.write("Answered")
            }
        }

        next.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            if (!isAnswered) {
                Toast.makeText(this, "Vous devez répondre à la question", Toast.LENGTH_SHORT).show()
            } else {
                if (!isMulti) { // mode solo
                    if (numberOfQuestions == 0) {
                        // TO DO on met l'activité de fin
                        startActivityForResult(Intent(this, ScorePage::class.java).putExtra("score", score), 1)
                    } else {
                        nextQuestion()
                    }
                } else { // mode multijoueurs
                    Log.d("DATAEXCHANGE", "[QuickQuiz] isAdversaireAnswered : $isAdversaireAnswered")
                    if (isServer || !isAdversaireAnswered) {
                        Toast.makeText(this, "En attente de l'autre joueur", Toast.LENGTH_SHORT).show()
                    } else { // côté client
                        if (numberOfQuestions != 0) {
                            Log.d("DATAEXCHANGE", "[QuickQuiz] On peut passer à la question suivante")
                            nextQuestion()
                        } else {
                            Log.d("DATAEXCHANGE", "[QuickQuiz] Client envoie le score car le serveur a répondu")
                            Multiplayer.Exchange.dataExchangeClient.write("score:$score")
                            scoreSent = true
                        }
                    }
                }
            }


        }
        if (!isServer) { // 1ère question
            if (isMulti) Thread.sleep(500) // On attend que le thread d'échange soit bien lancé chez le serveur
            nextQuestion()
        }
    }

    /**
     * met à jour la question (textView) et les choix de réponses (button)
     */
    private fun nextQuestion() {
        deleteOldQuestion()
        isAdversaireAnswered = false
        questionNumber = Random.nextInt(questions.size)// génère un nombre aléatoire entre 0 et le dernier indice de la liste
        if (isMulti) { // on envoie le numéro question à l'autre joueur
            Multiplayer.Exchange.dataExchangeClient.write(questionNumber.toString())
        }
        // On met à jour les éléments graphiques
        setQuestion()
    }

    /**
     * On enlève la question précédente de la liste des questions
     */
    private fun deleteOldQuestion() {
        if (questionNumber != -1){ // on n'est pas à la première question
            // On supprime les éléments de la question que l'on venait de poser
            choices.removeAt(questionNumber)
            answers.removeAt(questionNumber)
            questions.removeAt(questionNumber)
        }
    }

    /**
     * Affiche la question et les réponses
     */
    private fun setQuestion() {
        isAnswered = false
        val question = questions[questionNumber]
        questionView.text = question
        val listOfChoices = choices[questionNumber]
        choice1.text = listOfChoices[0]
        choice2.text = listOfChoices[1]
        choice3.text = listOfChoices[2]
        choice4.text = listOfChoices[3]
        numberOfQuestions--
        if (isMulti && numberOfQuestions == 0) {
            Log.d("DATAEXCHANGE", "[QuickQuiz] On est à la dernière question")
        }
    }

    /**
     * @param answer, la réponse donnée par le joueur
     * renvoie true si la réponse est bonne, fausse sinon
     */
    private fun checkAnswer(answer : String) : Boolean {
        return answer.equals(answers[questionNumber])
    }

    /**
     * @param goodAnswer, booléen indiquant si le joueur a bien répondu ou non
     * met à jour l'affichage du score sur l'écran de jeu
     */
    private fun updateScore(goodAnswer : Boolean) {
        score += calculateScore(goodAnswer)
        scoreView.text = score.toString()
    }

    /**
     * @param goodAnswer, booléen indiquant si le joueur a bien répondu ou non
     * calcule le score en prenant compte le temps de réponse du joueur (ajouter un compteur du temps en attribut de classe)
     */
    private fun calculateScore(goodAnswer : Boolean) : Int {
        return if (goodAnswer) 10 else 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DATAEXCHANGE", "[QuickQuiz] onActivityResult, resultCode : $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            Log.d("DATAEXCHANGE", "score :" + data?.getIntExtra("score", 0).toString())
            Log.d("DATAEXCHANGE", "scoreAdversaire :" + data?.getIntExtra("scoreAdversaire", 0).toString())
            score = data?.getIntExtra("score", 0) ?: 0
            scoreAdversaire = data?.getIntExtra("scoreAdversaire", 0) ?: 0
            Log.d("DATAEXCHANGE", "score : $score, scoreAdversaire : $scoreAdversaire")
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            resultIntent.putExtra("scoreAdversaire", scoreAdversaire)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}