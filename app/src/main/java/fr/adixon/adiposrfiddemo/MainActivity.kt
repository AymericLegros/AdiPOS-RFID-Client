package fr.adixon.adiposrfiddemo

import android.content.*
import android.os.*
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


private const val RFID_HELLO = 0
private const val RFID_INIT = 1
private const val RFID_TERMINATE = 2
private const val RFID_CONNECTOR_STATUS = 3
private const val RFID_SCAN_START = 4
private const val RFID_SCAN_STOP = 5

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var adapter: RFIDTagAdapter

    private var mContext: Context? = null
    private var mService: Messenger? = null
    private var bound = false
    private var serviceIntent: Intent? = null

    private var buttonHello: Button? = null
    private var buttonStartScan: Button? = null
    private var buttonStopScan: Button? = null

    private var tags: ArrayList<String> = arrayListOf();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = applicationContext

        adapter = RFIDTagAdapter(tags)
        val recyclerView = findViewById<RecyclerView>(R.id.tag_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        buttonHello = findViewById(R.id.buttonHello)
        buttonStartScan = findViewById(R.id.buttonStartScan)
        buttonStopScan = findViewById(R.id.buttonStopScan)

        buttonHello?.setOnClickListener(this)
        buttonStartScan?.setOnClickListener(this)
        buttonStopScan?.setOnClickListener(this)

        serviceIntent = Intent()
        serviceIntent!!.component = ComponentName("fr.adixon.adiposrfid", "fr.adixon.adiposrfid.RFIDService")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonStartScan -> bindToRemoteService()
            R.id.buttonHello -> sendMessageToService(RFID_HELLO)
            R.id.buttonStopScan -> unbindFromRemoteService()
            else -> {}
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getStringArrayListExtra("fr.adixon.adiposrfid.AllTags")
            tags.clear();
            val newTags = data as ArrayList<String>
            tags.addAll(newTags)
            adapter.notifyDataSetChanged();
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

    private fun bindToRemoteService() {
        if (!bound) {
            println("------------ bindToRemoteService ------------")
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
            registerReceiver(receiver, IntentFilter("fr.adixon.adiposrfid.RFIDService.BROADCAST_ACTION"))
        }
    }

    private fun unbindFromRemoteService() {
        if (bound) {
            println("------------ unbindFromRemoteService ------------")
            unregisterReceiver(receiver)
            unbindService(mConnection)
            bound = false
        }
    }
}