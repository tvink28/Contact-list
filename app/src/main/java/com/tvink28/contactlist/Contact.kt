package com.tvink28.contactlist

interface Contact {
    val id: Int
    override fun equals(other: Any?): Boolean
    fun toContactCheck(): ContactWithCheckBox
    fun toContactNonCheck(): ContactWithoutCheckBox
}

data class ContactWithCheckBox(
    override val id: Int,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val isSelected: Boolean = false
) : Contact {
    override fun toContactCheck(): ContactWithCheckBox {
        return this
    }

    override fun toContactNonCheck(): ContactWithoutCheckBox {
        return ContactWithoutCheckBox(id, firstName, lastName, phoneNumber)
    }
}

data class ContactWithoutCheckBox(
    override val id: Int,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String
) : Contact {
    override fun toContactCheck(): ContactWithCheckBox {
        return ContactWithCheckBox(id, firstName, lastName, phoneNumber)
    }

    override fun toContactNonCheck(): ContactWithoutCheckBox {
        return this
    }
}
