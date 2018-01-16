package edu.uwp.appfactory.rusd.ui.support

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.model.Contact
import edu.uwp.appfactory.rusd.databinding.ListItemSupportBinding

/**
 * Created by Marshall on 6/28/2017.
 *
 * Edited by Jeremiah on 7/21/2017.
 */

class SupportRecyclerViewAdapter(var contacts: List<Contact>):
        RecyclerView.Adapter<SupportRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemSupportBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.list_item_support, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun setContactsList(contacts: List<Contact>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ListItemSupportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.setVariable(BR.contact, contact)
            binding.executePendingBindings()
        }
    }
}