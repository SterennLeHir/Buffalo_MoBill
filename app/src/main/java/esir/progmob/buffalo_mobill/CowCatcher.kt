package esir.progmob.buffalo_mobill

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import androidx.activity.ComponentActivity

class CowCatcher : ComponentActivity() {
    private lateinit var lasso: ImageView
    private lateinit var gest: GestureDetector
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.cow_catcher)
        lasso = findViewById(R.id.lassoView)

        // Créer une instance de GestureDetector avec votre OnGestureListener
        gest = GestureDetector(this, MyGestureListener())

        /*
        // Ajouter un OnTouchListener à la vue FlingBallView
        lasso.setOnTouchListener { _, event ->
            // Transmettre les événements tactiles au GestureDetector
            gest.onTouchEvent(event)
        }
        */
    }

    private inner class MyGestureListener : GestureDetector.OnGestureListener {
        // METHODES DE ONGESTURELISTENER
        override fun onDown(p0: MotionEvent): Boolean {
            Log.d("Lasso", "Lasso sélectionné")
            return true
        }
        override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            Log.d("Lasso", "Lasso qui bouge")
            return true
        }
        override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            Log.d("Lasso", "Lasso lancé")
            return true
        }
        //non imlpémentée
        override fun onShowPress(p0: MotionEvent) {
        }
        override fun onSingleTapUp(p0: MotionEvent): Boolean {
            return true
        }
        override fun onLongPress(p0: MotionEvent) {
        }
    }
}