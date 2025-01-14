package com.example.messenger
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.databinding.FragmentChatBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        adapter = MessageAdapter()
        binding.messagesList.adapter = adapter
        binding.messagesList.layoutManager = LinearLayoutManager(this.context)

        val name = arguments?.getString("NAME") ?: "anonymous"
        val companionName = arguments?.getString("COMPANION_NAME") ?: "anonymous"
        val avatarUrl = arguments?.getString("AVATAR_URL") ?: DEFAULT_AVATAR_URL
        val chatId = arguments?.getString("CHAT_ID") ?: "1"

        binding.chatToolbar.title = companionName

        val dao = StorageApp.db.messageItemDao()
        val factory = ChatViewModelFactory(chatId, name, avatarUrl, dao)
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        viewModel.state
            .onEach { render(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        initEventListeners()
    }

    private fun render(state: ChatState) {
        adapter.messageList = state.messages
        adapter.notifyDataSetChanged()
        val count = adapter.itemCount
        if (count != 0) {
            binding.messagesList.smoothScrollToPosition(count - 1)
        }

        binding.chatToolbar.title = state.name
    }

    private fun initEventListeners() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            binding.messageInput.setText("")
            viewModel.sendMessage(message)
        }

        binding.chatToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}