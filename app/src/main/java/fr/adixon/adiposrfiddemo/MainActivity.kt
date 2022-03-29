package fr.adixon.adiposrfiddemo

import android.content.*
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.adixon.adiposrfid.RFIDTag


private const val MSG_SAY_HELLO = 0
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

    // private var listView: ListView? = null
    private var buttonBindService: Button? = null
    private var buttonInitService: Button? = null
    private var buttonTerminateService: Button? = null
    private var buttonUnBindService: Button? = null
    private var buttonHello: Button? = null
    private var buttonScanStart: Button? = null
    private var buttonScanStop: Button? = null

    private var tags: ArrayList<RFIDTag> = arrayListOf();
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

        buttonBindService = findViewById(R.id.buttonBindService)
        buttonInitService = findViewById(R.id.buttonInitService)
        buttonTerminateService = findViewById(R.id.buttonTerminateService)
        buttonUnBindService = findViewById(R.id.buttonUnBindService)
        buttonHello = findViewById(R.id.buttonHello)
        buttonScanStart = findViewById(R.id.buttonScanStart)
        buttonScanStop = findViewById(R.id.buttonScanStop)

        buttonBindService?.setOnClickListener(this)
        buttonInitService?.setOnClickListener(this)
        buttonTerminateService?.setOnClickListener(this)
        buttonUnBindService?.setOnClickListener(this)
        buttonHello?.setOnClickListener(this)
        buttonScanStart?.setOnClickListener(this)
        buttonScanStop?.setOnClickListener(this)

        serviceIntent = Intent()
        serviceIntent!!.component = ComponentName("fr.adixon.adiposrfid", "fr.adixon.adiposrfid.RFIDService")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonBindService -> bindToRemoteService()
            R.id.buttonInitService -> sendMessageToService(RFID_INIT)
            R.id.buttonHello -> sendMessageToService(MSG_SAY_HELLO)
            R.id.buttonScanStart -> sendMessageToService(RFID_SCAN_START)
            R.id.buttonScanStop -> sendMessageToService(RFID_SCAN_STOP)
            R.id.buttonTerminateService -> sendMessageToService(RFID_TERMINATE)
            R.id.buttonUnBindService -> unbindFromRemoteService()
            else -> {}
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getParcelableArrayListExtra<RFIDTag>("fr.adixon.adiposrfid.AllTags")
            println(data);
            tags.clear();

            val newTags = data as ArrayList<RFIDTag>
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
            registerReceiver(receiver, IntentFilter("fr.adixon.adiposrfid.RFIDService.BROADCAST_ACTION"));
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