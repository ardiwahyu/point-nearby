package com.gitlab.pointnearby.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gitlab.pointnearby.R
import com.gitlab.pointnearby.data.remote.model.Location
import com.gitlab.pointnearby.databinding.ItemListBluePointBinding
import timber.log.Timber
import javax.inject.Inject

class ListPointAdapter @Inject constructor(): ListAdapter<Location, ListPointAdapter.ViewHolder>(
    TRANSACTION_COMPARATOR){

    private lateinit var layoutInflater: LayoutInflater
    @Inject lateinit var onItemClickHandlers: MainActivity.OnItemClickHandlers

    class ViewHolder(itemListBluePointBinding: ItemListBluePointBinding): RecyclerView.ViewHolder(itemListBluePointBinding.root){
        var binding: ItemListBluePointBinding = itemListBluePointBinding
    }

    companion object{
        private val TRANSACTION_COMPARATOR = object : DiffUtil.ItemCallback<Location>(){
            override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean =
                oldItem.name == newItem.name

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layoutInflater = LayoutInflater.from(parent.context)
        val binding: ItemListBluePointBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.item_list_blue_point, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.binding.location = getItem(position)
            holder.binding.clickHandlers = onItemClickHandlers
        }catch (e: Exception){
            Timber.e(e)
        }
    }
}