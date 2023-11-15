package com.tvink28.contactlist

interface OnContactCheckedChangeListener {
    fun onContactCheckedChange(contact: ContactWithCheckBox, isChecked: Boolean)
}