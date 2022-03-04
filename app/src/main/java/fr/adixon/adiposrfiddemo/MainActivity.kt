package fr.adixon.adiposrfiddemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var mContext: Context? = null
    private var mIsBound = false
    private var randomNumberValue = 0
    var randomNumberRequestMessenger: Messenger? = null
    var randomNumberReceiveMessenger: Messenger? = null
    private var serviceIntent: Intent? = null
    private var textViewRandomNumber: TextView? = null
    private var buttonBindService: Button? = null
    private var buttonUnBindService: Button? = null
    private var buttonGetRandomNumber: Button? = null
    private var buttonStartService: Button? = null
    private var buttonStopService: Button? = null

    internal inner class RecieveRandomNumberHandler : Handler() {
        override fun handleMessage(msg: Message) {
            randomNumberValue = 0
            when (msg.what) {
                GET_RANDOM_NUMBER_FLAG -> {
                    randomNumberValue = msg.arg1
                    textViewRandomNumber?.text = "Random Number: $randomNumberValue"
                }
                else -> {}
            }
            super.handleMessage(msg)
        }
    }

    private var randomNumberServiceConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceDisconnected(arg0: ComponentName) {
            randomNumberRequestMessenger = null
            randomNumberReceiveMessenger = null
            mIsBound = false
        }

        override fun onServiceConnected(arg0: ComponentName, binder: IBinder) {
            randomNumberRequestMessenger = Messenger(binder)
            randomNumberReceiveMessenger = Messenger(RecieveRandomNumberHandler())
            mIsBound = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = applicationContext
        textViewRandomNumber = findViewById<TextView>(R.id.textViewRandomNumber)

        buttonBindService = findViewById<Button>(R.id.buttonBindService)
        buttonUnBindService = findViewById<Button>(R.id.buttonUnBindService)
        buttonGetRandomNumber = findViewById<Button>(R.id.buttonGetRandomNumber)
        buttonStartService = findViewById<Button>(R.id.buttonStartService)
        buttonStopService = findViewById<Button>(R.id.buttonStopService)

        buttonGetRandomNumber?.setOnClickListener(this)
        buttonBindService?.setOnClickListener(this)
        buttonUnBindService?.setOnClickListener(this)
        buttonStartService?.setOnClickListener(this)
        buttonStopService?.setOnClickListener(this)

        serviceIntent = Intent()
        serviceIntent!!.component = ComponentName("fr.adixon.adiposrfid", "fr.adixon.adiposrfid.MyService")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonBindService -> bindToRemoteService()
            R.id.buttonUnBindService -> unbindFromRemoteSevice()
            R.id.buttonGetRandomNumber -> fetchRandomNumber()
            R.id.buttonStartService -> startMyService()
            R.id.buttonStopService -> stopMyService()
            else -> {}
        }
    }

    private fun bindToRemoteService() {
        randomNumberServiceConnection?.let { bindService(serviceIntent, it, BIND_AUTO_CREATE) }
        Toast.makeText(mContext, "Service bound", Toast.LENGTH_SHORT).show()
    }

    private fun unbindFromRemoteSevice() {
        if (mIsBound) {
            randomNumberServiceConnection?.let { unbindService(it) }
            mIsBound = false
            Toast.makeText(mContext, "Service Unbound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRandomNumber() {
        if (mIsBound) {
            val requestMessage = Message.obtain(null, GET_RANDOM_NUMBER_FLAG)
            requestMessage.replyTo = randomNumberReceiveMessenger
            try {
                randomNumberRequestMessenger?.send(requestMessage)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(mContext, "Service Unbound, can't get random number", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        randomNumberServiceConnection = null
    }

    private fun startMyService() {
        startService(serviceIntent);
        Toast.makeText(mContext, "Service Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopMyService() {
        stopService(serviceIntent);
        Toast.makeText(mContext, "Service Stopped", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val GET_RANDOM_NUMBER_FLAG = 0
    }
}