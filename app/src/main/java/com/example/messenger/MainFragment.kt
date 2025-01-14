package com.example.messenger
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.core.content.ContextCompat
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
    private lateinit var viewModel: MainFragmentViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requestPermissionsIfNeeded()
        super.onViewCreated(view, savedInstanceState)

        val dao = StorageApp.db.messageItemDao()
        val factory = MainFragmentViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[MainFragmentViewModel::class.java]

        binding = FragmentMainBinding.bind(view)
        adapter = ChatAdapter {
            goToChat(it)
        }
        binding.chatList.adapter = adapter
        binding.chatList.layoutManager = LinearLayoutManager(this.context)

        viewModel.state
            .onEach { render(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.event
            .onEach { handleEvents(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        initEventListeners()

        // TODO: сохранить ip во viewModel
        val wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectionInfo = wifiManager.dhcpInfo
        val ipAddress = connectionInfo.ipAddress
        val ip = Formatter.formatIpAddress(ipAddress)
        binding.profileIp.text = ip.toString()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun render(state: State) {
        binding.mainContainer.isInvisible = state.isInput
        binding.inputContainer.isInvisible = !state.isInput

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
            viewModel.onAddCompanion()
        }
        binding.addButtonInEmptyState.setOnClickListener {
            viewModel.onAddCompanion()
        }
        binding.toolbarInput.setOnMenuItemClickListener {
            val ip = binding.ipInput.text.toString()
            viewModel.onCommitIp(ip)
            return@setOnMenuItemClickListener true
        }
        binding.toolbarInput.setNavigationOnClickListener {
            viewModel.onCloseInput()
        }
    }

    private fun handleEvents(event: Event) {
        when (event) {
            is Event.GoToChat -> {
                goToChat(event.chat)
            }
        }
    }

    private fun goToChat(chat: ChatItem) {
        val arguments = Bundle().apply {
            putString("CHAT_ID", chat.id)
            putString("NAME", viewModel.state.value.name)
            putString("AVATAR_URL", viewModel.state.value.avatarUrl)
            putString("COMPANION_NAME", chat.name)
        }

        findNavController()
            .navigate(R.id.action_mainFragment_to_chatFragment, arguments)
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

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
    }

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
}