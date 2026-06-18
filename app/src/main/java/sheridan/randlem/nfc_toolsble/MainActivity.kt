package sheridan.randlem.nfc_toolsble

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.*
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.radiobutton.MaterialRadioButton
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

enum class WriteState {
    WAITING, READY, WRITING, DONE
}

class MainActivity : AppCompatActivity() {

    private var devMode = false
    private lateinit var statusText: TextView
    private lateinit var textView: TextView
    private lateinit var editText: EditText
    private var writeMode = false

    private var currentState = WriteState.WAITING

    private fun updateState(state: WriteState) {
        currentState = state

        val text = when (state) {
            WriteState.WAITING -> "Waiting for command..."
            WriteState.READY -> "Tap tag to write"
            WriteState.WRITING -> "Writing..."
            WriteState.DONE -> "Done!"
        }

        runOnUiThread { statusText.text = text }
    }

    // NFC
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techListsArray: Array<Array<String>>? = null

    // Bluetooth
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    //private var gatt: android.bluetooth.BluetoothGatt? = null
    private var isTracking = false
    private var triangulate = false

    //private val handler = Handler(Looper.getMainLooper())

    // BLE for Signal Strength
    private var bleScanner: BluetoothLeScanner? = null
    //private var scanning = false

    private var currentAnchorId: String? = null


    // Pending write from Unity
    private var pendingJsonToWrite: String? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        statusText = findViewById(R.id.statusText)
        updateState(WriteState.WAITING)

        textView = findViewById(R.id.text_view)
        editText = findViewById(R.id.edit_text)

        editText.visibility = View.GONE

        val readBtn: Button = findViewById(R.id.read)
        val writeBtn: Button = findViewById(R.id.write)

        readBtn.setOnClickListener {
            writeMode = false
            Toast.makeText(this, "Tap tag to read", Toast.LENGTH_SHORT).show()
        }

        writeBtn.setOnClickListener {
            writeMode = true
            val json = editText.text.toString()

            if (json.isNotEmpty()) {
                pendingJsonToWrite = json
                updateState(WriteState.READY)

                Toast.makeText(this, "Tap tag to write", Toast.LENGTH_SHORT).show()
            }
        }

        readBtn.visibility = View.GONE
        writeBtn.visibility = View.GONE

        val devToggle: SwitchCompat = findViewById(R.id.devToggle)

        devToggle.setOnCheckedChangeListener { _, isChecked ->
            devMode = isChecked

            val mode = if (devMode) "DEV MODE" else "GAME MODE"
            Toast.makeText(this, mode, Toast.LENGTH_SHORT).show()

            if (devMode) {
                readBtn.visibility = View.VISIBLE
                writeBtn.visibility = View.VISIBLE
                editText.visibility = View.VISIBLE
            } else {
                readBtn.visibility = View.GONE
                writeBtn.visibility = View.GONE
                editText.visibility = View.GONE
            }
        }

        val connectBtn: Button = findViewById(R.id.connect)

        connectBtn.setOnClickListener {
            try {
                connectToPC("_SHALISSA_")
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        bleScanner = bluetoothAdapter?.bluetoothLeScanner

        val trackToggle: SwitchCompat = findViewById(R.id.trackingToggle)
        trackToggle.setOnCheckedChangeListener @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN) { _, isChecked ->
            isTracking = isChecked
            //getTrackOption()
            if (isTracking) {
                triangulate = true
                startBleScan()
            }
            else {
                triangulate = false
                stopBleScan()
                //handler.removeCallbacks(rssiRunnable)
                sendRssiToUnity(0, "-1")
            }
        }

        // NFC setup
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        pendingIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)

        intentFiltersArray = arrayOf(tagDetected, techDetected, ndefDetected)

        techListsArray = arrayOf(
            arrayOf(android.nfc.tech.NfcA::class.java.name)
        )
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val action = intent.action

        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag

            if (tag == null) {
                Toast.makeText(this, "TAG IS NULL", Toast.LENGTH_SHORT).show()
                return
            }

            Toast.makeText(this, "TAG OK", Toast.LENGTH_SHORT).show()

