package sheridan.randlem.nfc_toolsble

import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.app.PendingIntent
import android.content.IntentFilter

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcF

class WriteActivity : AppCompatActivity() {

    private lateinit var editText: EditText

    private var intentFiltersArray: Array<IntentFilter>? = null
    //private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
    //private val techListsArray = arrayOf(arrayOf(android.nfc.tech.NfcA::class.java.name))
    private val techListsArray = null
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var pendingIntent: PendingIntent? = null

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_write)
//
//        editText = findViewById(R.id.edit_text)
//
//        // prepare pending Intent
//        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
//        } else {
//            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
//        }
//
//        //val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
////        try {
////            ndef.addDataType("text/plain")
////        } catch (e: IntentFilter.MalformedMimeTypeException) {
////            throw RuntimeException("fail", e)
////        }
////        intentFiltersArray = arrayOf(ndef)
////        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
////        ndef.addDataType("text/plain")
////
////        val tech = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
////
////        intentFiltersArray = arrayOf(ndef, tech)
//        val tech = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
//        intentFiltersArray = arrayOf(tech)
//
//        // Check NFC availability
//        if (nfcAdapter == null) {
//            Toast.makeText(this, "NFC not supported", Toast.LENGTH_SHORT).show()
//        } else if (!nfcAdapter!!.isEnabled) {
//            Toast.makeText(this, "Please turn on NFC", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        Toast.makeText(this, "Intent action: " + intent.action, Toast.LENGTH_LONG).show()
//        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
//        } else {
//            PendingIntent.getActivity(
//                this,
//                0,
//                intent,
//                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//            )
//        }
//
//        val tech = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
//        intentFiltersArray = arrayOf(tech)

        handleNfcIntent(intent) // safe now
    }

//    override fun onResume() {
//        super.onResume()
//        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
//    }

    override fun onResume() {
        super.onResume()

        if (nfcAdapter == null) return

//        if (pendingIntent == null) {
//            Toast.makeText(this, "PendingIntent NULL", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        nfcAdapter?.enableForegroundDispatch(
//            this,
//            pendingIntent,
//            intentFiltersArray,
//            techListsArray
//        )
    }

//    // handles new intent delivered to the activity. For e.g. NFC Intent
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//
//        Toast.makeText(this, "NFC detected!", Toast.LENGTH_SHORT).show()
//
////        try {
////            val message=editText.text.toString()
////            if(message != "") {
////                if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action
////                    || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action
////                ) {
////                    val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
////                    val ndef = Ndef.get(tag)
////
////                    if (ndef == null) {
////                        // Tag not formatted → format it
////                        val formatable = android.nfc.tech.NdefFormatable.get(tag)
////                        formatable?.connect()
////                        formatable?.format(
////                            NdefMessage(arrayOf(NdefRecord.createTextRecord("en", message)))
////                        )
////                        formatable?.close()
////
////                        Toast.makeText(this, "Tag formatted & written!", Toast.LENGTH_SHORT).show()
////                        return
////                    }
////
////                    if (ndef.isWritable) {
////                        val nfcMessage = NdefMessage(
////                            arrayOf(
////                                NdefRecord.createTextRecord("en", message)
////                            )
////                        )
////
////                        ndef.connect()
////                        ndef.writeNdefMessage(nfcMessage)
////                        ndef.close()
////
////                        Toast.makeText(applicationContext, "Successfully Written!", Toast.LENGTH_SHORT).show()
////                    }
////                }
////            } else {
////                Toast.makeText(applicationContext, "Write on text box!", Toast.LENGTH_SHORT).show()
////            }
////        }
////        catch (e:Exception) {
////            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
////        }
//        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
//
//        val message = editText.text.toString()
//        if (message.isEmpty()) {
//            Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val ndef = Ndef.get(tag)
//
//        if (ndef != null) {
//            ndef.connect()
//
//            val nfcMessage = NdefMessage(
//                arrayOf(NdefRecord.createTextRecord("en", message))
//            )
//
//            ndef.writeNdefMessage(nfcMessage)
//            ndef.close()
//
//            Toast.makeText(this, "Write success!", Toast.LENGTH_SHORT).show()
//        } else {
//            // FORMAT TAG
//            val formatable = android.nfc.tech.NdefFormatable.get(tag)
//            formatable?.connect()
//            formatable?.format(
//                NdefMessage(arrayOf(NdefRecord.createTextRecord("en", message)))
//            )
//            formatable?.close()
//
//            Toast.makeText(this, "Formatted + Written!", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        Toast.makeText(this, "Action: ${intent.action}", Toast.LENGTH_LONG).show()
        if (intent == null) return

        val action = intent.action

        if (action != NfcAdapter.ACTION_TECH_DISCOVERED &&
            action != NfcAdapter.ACTION_NDEF_DISCOVERED &&
            action != NfcAdapter.ACTION_TAG_DISCOVERED
        ) {
            return
        }

        Toast.makeText(this, "NFC detected!", Toast.LENGTH_SHORT).show()

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag == null) {
            Toast.makeText(this, "Tag is NULL", Toast.LENGTH_SHORT).show()
            return
        }

        // TEMP DEBUG
        Toast.makeText(this, "Tag detected successfully", Toast.LENGTH_SHORT).show()


        // 👉 your existing read/write logic goes here
        val message = editText.text.toString()
        if (message.isEmpty()) {
            Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show()
            return
        }

        val ndef = Ndef.get(tag)
        if (ndef == null) {
            Toast.makeText(this, "Tag not NDEF formatted", Toast.LENGTH_SHORT).show()
            return
        }

        if (ndef != null) {
            ndef.connect()

            val nfcMessage = NdefMessage(
                arrayOf(NdefRecord.createTextRecord("en", message))
            )

            ndef.writeNdefMessage(nfcMessage)
            ndef.close()

            Toast.makeText(this, "Write success!", Toast.LENGTH_SHORT).show()
        } else {
            // FORMAT TAG
            val formatable = android.nfc.tech.NdefFormatable.get(tag)
            formatable?.connect()
            formatable?.format(
                NdefMessage(arrayOf(NdefRecord.createTextRecord("en", message)))
            )
            formatable?.close()

            Toast.makeText(this, "Formatted + Written!", Toast.LENGTH_SHORT).show()
        }
    }


//    override fun onPause() {
//        if (this.isFinishing) {
//            nfcAdapter?.disableForegroundDispatch(this)
//        }
//        super.onPause()
//    }
override fun onPause() {
    super.onPause()
    //nfcAdapter?.disableForegroundDispatch(this)
}
}