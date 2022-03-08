package fr.adixon.adiposrfiddemo

import android.content.*
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

private const val MSG_SAY_HELLO = 0
private const val RFID_START = 1
private const val RFID_TERMINATE = 2
private const val RFID_CONNECTOR_STATUS = 3
private const val RFID_SCAN = 4

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var mContext: Context? = null
    private var mService: Messenger? = null
    private var bound = false
    private var serviceIntent: Intent? = null
    
    private var textViewRandomNumber: TextView? = null
    
    private var buttonBindService: Button? = null
    private var buttonUnBindService: Button? = null
    private var buttonHello: Button? = null
    private var buttonScan: Button? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            textViewRandomNumber?.text = intent.getStringExtra("data")
//            StringBuilder().apply {
//                append("Action: ${intent.action}\n")
//                append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
//                toString().also { log ->
//                    Log.d(TAG, log)
//                    Toast.makeText(context, log, Toast.LENGTH_LONG).show()
//                }
//            }
        }
    }

    private var mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            println("----------- onServiceConnected -----------")
            mService = Messenger(service)
            bound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            println("----------- onServiceDisconnected -----------")
            mService = null
            bound = false
        }
    }

    private fun sendMessageToService(msgCode: Int) {
        if (!bound) return
        // Create and send a message to the service, using a supported 'what' value
        val msg: Message = Message.obtain(null, msgCode, 0, 0)
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = applicationContext

        textViewRandomNumber = findViewById(R.id.textViewRandomNumber)

        buttonBindService = findViewById(R.id.buttonBindService)
        buttonUnBindService = findViewById(R.id.buttonUnBindService)
        buttonHello = findViewById(R.id.buttonHello)
        buttonScan = findViewById(R.id.buttonScan)

        buttonBindService?.setOnClickListener(this)
        buttonUnBindService?.setOnClickListener(this)
        buttonHello?.setOnClickListener(this)
        buttonScan?.setOnClickListener(this)

        serviceIntent = Intent()
        serviceIntent!!.component = ComponentName("fr.adixon.adiposrfid", "fr.adixon.adiposrfid.MyService")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonBindService -> bindToRemoteService()
            R.id.buttonUnBindService -> unbindFromRemoteService()
            R.id.buttonHello -> sendMessageToService(MSG_SAY_HELLO)
            R.id.buttonScan -> sendMessageToService(RFID_SCAN)
            else -> {}
        }
    }

    private fun bindToRemoteService() {
        if (!bound) {
            println("------------ bindToRemoteService ------------")
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
            registerReceiver(receiver, IntentFilter("fr.adixon.adiposrfid.MyService.BROADCAST_ACTION"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            };
            bound = true
            Toast.makeText(mContext, "Service bound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unbindFromRemoteService() {
        if (bound) {
            println("------------ unbindFromRemoteService ------------")
            stopService(serviceIntent)
            unregisterReceiver(receiver)
            unbindService(mConnection)
            bound = false
            Toast.makeText(mContext, "Service Unbound", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        println("--------- onStop ----------")
        // Unbind from the service
        if (bound) {
            stopService(serviceIntent)
            unregisterReceiver(receiver)
            unbindService(mConnection)
            bound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("--------- onDestroy ----------")
        // Unbind from the service
        if (bound) {
            stopService(serviceIntent)
            unregisterReceiver(receiver)
            unbindService(mConnection)
            bound = false
        }
    }
}