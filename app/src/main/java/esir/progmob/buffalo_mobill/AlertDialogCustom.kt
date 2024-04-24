package esir.progmob.buffalo_mobill

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView

class AlertDialogCustom(private val context : Context, private val titre : String, private val message : String, private val textButton : String, val onOkClicked : () -> Unit) {

    fun create(): Unit {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null)

        // Mise à jour des éléments graphiques

        val titleView = view.findViewById<TextView>(R.id.title)
        titleView.text = titre
        val messageView = view.findViewById<TextView>(R.id.message)
        messageView.text = message
        val buttonView = view.findViewById<TextView>(R.id.button)
        builder.setView(view)
        val dialog = builder.create()
        buttonView.text = textButton
        buttonView.setOnClickListener {
            Log.d("AlertDialogCustom", "Button clicked")
            onOkClicked()
            dialog.dismiss()
        }
        dialog.show()
    }

}