package esir.progmob.buffalo_mobill

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.random.Random

class QuickQuiz : ComponentActivity() {

    // listes des questions
    var questions : MutableList<String> = mutableListOf(
        "Quel est le nom du cheval de Lucky Luke ?",
        "Combien il y a t'il de BD Lucky Luke ?",
        "Quel est le plus grand des frères Dalton ?",
        "Quels sont les prénoms des frères Dalton ?"
    )

    // listes des propositions
    var choices :  MutableList<List<String>> = mutableListOf(
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
    var answers : MutableList<String> = mutableListOf(
        "Jolly Jumper",
        "85",
        "Averell",
        "Jack, Joe, William et Averell"
    )

    var numberOfQuestions : Int = 1
    var questionNumber : Int = -1
    var score : Int = 0

    // éléments graphiques
    lateinit var choice1 : Button
    lateinit var choice2 : Button
    lateinit var choice3 : Button
    lateinit var choice4 : Button
    lateinit var next : Button
    lateinit var scoreView : TextView
    lateinit var questionView : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quick_quiz)
        // Message affiché pour expliquer les règles du jeu

        // Le jeu se lance quand le joueur clique sur "Jouer"

        // Initialisation des éléments graphiques
        questionView = findViewById(R.id.question)
        scoreView = findViewById(R.id.score)
        choice1 = findViewById(R.id.choice1)
        choice2 = findViewById(R.id.choice2)
        choice3 = findViewById(R.id.choice3)
        choice4 = findViewById(R.id.choice4)
        next = findViewById(R.id.next)

        // Initialisation des actions liés au clic sur les boutons
        choice1.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            var goodAnswer = checkAnswer((choice1.text.toString()))
            updateScore(goodAnswer)
        }
        choice2.setOnClickListener { // on pourra changer le fond en rouge ou vert en fonction de la réponse
            var goodAnswer = checkAnswer((choice2.text.toString()))
            updateScore(goodAnswer)
        }
        choice3.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            var goodAnswer = checkAnswer((choice3.text.toString()))
            updateScore(goodAnswer)
        }
        choice4.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            var goodAnswer = checkAnswer((choice4.text.toString()))
            updateScore(goodAnswer)
        }
        next.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            if (numberOfQuestions == 0) {
                // on met l'activité de fin
            } else {
                nextQuestion()
            }
        }

    }

    /**
     * met à jour la question (textView) et les choix de réponses (button)
     */
    fun nextQuestion() {

        if (questionNumber != -1){ // on n'est pas à la première question
            // On supprime les éléments de la question que l'on venait de poser
            choices.removeAt(questionNumber)
            answers.removeAt(questionNumber)
            questions.removeAt(questionNumber)
        }
        questionNumber = Random.nextInt(questions.size) // génère un nombre aléatoire entre 0 et le dernier indice de la liste

        // On met à jour les éléments graphiques
        val question = questions[questionNumber]
        questionView.text = question
        val listOfChoices = choices[questionNumber]
        choice1.text = listOfChoices[0]
        choice2.text = listOfChoices[1]
        choice3.text = listOfChoices[2]
        choice4.text = listOfChoices[3]
    }

    /**
     * @param answer, la réponse donnée par le joueur
     * renvoie true si la réponse est bonne, fausse sinon
     */
    fun checkAnswer(answer : String) : Boolean {
        return answer.equals(answers[questionNumber])
    }

    /**
     * @param goodAnswer, booléen indiquant si le joueur a bien répondu ou non
     * met à jour l'affichage du score sur l'écran de jeu
     */
    fun updateScore(goodAnswer : Boolean) {
        var newScore : Int = score + calculateScore(goodAnswer)
        scoreView.text = newScore.toString()
    }

    /**
     * @param goodAnswer, booléen indiquant si le joueur a bien répondu ou non
     * calcule le score en prenant compte le temps de réponse du joueur (ajouter un compteur du temps en attribut de classe)
     */
    fun calculateScore(goodAnswer : Boolean) : Int {
        return 10
    }
}