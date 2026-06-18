package sheridan.randlem.nfc_toolsble

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcF
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReadActivity : AppCompatActivity() {
    private lateinit var textView: TextView

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    //private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
    //private val techListsArray = arrayOf(arrayOf(android.nfc.tech.NfcA::class.java.name))
    private val techListsArray = null

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_read)
//
//        textView = findViewById(R.id.text_view)
//
//        // Check NFC availability
//        if (nfcAdapter == null) {
//            Toast.makeText(this, "NFC not supported", Toast.LENGTH_SHORT).show()
//        } else if (!nfcAdapter!!.isEnabled) {
//            Toast.makeText(this, "Please turn on NFC", Toast.LENGTH_SHORT).show()
//        }
//
//        // Prepare pending intent for NFC detection
//        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
//        //val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
////        try {
////            ndef.addDataType("text/plain")
////        } catch (e: IntentFilter.MalformedMimeTypeException) {
////            throw RuntimeException("fail", e)
////        }
////        intentFiltersArray = arrayOf(ndef)
//
////        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
////        ndef.addDataType("text/plain")
////
////        val tech = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
////
////        intentFiltersArray = arrayOf(ndef, tech)
//
//        val tech = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
//        intentFiltersArray = arrayOf(tech)
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
//        // Enable NFC foreground dispatch to listen for tags
//        nfcAdapter?.enableForegroundDispatch(
//            this,
//            pendingIntent,
//            intentFiltersArray,
//            techListsArray
//        )
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

//    override fun onPause() {
//        // Disable NFC foreground dispatch
//        if (this.isFinishing) {
//            nfcAdapter?.disableForegroundDispatch(this)
//        }
//        super.onPause()
//    }
    override fun onPause() {
        super.onPause()
        //nfcAdapter?.disableForegroundDispatch(this)
    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//
//        Toast.makeText(this, "NFC detected!", Toast.LENGTH_SHORT).show()
//
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
//            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
//            val ndef = Ndef.get(tag) ?: return
//
//            try {
//                ndef.connect()
//                val ndefMessage = ndef.cachedNdefMessage
//                val records = ndefMessage.records
//
//                if (records.isNotEmpty()) {
//                    // Assuming the message is stored in the first record
//                    val messageRecord = records[0]
//                    //val message = String(messageRecord.payload).drop(3)
//                    val payload = messageRecord.payload
//                    val textEncoding = if ((payload[0].toInt() and 0x80) == 0) "UTF-8" else "UTF-16"
//                    val languageCodeLength = payload[0].toInt() and 0x3F
//                    val message = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, charset(textEncoding))
//                    textView.text = message // Set the message to the TextView
//                }
//
//                ndef.close()
//            } catch (e: Exception) {
//                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
//            }
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
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
            val ndef = Ndef.get(tag) ?: return

            try {
                ndef.connect()
                val ndefMessage = ndef.cachedNdefMessage
                val records = ndefMessage.records

                if (records.isNotEmpty()) {
                    // Assuming the message is stored in the first record
                    val messageRecord = records[0]
                    //val message = String(messageRecord.payload).drop(3)
                    val payload = messageRecord.payload
                    val textEncoding = if ((payload[0].toInt() and 0x80) == 0) "UTF-8" else "UTF-16"
                    val languageCodeLength = payload[0].toInt() and 0x3F
                    val message = String(
                        payload,
                        languageCodeLength + 1,
                        payload.size - languageCodeLength - 1,
                        charset(textEncoding)
                    )
                    textView.text = message // Set the message to the TextView
                }

                ndef.close()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}