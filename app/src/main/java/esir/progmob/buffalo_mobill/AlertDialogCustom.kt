package esir.progmob.buffalo_mobill

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

class AlertDialogCustom(private val context : Context, private val titre : String, private val message : String, private val textButton : String, private val adapter : ArrayAdapter<String>? = null, val onOkClicked : (Int) -> Unit) {

    fun create(): AlertDialog {
        val builder = AlertDialog.Builder(context)
        // On récupère le layout
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null)

        // Mise à jour des éléments graphiques
        val titleView = view.findViewById<TextView>(R.id.title)
        titleView.text = titre
        val messageView = view.findViewById<TextView>(R.id.message)
        messageView.text = message
        val buttonView = view.findViewById<TextView>(R.id.button)
        val spinner = view.findViewById<Spinner>(R.id.spinner)

        if (adapter != null) { // On veut afficher le spinner
            adapter.setDropDownViewResource(R.layout.spinner)
            spinner.adapter = adapter
        } else {
            spinner.visibility = View.GONE
        }

        builder.setView(view)
        val dialog = builder.create()
        buttonView.text = textButton
        buttonView.setOnClickListener {
            Log.d("AlertDialogCustom", "Button clicked")
            if (adapter != null) {
                onOkClicked(spinner.selectedItem.toString().toInt()) // On récupère la valeur du spinner
            } else {
                onOkClicked(0) // On n'a pas de spinner
            }
        }
        return dialog
    }

}