package com.example.messenger

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.databinding.FragmentChatBinding
import com.example.messenger.databinding.FragmentInputBinding
import com.example.messenger.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

class InputFragment : Fragment(R.layout.fragment_input) {
    private lateinit var binding: FragmentInputBinding
    private val viewModel by lazy {
        val provider = ViewModelProvider(owner = this)
        provider[MainFragmentViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInputBinding.bind(view)

        initEventListeners()
    }

    private fun initEventListeners() {
        binding.toolbar.setOnMenuItemClickListener {
            val ip = binding.ipInput.text.toString()
            viewModel.onCommitIp(ip)
            return@setOnMenuItemClickListener true
        }
    }
}