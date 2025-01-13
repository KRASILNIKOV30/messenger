import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import com.example.messenger.MainActivity
import com.example.messenger.MainFragment

class WifiP2pBroadcastReceiver(private val fragment: MainFragment) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Определите, включен ли режим Wi-Fi Direct или нет
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                fragment.onWifiP2pStateChanged(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Список пиров изменился, необходимо обновить список пиров
                fragment.onPeersChanged()
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Состояние соединения изменилось
                fragment.onConnectionChanged(intent)
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Информация об этом устройстве изменилась
                fragment.onDeviceChanged(intent)
            }
        }
    }
}
