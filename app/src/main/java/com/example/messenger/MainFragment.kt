package com.example.messenger

import WifiP2pBroadcastReceiver
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ChatAdapter
    private val viewModel by lazy {
        val provider = ViewModelProvider(owner = this)
        provider[MainFragmentViewModel::class.java]
    }
    private val intentFilter = IntentFilter()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    private lateinit var receiver: WifiP2pBroadcastReceiver

    // Адаптер для отображения пиров в списке
    private lateinit var peerAdapter: PeerAdapter

    // Реализуем PeerListListener для обработки изменений в списке пиров
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList.toList()
        if (refreshedPeers.isEmpty()) {
            Log.d("My App", "No devices found")
        }
        if (refreshedPeers != peerAdapter.peerList) {
            peerAdapter.peerList = refreshedPeers
            peerAdapter.notifyDataSetChanged()
            binding.emptyState.isInvisible = true
            binding.peersList.isInvisible = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requestPermissionsIfNeeded()
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        adapter = ChatAdapter {
            val arguments = Bundle().apply {
                putString("CHAT_ID", it.id)
                putString("NAME", it.name)
            }

            findNavController()
                .navigate(R.id.action_mainFragment_to_chatFragment, arguments)
        }
        binding.chatList.adapter = adapter
        binding.chatList.layoutManager = LinearLayoutManager(this.context)

        peerAdapter = PeerAdapter {
            Log.i("My App", "on Peer click")
        }
        binding.peersList.adapter = adapter
        binding.peersList.layoutManager = LinearLayoutManager(this.context)

        viewModel.state
            .onEach { render(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        initEventListeners()

        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        manager = requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(requireContext(), Looper.getMainLooper(), null)
        receiver = WifiP2pBroadcastReceiver(this)
        requireContext().registerReceiver(receiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        // Создаем и регистрируем приемник
        receiver = WifiP2pBroadcastReceiver(fragment = this)
        requireContext().registerReceiver(receiver, intentFilter)
        requestPeers()
    }

    /** Unregister the BroadcastReceiver when the fragment is paused */
    override fun onPause() {
        super.onPause()
        // Отменяем регистрацию приемника
        requireContext().unregisterReceiver(receiver)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun render(state: State) {
        binding.peersList.isInvisible = true
        if (state.isChatsSelected) {
            binding.toolbar.title = "Messenger"
            binding.emptyState.isInvisible = state.chats.isNotEmpty()
            binding.chats.isInvisible = state.chats.isEmpty()
            binding.profile.isInvisible = true

            adapter.chatList = state.chats
            adapter.notifyDataSetChanged()
        }
        else {
            binding.toolbar.title = "Profile"
            binding.emptyState.isInvisible = true
            binding.chats.isInvisible = true
            binding.profile.isInvisible = false
        }
    }

    private fun initEventListeners() {
        binding.navBottom.setOnItemSelectedListener {
            viewModel.onMenuItemReselected(it)
            return@setOnItemSelectedListener true
        }
        binding.addButton.setOnClickListener {
            viewModel.onAddChat()
            startPeerDiscovery()
        }
        binding.addButtonInEmptyState.setOnClickListener {
            viewModel.onAddChat()
            startPeerDiscovery()
        }
    }

    fun onWifiP2pStateChanged(state: Boolean) {}

    fun onPeersChanged() {}

    fun onConnectionChanged(intent: Intent) {}

    fun onDeviceChanged(intent: Intent) {}

    /** Начать поиск доступных устройств через Wi-Fi Direct */
    @SuppressLint("MissingPermission")
    private fun startPeerDiscovery() {
        if (checkLocationPermission()) {
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

                override fun onSuccess() {
                    Log.d("WiFiP2P", "Peer discovery initiated successfully.")
                    requestPeers()
                }

                override fun onFailure(reasonCode: Int) {
                    // Код при неудачном запуске поиска
                    when (reasonCode) {
                        WifiP2pManager.P2P_UNSUPPORTED -> {
                            Log.e("WiFiP2P", "Wi-Fi Direct is unsupported on this device.")
                        }
                        WifiP2pManager.ERROR -> {
                            Log.e("WiFiP2P", "An error occurred while initiating discovery.")
                        }
                        else -> {
                            Log.e("WiFiP2P", "Discovery failed with reason code: $reasonCode")
                        }
                    }
                }
            })
        } else {
            requestPermissionsIfNeeded()
        }
    }

    private fun requestPeers() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("my app", "permission not accessed")
            return
        }
        manager.requestPeers(channel, peerListListener)
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 101
    private val NEARBY_WIFI_DEVICES_PERMISSION_REQUEST_CODE = 200

    private fun checkNearbyWifiDevicesPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.NEARBY_WIFI_DEVICES
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Запросить разрешение NEARBY_WIFI_DEVICES
    private fun requestNearbyWifiDevicesPermission() {
        if (!checkNearbyWifiDevicesPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES),
                NEARBY_WIFI_DEVICES_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Проверка разрешений на геолокацию
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Проверка разрешений на Bluetooth
    private fun checkBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Запросить разрешения
    private fun requestPermissionsIfNeeded() {
        if (!checkLocationPermission() || !checkBluetoothPermission() || !checkNearbyWifiDevicesPermission()) {
            val permissions = mutableListOf<String>()

            if (!checkLocationPermission()) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            if (!checkBluetoothPermission()) {
                permissions.add(Manifest.permission.BLUETOOTH)
            }

            if (!checkNearbyWifiDevicesPermission()) {
                permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }

            requestPermissions(
                permissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            requestPermissions(
                permissions.toTypedArray(),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
            requestPermissions(
                permissions.toTypedArray(),
                NEARBY_WIFI_DEVICES_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Обработчик результатов запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешения предоставлены, можно начать поиск пиров или использовать Bluetooth
                startPeerDiscovery()
            } else {
                Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}