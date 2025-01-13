package com.example.messenger

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.messenger.databinding.FragmentInputBinding

class InputFragment : Fragment(R.layout.fragment_input) {
    private lateinit var binding: FragmentInputBinding
    private lateinit var viewModel: MainFragmentViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInputBinding.bind(view)

        val dao = StorageApp.db.messageItemDao()
        val factory = MainFragmentViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[MainFragmentViewModel::class.java]

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