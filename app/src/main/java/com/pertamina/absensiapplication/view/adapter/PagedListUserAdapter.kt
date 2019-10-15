package com.pertamina.absensiapplication.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.ItemUserBinding
import com.pertamina.absensiapplication.model.User

class PagedListUserAdapter(private var parentFragment: String = "", private val callback: OnClickListener) :
        PagedListAdapter<User, PagedListUserAdapter.ViewHolder>(
                diffCallback
        ) {
    fun setParentFragment(parentFragment: String) {
        this.parentFragment = parentFragment
    }

    private var networkState: NetworkState? = null

    interface OnClickListener {
        fun whenListIsUpdated(size: Int, networkState: NetworkState?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<ItemUserBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_user,
                parent,
                false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position), parentFragment)
    }

    override fun getItemCount(): Int {
        this.callback.whenListIsUpdated(super.getItemCount(), this.networkState)
        return super.getItemCount()
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.SUCCESS

    fun updateNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
        }
    }

    class ViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(user: User?, parentFragment: String) {
            binding.fragment = parentFragment
            binding.user = user
            if (user != null) {
                if (user.standIn != null) {
                    if (user.standIn.size == 1) {
                        setPosition(binding.pjs, user.standIn[0])
                    } else if (user.standIn.size == 2) {
                        setPosition(binding.pjs, user.standIn[0])
                        setPosition(binding.pjs2, user.standIn[1])
                    }
                }
            }
        }

        private fun setPosition(view: TextView, uid: String) {
            Firebase.firestore.document("branch/f303/users/$uid").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val user: User? = it.result?.toObject()
                    var result = when (user?.position) {
                        "Operation Head TBBM Malang" -> "PJS OH"
                        "Spv Receiving, Storage & Distribution" -> "PJS RSD"
                        "Spv Maintanance Planning & Service" -> "PJS MPS"
                        "Spv Sales Services & General Affair" -> "PJS SSGA"
                        "Spv Quality & Quantity" -> "PJS QQ"
                        "Jr Spv HSSE" -> "PJS HSSE"
                        else -> "PJS"
                    }
                    view.text = result
                }
            }
        }
    }
}