package esir.progmob.buffalo_mobill

import android.os.Bundle
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
    private var numberOfQuestions : Int = 2
    private var questionNumber : Int = -1
    private var score : Int = 0

    // éléments graphiques
    private lateinit var choice1 : Button
    private lateinit var choice2 : Button
    private lateinit var choice3 : Button
    private lateinit var choice4 : Button
    private lateinit var next : Button
    private lateinit var scoreView : TextView
    private lateinit var questionView : TextView
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
            val goodAnswer = checkAnswer((choice1.text.toString()))
            isAnswered = true
            updateScore(goodAnswer)
            if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
        }
        choice2.setOnClickListener { // on pourra changer le fond en rouge ou vert en fonction de la réponse
            val goodAnswer = checkAnswer((choice2.text.toString()))
            isAnswered = true
            updateScore(goodAnswer)
            if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
        }
        choice3.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            val goodAnswer = checkAnswer((choice3.text.toString()))
            isAnswered = true
            updateScore(goodAnswer)
            if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
        }
        choice4.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            val goodAnswer = checkAnswer((choice4.text.toString()))
            isAnswered = true
            updateScore(goodAnswer)
            if (goodAnswer) Toast.makeText(this, "Réponse correcte", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show()
        }
        next.setOnClickListener{ // on pourra changer le fond en rouge ou vert en fonction de la réponse
            if (numberOfQuestions == 0) {
                // TO DO on met l'activité de fin
                finish()
            } else if (isAnswered){
                nextQuestion()
            }
        }
        nextQuestion() // 1ère question
    }

    /**
     * met à jour la question (textView) et les choix de réponses (button)
     */
    private fun nextQuestion() {
        if (questionNumber != -1){ // on n'est pas à la première question
            // On supprime les éléments de la question que l'on venait de poser
            choices.removeAt(questionNumber)
            answers.removeAt(questionNumber)
            questions.removeAt(questionNumber)
        }
        questionNumber = Random.nextInt(questions.size) // génère un nombre aléatoire entre 0 et le dernier indice de la liste
        isAnswered = false
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
}