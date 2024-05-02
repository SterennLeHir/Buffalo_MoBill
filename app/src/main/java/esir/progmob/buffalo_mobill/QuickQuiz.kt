package esir.progmob.buffalo_mobill

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.isInvisible
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
    private lateinit var alertDialog: AlertDialog

    // données échangées
    private var isMulti : Boolean = false
    private var isServer : Boolean = false
    private var score : Int = 0
    private var scoreAdversaire : Int = 0
    private var isAdversaireAnswered : Boolean = false // indique si l'adversaire a répondu ou non
    private var scoreSent : Boolean = false // indique si le score a été envoyé
    private var isReady : Boolean = false
    private var isAdversaireReady : Boolean = false

    //timer
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var timer : TextView
    private var seconds: Long = 0
    private val time : Int = 11
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération des données fournies
        isMulti = intent.getBooleanExtra("isMulti", false)
        isServer = intent.getBooleanExtra("isServer", false)

        // Message affiché pour expliquer les règles du jeu
        if (!isMulti) {
            // Affiche la boîte de dialogue lorsque l'activité est créée
            val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame4), "JOUER") {
                startGame()
                alertDialog.dismiss()
            }
            alertDialog = customAlertDialog.create()
            alertDialog.show()
        } else {
            Log.d("DATAEXCHANGE", "[QuickQuiz] Mode multijoueurs")
            initMulti()
            if (!isServer) {
                val customAlertDialog = AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame4), "JOUER") {
                    isReady = true
                    if (isAdversaireReady) {
                        Multiplayer.Exchange.dataExchangeClient.write("Ready")
                        alertDialog.dismiss()
                        startGame()
                    }
                }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            } else {
                val customAlertDialog =
                    AlertDialogCustom(this, "BUT DU JEU", getString(R.string.RulesGame4), "JOUER") {
                        isReady = true
                        Multiplayer.Exchange.dataExchangeServer.write("Ready")
                    }
                alertDialog = customAlertDialog.create()
                alertDialog.show()
            }

            // Le jeu se lance directement en multi
            //startGame()
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
                    } else if (msg.obj.toString().contains("Ready")) {
                        Log.d("DATAEXCHANGE", "[QuickQuiz] On peut lancer le jeu")
                        alertDialog.dismiss()
                        startGame()
                    } else {
                        Log.d("DATAEXCHANGE", "[Server] Mise à jour de la question")
                        // On met à jour les éléments graphiques de la question
                        cleanOldQuestion()
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
                    } else if (msg.obj.toString().contains("Ready")) {
                        isAdversaireReady = true
                        if (isReady) {
                            Multiplayer.Exchange.dataExchangeClient.write("Ready")
                            alertDialog.dismiss()
                            startGame()
                        }
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
        setContentView(R.layout.quick_quiz)
        questionView = findViewById(R.id.question)
        scoreView = findViewById(R.id.score)
        choice1 = findViewById(R.id.choice1)
        choice2 = findViewById(R.id.choice2)
        choice3 = findViewById(R.id.choice3)
        choice4 = findViewById(R.id.choice4)
        timer = findViewById(R.id.timerView)

        // Initialisation des actions liées au clic sur les boutons
        choice1.setOnClickListener{
            buttonOnClickListener(it as Button)
        }

        choice2.setOnClickListener {
            buttonOnClickListener(it as Button)
        }

        choice3.setOnClickListener{
            buttonOnClickListener(it as Button)
        }

        choice4.setOnClickListener{
            buttonOnClickListener(it as Button)
        }

        if (!isServer) { // 1ère question
            if (isMulti) Thread.sleep(500) // On attend que le thread d'échange soit bien lancé chez le serveur
            nextQuestion()
        }
    }

    private fun buttonOnClickListener(button : Button) {
        if (!isAnswered) { //si on a pas répondu
            //tuer le timer
            countDownTimer.cancel()
            val goodAnswer = checkAnswer((choice4.text.toString()))
            updateScore(goodAnswer)
            if (goodAnswer) {
                Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
                button.setBackgroundResource(R.drawable.green_button)
            }
            else {
                Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
                button.setBackgroundResource(R.drawable.red_button)
            }
            gestionSynch()
        }
    }

    private fun gestionSynch(){
        isAnswered = true
        if (isMulti && !isServer) {
            if (isAdversaireAnswered) {
                if (numberOfQuestions != 0) {
                    Log.d("DATAEXCHANGE", "[QuickQuiz] On peut passer à la question suivante")
                    Handler(Looper.getMainLooper()).postDelayed({
                        nextQuestion()
                    }, 2000)
                } else {
                    Log.d("DATAEXCHANGE", "[QuickQuiz] Client envoie le score car le serveur a répondu")
                    Multiplayer.Exchange.dataExchangeClient.write("score:$score")
                    scoreSent = true
                }
            }
        } else if (!isMulti) {
            if (numberOfQuestions == 0) {
                val intent = Intent(this, ScorePage::class.java)
                intent.putExtra("score", score)
                intent.putExtra("game", "QuickQuiz")
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivityForResult(intent, 1)
                }, 2000)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    nextQuestion()
                }, 2000)
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                Multiplayer.Exchange.dataExchangeServer.write("Answered")
            }, 2000)
        }
    }

    /**
     * met à jour la question (textView) et les choix de réponses (button)
     */
    private fun nextQuestion() {
        cleanOldQuestion()
        isAdversaireAnswered = false
        questionNumber = Random.nextInt(questions.size)// génère un nombre aléatoire entre 0 et le dernier indice de la liste
        if (isMulti) { // on envoie le numéro question à l'autre joueur
            Multiplayer.Exchange.dataExchangeClient.write(questionNumber.toString())
        }

        //On set le timer pour la question
        val timeInMillis: Long = time.toLong() * 1000 // 5s par question

        // Initialisation du CountDownTimer
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Mise à jour de l'affichage du timer chaque seconde
                seconds = millisUntilFinished / 1000
                timer.text = "Temps restant : $seconds secondes"
            }

            override fun onFinish() {
                timer.text = "Temps écoulé!"
                gestionSynch()
            }
        }

        // Démarrage du CountDownTimer
        countDownTimer.start()

        // On met à jour les éléments graphiques
        setQuestion()
    }

    /**
     * On enlève la question précédente de la liste des questions et on remet les boutons dans leur état initial
     */
    private fun cleanOldQuestion() {
        if (questionNumber != -1){ // on n'est pas à la première question
            // On supprime les éléments de la question que l'on venait de poser
            choices.removeAt(questionNumber)
            answers.removeAt(questionNumber)
            questions.removeAt(questionNumber)
            choice1.setBackgroundResource(R.drawable.brown_button)
            choice2.setBackgroundResource(R.drawable.brown_button)
            choice3.setBackgroundResource(R.drawable.brown_button)
            choice4.setBackgroundResource(R.drawable.brown_button)
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
        return if (goodAnswer) {
            seconds.toInt()
        } else {
            0
        }
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