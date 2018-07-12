package org.xwiki.android.sync.activities.EditContact

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.get
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.ResponseBody
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.base.BaseActivity
import org.xwiki.android.sync.bean.MutableInternalXWikiUserInfo
import org.xwiki.android.sync.bean.XWikiUserFull
import org.xwiki.android.sync.contactdb.*
import org.xwiki.android.sync.rest.XWikiHttp
import org.xwiki.android.sync.utils.StringUtils.*
import org.xwiki.android.sync.utils.extensions.TAG
import org.xwiki.android.sync.utils.extensions.unauthorized
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

private const val reloginTryes = 3

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

    private val accountName: String? by lazy {
        rowId ?.let {
            getContactAccountName(
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

    private val addressEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactAddressEditText)
    }

    private val companyEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactCompanyEditText)
    }
    private val noteEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactNoteEditText)
    }

    private val container: ViewGroup by lazy {
        findViewById<ViewGroup>(R.id.editContactDataLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        Log.i(TAG, "Row id: $rowId; User id: $userId")

        findViewById<FloatingActionButton>(R.id.editContactSaveButton).setOnClickListener {
            view ->
            saveData(view)
        }

        refillData()
    }

    private fun saveData(view: View, count: Int = 0) {
        formDataToUserInfo() ?.also {
            if (count == 0) {
                Snackbar.make(view, getString(R.string.pleaseWait), Snackbar.LENGTH_INDEFINITE).show()
            } else {
                if (count >= reloginTryes) {
                    Snackbar.make(
                        view,
                        getString(R.string.prohibitedAction),
                        Snackbar.LENGTH_LONG
                    ).show()

                    enableContainer()
                    return
                }
            }
            disableContainer()

            AppContext.getApiManager().xwikiServicesApi.updateUser(
                it.wiki,
                it.space,
                it.pageName,
                it.firstName,
                it.lastName,
                it.email,
                it.phone,
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
                        if (e ?. unauthorized == true) {
                            XWikiHttp.relogin(
                                this@EditContactActivity,
                                accountName
                            ) ?.subscribe {
                                saveData(view, count + 1)
                            }
                        } else {
                            Snackbar.make(
                                view,
                                e?.message ?: getString(R.string.somethingWentWrong),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onNext(t: ResponseBody?) {
                        Snackbar.make(
                            view,
                            getString(R.string.success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    override fun onCompleted() {
                        Snackbar.make(
                            view,
                            getString(R.string.syncContactInfoWithServer),
                            Snackbar.LENGTH_INDEFINITE
                        ).show()

                        AppContext.getApiManager().xwikiServicesApi.getFullUserDetails(
                            it.wiki,
                            it.space,
                            it.pageName
                        ).subscribeOn(
                            Schedulers.newThread()
                        ).subscribe(
                            object : Observer<XWikiUserFull> {
                                private var synchronized: Boolean = false
                                override fun onError(e: Throwable?) {
                                    Snackbar.make(
                                        view,
                                        getString(R.string.cantSyncContact),
                                        Snackbar.LENGTH_LONG
                                    ).show()

                                    finish()
                                }

                                override fun onNext(t: XWikiUserFull?) {
                                    synchronized = t ?.let {
                                        user ->
                                        accountName ?.let {
                                            val batchOperation = BatchOperation(
                                                contentResolver
                                            )
                                            user.toContentProviderOperations(
                                                contentResolver,
                                                it
                                            ).forEach {
                                                batchOperation.add(it)
                                            }
                                            batchOperation.execute()
                                            true
                                        }
                                    } ?:let {
                                        false
                                    }
                                }

                                override fun onCompleted() {
                                    launch (UI) {
                                        if (synchronized) {
                                            Snackbar.make(
                                                view,
                                                getString(R.string.success),
                                                Snackbar.LENGTH_SHORT
                                            ).show()

                                            refillData()
                                        } else {
                                            Snackbar.make(
                                                view,
                                                getString(R.string.cantSyncContact),
                                                Snackbar.LENGTH_LONG
                                            ).show()

                                            finish()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            )
        } ?:also {
            Snackbar.make(view, getString(R.string.checkErrors), Snackbar.LENGTH_SHORT).show()
        }
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
                    null,
                    null,
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
                enableContainer()
            }
        }
    }

    private fun disableContainer() {
        launch (UI) {
            container.isEnabled = false
            (0 until container.childCount).forEach {
                container[it].isEnabled = false
            }
        }
    }

    private fun enableContainer() {
        launch (UI) {
            container.isEnabled = true
            (0 until container.childCount).forEach {
                container[it].isEnabled = true
            }
        }
    }
}