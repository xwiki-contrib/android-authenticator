package org.xwiki.android.sync.activities.EditContact

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.EditText
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.ResponseBody
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.base.BaseActivity
import org.xwiki.android.sync.bean.MutableInternalXWikiUserInfo
import org.xwiki.android.sync.bean.XWikiUserFull
import org.xwiki.android.sync.contactdb.getContactRowId
import org.xwiki.android.sync.contactdb.getContactUserId
import org.xwiki.android.sync.contactdb.getUserInfo
import org.xwiki.android.sync.utils.StringUtils.*
import org.xwiki.android.sync.utils.extensions.TAG
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Activity for work with editing of contact
 *
 * @version: $Id$
 */
class EditContactActivity : BaseActivity() {

    private val rowId: Long? by lazy {
        getContactRowId(
            contentResolver,
            intent.data
        )
    }

    private val userId: String? by lazy {
        rowId ?.let {
            getContactUserId(
                contentResolver,
                it
            )
        }
    }

    private val splittedUserId: Array<String>? by lazy {
        userId ?.let {
            XWikiUserFull.splitId(it)
        }
    }

    private val firstNameEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactFirstNameEditText)
    }
    private val lastNameEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactLastNameEditText)
    }

    private val phoneEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactPhoneEditText)
    }
    private val emailEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactEmailEditText)
    }

    private val countryEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactCountryEditText)
    }
    private val cityEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactCityEditText)
    }
    private val addressEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactAddressEditText)
    }

    private val companyEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactCompanyEditText)
    }
    private val noteEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactNoteEditText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        Log.i(TAG, "Row id: $rowId; User id: $userId")

        findViewById<FloatingActionButton>(R.id.editContactSaveButton).setOnClickListener {
            view ->
            formDataToUserInfo() ?.also {
                Snackbar.make(view, getString(R.string.pleaseWait), Snackbar.LENGTH_INDEFINITE).show()

                AppContext.getApiManager().xwikiServicesApi.updateUser(
                    it.wiki,
                    it.space,
                    it.pageName,
                    it.firstName,
                    it.lastName,
                    it.email,
                    it.phone,
                    it.country,
                    it.city,
                    it.address,
                    it.company,
                    it.comment
                ).observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribeOn(
                    Schedulers.newThread()
                ).subscribe(
                    object : Observer<ResponseBody> {
                        override fun onError(e: Throwable?) {
                            Snackbar.make(view, e?.message ?: "Something went wrong", Snackbar.LENGTH_LONG).show()
                        }

                        override fun onNext(t: ResponseBody?) {
                            Snackbar.make(view, "Success", Snackbar.LENGTH_SHORT).show()
                        }

                        override fun onCompleted() {}
                    }
                )
            } ?:also {
                Snackbar.make(view, getString(R.string.checkErrors), Snackbar.LENGTH_SHORT).show()
            }
        }

        refillData()
    }

    private fun isCorrect(): Boolean {
        return emailEditText.let {
            (isEmpty(it.text) || isEmail(it.text)).also {
                if (!it) {
                    emailEditText.error = getString(R.string.wrongEmail)
                }
            }
        } && phoneEditText.let {
            (isEmpty(it.text) || isPhone(it.text)).also {
                if (!it) {
                    phoneEditText.error = getString(R.string.wrongPhone)
                }
            }
        }
    }

    private fun formDataToUserInfo(): MutableInternalXWikiUserInfo? {
        return if (isCorrect()) {
            splittedUserId ?.let {
                MutableInternalXWikiUserInfo(
                    it[0],
                    it[1],
                    it[2],
                    firstNameEditText.text.toString(),
                    lastNameEditText.text.toString(),
                    phoneEditText.text.toString(),
                    emailEditText.text.toString(),
                    countryEditText.text.toString(),
                    cityEditText.text.toString(),
                    addressEditText.text.toString(),
                    companyEditText.text.toString(),
                    noteEditText.text.toString()
                )
            }
        } else {
            null
        }
    }

    private fun refillData() {
        getUserInfo(
            contentResolver,
            rowId ?: return,
            splittedUserId ?: return
        ).also {
            launch (UI) {
                firstNameEditText.text.apply {
                    clear()
                    insert(0, it.firstName ?: "")
                }
                lastNameEditText.text.apply {
                    clear()
                    insert(0, it.lastName ?: "")
                }
                phoneEditText.text.apply {
                    clear()
                    insert(0, it.phone ?: "")
                }
                emailEditText.text.apply {
                    clear()
                    insert(0, it.email ?: "")
                }
                countryEditText.text.apply {
                    clear()
                    insert(0, it.country ?: "")
                }
                cityEditText.text.apply {
                    clear()
                    insert(0, it.city ?: "")
                }
                addressEditText.text.apply {
                    clear()
                    insert(0, it.address ?: "")
                }
                companyEditText.text.apply {
                    clear()
                    insert(0, it.company ?: "")
                }
                noteEditText.text.apply {
                    clear()
                    insert(0, it.comment ?: "")
                }
            }
        }
    }
}