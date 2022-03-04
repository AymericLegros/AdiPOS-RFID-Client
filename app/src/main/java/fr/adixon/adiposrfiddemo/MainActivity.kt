package fr.adixon.adiposrfiddemo

import android.content.*
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var mContext: Context? = null
    private var bound = false
    private var serviceIntent: Intent? = null
    private var textViewRandomNumber: TextView? = null
    private var buttonBindService: Button? = null
    private var buttonUnBindService: Button? = null

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
            bound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            println("----------- onServiceDisconnected -----------")
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = applicationContext
        textViewRandomNumber = findViewById<TextView>(R.id.textViewRandomNumber)

        buttonBindService = findViewById<Button>(R.id.buttonBindService)
        buttonUnBindService = findViewById<Button>(R.id.buttonUnBindService)

        buttonBindService?.setOnClickListener(this)
        buttonUnBindService?.setOnClickListener(this)

        serviceIntent = Intent()
        serviceIntent!!.component = ComponentName("fr.adixon.adiposrfid", "fr.adixon.adiposrfid.MyService")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonBindService -> bindToRemoteService()
            R.id.buttonUnBindService -> unbindFromRemoteSevice()
            else -> {}
        }
    }

    private fun bindToRemoteService() {
        if (!bound) {
            println("------------ bindToRemoteService ------------")
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
            registerReceiver(receiver, IntentFilter("fr.adixon.adiposrfid.MyService.BROADCAST_ACTION"));
            startService(serviceIntent);
            bound = true
            Toast.makeText(mContext, "Service bound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unbindFromRemoteSevice() {
        if (bound) {
            println("------------ unbindFromRemoteSevice ------------")
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