package edu.uwp.appfactory.rusd.ui.publications

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.model.Publication
import edu.uwp.appfactory.rusd.databinding.ListItemPublicationBinding

/**
 * Created by Marshall on 6/26/2017.
 * Edited by Jeremiah on 7/21/2017.
 */
class PublicationsRecyclerViewAdapter(var publications: List<Publication>) : RecyclerView.Adapter<PublicationsRecyclerViewAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(publications[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemPublicationBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.list_item_publication, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return publications.size
    }

    class ViewHolder(private val binding: ListItemPublicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(publication: Publication) {
            binding.setVariable(BR.publication, publication)
            binding.executePendingBindings()
        }
    }

    fun setPublicationList(publications: List<Publication>) {
        this.publications = publications
        notifyDataSetChanged()
    }

}