            if (pendingJsonToWrite != null) {
                writeTag(tag, pendingJsonToWrite!!)
                pendingJsonToWrite = null

                updateState(WriteState.DONE)

                Handler(Looper.getMainLooper()).postDelayed({
                    updateState(WriteState.WAITING)
                }, 2000)
            } else if (devMode && writeMode) {
                // Manual dev write
                val manual = editText.text.toString()

                if (manual.isNotEmpty()) {
                    writeTag(tag, manual)
                } else {
                    Toast.makeText(this, "Enter JSON first", Toast.LENGTH_SHORT).show()
                }
            } else {
                readTag(tag)
            }
        }
    }

    // =========================
    // NFC WRITE
    // =========================

    private fun writeTag(tag: Tag, json: String) {
        try {
            val record = NdefRecord.createMime("application/json", json.toByteArray())
            val message = NdefMessage(arrayOf(record))

            val ndef = Ndef.get(tag)

            if (ndef != null) {
                ndef.connect()
                ndef.writeNdefMessage(message)
                ndef.close()
            } else {
                val format = NdefFormatable.get(tag)
                format?.connect()
                format?.format(message)
                format?.close()
            }

            Toast.makeText(this, "JSON written!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Write failed", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================
    // NFC READ
    // =========================

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun readTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag) ?: return
            ndef.connect()

            val message = ndef.cachedNdefMessage ?: return
            val record = message.records[0]
            if (record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                String(record.type) == "application/json") {
                val json = String(record.payload)

                if (devMode) {
                    textView.text = json // show on screen
                }
                val trackToggle: SwitchCompat = findViewById(R.id.trackingToggle)
                val obj = JSONObject(json)

                if (obj.getString("type") == "anchor") {
                    trackToggle.isChecked = true
                    triangulate = false
                    currentAnchorId = obj.getString("id")
                    startBleScan()

                } else {
                    trackToggle.isChecked = false
                    currentAnchorId = null
                    stopBleScan()
                }

                send(json) // always send to Unity
                ndef.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // =========================
    // BLUETOOTH
    // =========================

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToPC(deviceName: String) {
        val device = bluetoothAdapter?.bondedDevices?.find { it.name == deviceName } ?: return

        Thread {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                socket = device.createRfcommSocketToServiceRecord(uuid)

                try {
                    bluetoothAdapter?.cancelDiscovery()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
                socket?.connect()

                outputStream = socket?.outputStream
                inputStream = socket?.inputStream

                listenForData()

                //gatt = device.connectGatt(this, false, gattCallback)

                runOnUiThread {
                    Toast.makeText(this, "Bluetooth Connected!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun listenForData() {
        Thread {
            val buffer = ByteArray(4096)

            while (true) {
                try {
                    val bytes = inputStream?.read(buffer) ?: break
                    val message = String(buffer, 0, bytes).trim()

                    handleIncomingCommand(message)

                } catch (e: Exception) {
                    break
                }
            }
        }.start()
    }

    private fun handleIncomingCommand(message: String) {
        try {
            val json = org.json.JSONObject(message)

            val action = json.getString("action")

            if (action == "write") {
                val payload = json.getJSONObject("payload")

                pendingJsonToWrite = payload.toString()

                updateState(WriteState.READY)

                runOnUiThread {
                    Toast.makeText(this, "Tap tag to write", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun send(json: String) {
        try {
            outputStream?.write((json + "\n").toByteArray())
            outputStream?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    private val gattCallback = object : android.bluetooth.BluetoothGattCallback() {
//
//        override fun onReadRemoteRssi(
//            gatt: android.bluetooth.BluetoothGatt,
//            rssi: Int,
//            status: Int
//        ) {
//            if (status == android.bluetooth.BluetoothGatt.GATT_SUCCESS && isTracking) {
//                sendRssiToUnity(rssi)
//            }
//        }
//    }

    private fun sendRssiToUnity(rssi: Int, id: String) {
        val json = """
        { "type": "rssi", "id": "$id", "value": $rssi }
    """.trimIndent()

        send(json)
    }

//    private val rssiRunnable = object : Runnable {
//        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
//        override fun run() {
//            if (isTracking) {
//                gatt?.readRemoteRssi()
//                handler.postDelayed(this, 500) // every 0.5 sec
//            }
//        }
//    }

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            val device = result.device
            val rssi = result.rssi
            val name = result.scanRecord?.deviceName ?: device.name

            Log.d("BLE", "Device: $name RSSI: $rssi")

            if (triangulate) {
                if (name != null && name.contains("torch")) {
                    sendRssiToUnity(rssi, name)
                }
            }
            else {
                val target = "torch_$currentAnchorId"
                if (name != null && name == target) {
                    sendRssiToUnity(rssi, name)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBleScan() {
        bleScanner?.startScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun stopBleScan() {
        bleScanner?.stopScan(scanCallback)
    }
}