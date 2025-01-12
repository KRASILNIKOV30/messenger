package com.example.messenger

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        adapter = ChatAdapter {
            // Добавить обработчик клика на чат
        }
        binding.chatList.adapter = adapter
        binding.chatList.layoutManager = LinearLayoutManager(this.context)

        viewModel.state
            .onEach { render(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        initEventListeners()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun render(state: State) {
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
        }
        binding.addButtonInEmptyState.setOnClickListener {
            viewModel.onAddChat()
        }
    }
}