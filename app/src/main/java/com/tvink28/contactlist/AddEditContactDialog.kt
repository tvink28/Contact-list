package com.tvink28.contactlist

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout

class AddEditContactDialog(
    private val isEdit: Boolean = false,
    private val contact: ContactWithoutCheckBox? = null,
    private val id: Int = 1
) : DialogFragment(), TextWatcher {

    private var onContactSaveListener: OnContactSaveListener? = null

    private lateinit var firstNameInputLayout: TextInputLayout
    private lateinit var lastNameInputLayout: TextInputLayout
    private lateinit var phoneInputLayout: TextInputLayout

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneEditText: EditText

    private lateinit var firstNameText: String
    private lateinit var lastNameText: String
    private lateinit var phoneNumberText: String

    fun setOnContactSaveListener(listener: OnContactSaveListener) {
        this.onContactSaveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_contact, null)

        view.apply {
            firstNameEditText = findViewById(R.id.inputEditFirstName)
            lastNameEditText = findViewById(R.id.inputEditLastName)
            phoneEditText = findViewById(R.id.inputEditPhone)
            firstNameInputLayout = findViewById(R.id.inputLayoutFirstName)
            lastNameInputLayout = findViewById(R.id.inputLayoutLastName)
            phoneInputLayout = findViewById(R.id.inputLayoutPhone)
        }

        if (isEdit) {
            firstNameEditText.setText(contact?.firstName)
            lastNameEditText.setText(contact?.lastName)
            phoneEditText.setText(contact?.phoneNumber)
        }

        firstNameEditText.addTextChangedListener(this)
        lastNameEditText.addTextChangedListener(this)
        phoneEditText.addTextChangedListener(this)

        builder.setView(view)
            .setTitle(if (isEdit) R.string.edit_contact else R.string.add_contact)
            .setPositiveButton(getString(R.string.save)) { _, _ ->

                firstNameText = firstNameEditText.text.toString()
                lastNameText = lastNameEditText.text.toString()
                phoneNumberText = phoneEditText.text.toString()

                if (isNotEmpty()) {
                    val newContact = if (!isEdit) {
                        ContactWithoutCheckBox(id, firstNameText, lastNameText, phoneNumberText)
                    } else {
                        contact?.let {
                            ContactWithoutCheckBox(it.id, firstNameText, lastNameText, phoneNumberText)
                        }
                    }
                    newContact?.let { onContactSaveListener?.onContactSaved(it) }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
        return builder.create()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        firstNameText = firstNameEditText.text.toString()
        lastNameText = lastNameEditText.text.toString()
        phoneNumberText = phoneEditText.text.toString()

        when (s) {
            firstNameEditText.text -> showError(firstNameEditText, firstNameInputLayout, R.string.error_first_name)
            lastNameEditText.text -> showError(lastNameEditText, lastNameInputLayout, R.string.error_last_name)
            phoneEditText.text -> showError(phoneEditText, phoneInputLayout, R.string.error_phone_number)
        }
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isNotEmpty()
    }

    override fun afterTextChanged(s: Editable?) {}

    private fun showError(editText: EditText, inputLayout: TextInputLayout, errorMessageResId: Int) {
        inputLayout.error =
            editText.text.toString().takeIf { it.isBlank() }?.let { getString(errorMessageResId) }
    }

    private fun isNotEmpty() =
        firstNameText.isNotEmpty() && lastNameText.isNotEmpty() && phoneNumberText.isNotEmpty()
}