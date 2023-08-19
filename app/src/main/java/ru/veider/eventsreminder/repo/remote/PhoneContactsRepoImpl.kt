package ru.veider.eventsreminder.repo.remote

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.usecases.addBirthDayEventFromContactPhone
import ru.veider.eventsreminder.usecases.extractBirthday
import ru.veider.eventsreminder.usecases.getCelebrationDateForBirthDay
import java.time.LocalDate
import javax.inject.Inject

class PhoneContactsRepoImpl @Inject constructor(
    val context: Context
) : PhoneContactsRepo {
    @SuppressLint("Range")
    override fun loadBirthDayEvents(endDay: Int): List<EventData> {
        val listBirthDayEvents = arrayListOf<EventData>()
        try {
            val contentResolver: ContentResolver? =
                context.contentResolver
            contentResolver?.let {
                val cursorWithContacts: Cursor? = it.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
                )
                cursorWithContacts?.let { cursor ->
                    while (cursor.moveToNext()) {
                        val id =
                            cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        val name =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        val selection = "%s = %s and %s = '%s' and %s = '%s'".format(
                            ContactsContract.Data.RAW_CONTACT_ID,
                            id.toString(),
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
                        )
                        val cursorWithBD: Cursor? = it.query(
                            ContactsContract.Data.CONTENT_URI, null, selection, null, null
                        )
                        cursorWithBD?.let { cursorBD ->
                            while (cursorBD.moveToNext()) {
                                val birthDay =
                                    extractBirthday(
                                        cursorBD.getString(
                                            cursorBD.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Event.START_DATE
                                            )
                                        )
                                    )
                                if (getCelebrationDateForBirthDay(birthDay) <= LocalDate.now()
                                        .plusDays(endDay.toLong())
                                ) {
                                    listBirthDayEvents.add(
                                        addBirthDayEventFromContactPhone(
                                            name,
                                            birthDay,
                                            id
                                        )
                                    )
                                }
                            }
                        }
                        cursorWithBD?.close()
                    }
                }
                cursorWithContacts?.close()
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
        return listBirthDayEvents
    }


    private fun logAndToast(t: Throwable) = logAndToast(t, this::class.java.toString())

    private fun logAndToast(t: Throwable, TAG: String) {
        try {
            Log.e(TAG, "", t)
            Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show()
        } catch (_: Throwable) {
        }
    }
}