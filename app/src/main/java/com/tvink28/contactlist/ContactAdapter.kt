package com.tvink28.contactlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val itemClickListener: OnContactSaveListener,
    private val checkChangedListener: OnContactCheckedChangeListener
) :
    ListAdapter<Contact, RecyclerView.ViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_contact_non_checkbox -> {
                ContactWithoutCheckBoxViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_contact_non_checkbox, parent, false)
                )
            }

            R.layout.item_contact_with_checkbox -> {
                ContactWithCheckBoxViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_contact_with_checkbox, parent, false)
                )
            }

            else -> throw IllegalArgumentException("Unsupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContactWithoutCheckBoxViewHolder -> holder.bind(getItem(position) as ContactWithoutCheckBox, itemClickListener)
            is ContactWithCheckBoxViewHolder -> holder.bind(getItem(position) as ContactWithCheckBox, checkChangedListener)
            else -> throw IllegalArgumentException("Unsupported ViewHolder type: ${holder.javaClass}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ContactWithoutCheckBox -> R.layout.item_contact_non_checkbox
            is ContactWithCheckBox -> R.layout.item_contact_with_checkbox
            else -> throw IllegalArgumentException("Unsupported item type at position $position")
        }
    }

    // ViewHolder without CheckBox
    class ContactWithoutCheckBoxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val idView: TextView = itemView.findViewById(R.id.id)
        private val nameView: TextView = itemView.findViewById(R.id.name)
        private val phoneView: TextView = itemView.findViewById(R.id.phone)

        fun bind(
            item: ContactWithoutCheckBox,
            itemClickListener: OnContactSaveListener
        ) {

            val id = item.id
            val firstName = item.firstName
            val lastName = item.lastName
            val phone = item.phoneNumber

            with(itemView.context) {
                idView.text = getString(R.string.contact_id, id)
                nameView.text = getString(R.string.contact_name, firstName, lastName)
                phoneView.text = getString(R.string.contact_phone, phone)
            }

            itemView.setOnClickListener {
                item.let { itemClickListener.onContactSaved(it) }
            }
        }
    }

    // ViewHolder with CheckBox
    class ContactWithCheckBoxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val idView: TextView = itemView.findViewById(R.id.id)
        private val nameView: TextView = itemView.findViewById(R.id.name)
        private val phoneView: TextView = itemView.findViewById(R.id.phone)
        private val isCheckedView: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(
            item: ContactWithCheckBox,
            checkChangedListener: OnContactCheckedChangeListener
        ) {

            val id = item.id
            val firstName = item.firstName
            val lastName = item.lastName
            val phone = item.phoneNumber
            isCheckedView.isChecked = item.isSelected

            with(itemView.context) {
                idView.text = getString(R.string.contact_id, id)
                nameView.text = getString(R.string.contact_name, firstName, lastName)
                phoneView.text = getString(R.string.contact_phone, phone)
            }

            itemView.setOnClickListener {
                isCheckedView.isChecked = !isCheckedView.isChecked
                item.let {
                    checkChangedListener.onContactCheckedChange(item, isCheckedView.isChecked)
                }
            }

            isCheckedView.setOnCheckedChangeListener { _, isChecked ->
                item.let {
                    checkChangedListener.onContactCheckedChange(item, isChecked)
                }
            }
        }
    }

    // DiffUtil
    private class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}