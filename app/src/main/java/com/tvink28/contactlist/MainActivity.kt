package com.tvink28.contactlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

private const val COUNT_CONTACTS = 100

class MainActivity : AppCompatActivity(), OnContactSaveListener, OnContactCheckedChangeListener {

    private var isDeleteMode = false
    private lateinit var addContactButton: Button
    private lateinit var cancelButton: Button
    private lateinit var deleteButton: Button
    private lateinit var deleteIcon: ImageButton
    private lateinit var recyclerView: RecyclerView
    private val adapter by lazy {
        ContactAdapter(createOnContactSaveListener(), this)
    }
    private val contactListLiveData = MutableLiveData<List<Contact>>(emptyList())
    private val deleteListLiveData = MutableLiveData<List<Contact>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        addContactButton = findViewById(R.id.addContactButton)
        cancelButton = findViewById(R.id.cancelBtn)
        deleteButton = findViewById(R.id.deleteBtn)
        deleteIcon = findViewById(R.id.deleteIcon)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        contactListLiveData.observe(this) { adapter.submitList(it) }
        contactListLiveData.value = generateContacts()

        addContactButton.setOnClickListener { showAddContactDialog() }
        deleteButton.setOnClickListener { deleteContact() }
        deleteIcon.setOnClickListener { toggleDeleteMode() }
        cancelButton.setOnClickListener { toggleDeleteMode() }

        val touchHelper = createItemTouchHelper()
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onContactSaved(contact: ContactWithoutCheckBox) {
        val list = contactListLiveData.value?.toMutableList()

        contact.let {
            val existingIndex =
                list?.indexOfFirst { existingContact ->
                    existingContact is ContactWithoutCheckBox && existingContact.id == contact.id
                }
            if (existingIndex != -1) {
                existingIndex?.let { itIndex -> list.set(itIndex, it) }
            } else list.add(it)
        }
        contactListLiveData.value = list
    }

    override fun onContactCheckedChange(contact: ContactWithCheckBox, isChecked: Boolean) {
        val list = deleteListLiveData.value?.filterIsInstance<ContactWithCheckBox>()?.toMutableList()
        val existingIndex = list?.indexOfFirst { it.id == contact.id }

        existingIndex?.let {
            val updatedContact = contact.copy(isSelected = isChecked)
            list[it] = updatedContact
        }
        deleteListLiveData.value = list
    }

    private fun createOnContactSaveListener(): OnContactSaveListener {
        return object : OnContactSaveListener {
            override fun onContactSaved(contact: ContactWithoutCheckBox) {
                showEditContactDialog(contact)
            }
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(UP + DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                onItemMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        })
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int) {
        val list = contactListLiveData.value
        list?.let { Collections.swap(it, fromPosition, toPosition) }
        adapter.notifyItemMoved(fromPosition, toPosition)
        contactListLiveData.value = list
    }

    private fun generateContacts(): MutableList<Contact> {
        val faker = Faker()
        val listContact = mutableListOf<Contact>()

        for (i in 1..COUNT_CONTACTS) {
            val contact = ContactWithoutCheckBox(
                id = i,
                firstName = faker.name.firstName(),
                lastName = faker.name.lastName(),
                phoneNumber = faker.phoneNumber.phoneNumber()
            )
            listContact.add(contact)
        }
        return listContact
    }

    private fun showAddContactDialog() {
        val dialog = AddEditContactDialog(id = checkContactIds())
        dialog.setOnContactSaveListener(this)
        dialog.show(supportFragmentManager, getString(R.string.addcontactdialog))
    }

    private fun showEditContactDialog(contact: ContactWithoutCheckBox) {
        val dialog = AddEditContactDialog(true, contact)
        dialog.setOnContactSaveListener(this)
        dialog.show(supportFragmentManager, getString(R.string.editcontactdialog))
    }

    private fun deleteContact() {
        val list = contactListLiveData.value?.filterIsInstance<ContactWithCheckBox>()?.toMutableList()
        val deleteList = deleteListLiveData.value?.filterIsInstance<ContactWithCheckBox>()?.toMutableList()
        val selectedContacts = deleteList?.filter { it.isSelected }

        selectedContacts?.forEach { selectedContact ->
            list?.removeAll { it.id == selectedContact.id }
        }

        contactListLiveData.value = list
        toggleDeleteMode()
    }

    private fun toggleDeleteMode() {
        isDeleteMode = !isDeleteMode
        val list = contactListLiveData.value?.toMutableList()

        lifecycleScope.launch(Dispatchers.Default) {

            list?.forEachIndexed { index, contact ->
                val updatedContact =
                    if (isDeleteMode) contact.toContactCheck() else contact.toContactNonCheck()
                list[index] = updatedContact
            }

            withContext(Dispatchers.Main) {
                contactListLiveData.value = list

                if (!isDeleteMode) deleteListLiveData.value = emptyList()
                else deleteListLiveData.value = contactListLiveData.value

                addContactButton.visibility = if (isDeleteMode) View.GONE else View.VISIBLE
                cancelButton.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
                deleteButton.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            }
        }
    }

    private fun checkContactIds(): Int {
        val list = contactListLiveData.value?.filterIsInstance<ContactWithoutCheckBox>()
        val ids = list?.map { it.id }
        for (i in 1..(list?.size ?: 1)) {
            if (ids?.contains(i) != true) {
                return i
            }
        }
        return list?.size?.plus(1) ?: 1
    }
}