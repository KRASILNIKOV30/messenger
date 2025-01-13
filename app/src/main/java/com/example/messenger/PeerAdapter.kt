package com.example.messenger
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.databinding.PeerItemBinding

class PeerViewHolder(view: View): RecyclerView.ViewHolder(view)

class PeerAdapter(
    private val onItemClick: (WifiP2pDevice) -> Unit
): RecyclerView.Adapter<PeerViewHolder>() {
    var peerList = listOf<WifiP2pDevice>()

    override fun getItemCount(): Int = peerList.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = PeerItemBinding.inflate(inflater, parent, false)

        return PeerViewHolder(view.root)
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        val itemBinding = PeerItemBinding.bind(holder.itemView)
        val peer = peerList[position]

        itemBinding.peerName.text = peer.deviceName

        itemBinding.peerContainer.setOnClickListener {
            onItemClick(peer)
        }
    }
}