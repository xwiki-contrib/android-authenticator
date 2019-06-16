package org.xwiki.android.sync.activities.EditContact

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.get
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.base.BaseActivity
import org.xwiki.android.sync.apiManager
import org.xwiki.android.sync.bean.MutableInternalXWikiUserInfo
import org.xwiki.android.sync.bean.XWikiUserFull
import org.xwiki.android.sync.contactdb.*
import org.xwiki.android.sync.rest.XWikiHttp
import org.xwiki.android.sync.utils.StringUtils.isEmail
import org.xwiki.android.sync.utils.StringUtils.isEmpty
import org.xwiki.android.sync.utils.StringUtils.isPhone
import org.xwiki.android.sync.utils.extensions.unauthorized
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

private const val reloginTryes = 3

/**
 * Activity for work with editing of contact
 *
 * @version: $Id$
 *
 * @since 0.5
 */
class EditContactActivity : BaseActivity() {

    /**
     * The scope of edit contact activity
     *
     * @since 0.6
     */
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Lazy initialized contact row id
     *
     * @see getContactRowId
     */
    private val rowId: Long? by lazy {
        intent.data ?.let {
            getContactRowId(
                contentResolver,
                it
            )
        }
    }

    /**
     * Lazy initialized contact user id
     *
     * @see getContactUserId
     * @see XWikiUserFull.id
     */
    private val userId: String? by lazy {
        rowId ?.let {
            getContactUserId(
                contentResolver,
                it
            )
        }
    }

    /**
     * Lazy initialized contact account name
     *
     * @see getContactAccountName
     */
    private val accountName: String? by lazy {
        rowId ?.let {
            getContactAccountName(
                contentResolver,
                it
            )
        }
    }

    /**
     * Lazy initialized splitted {@link #userId}
     */
    private val splittedUserId: Array<String>? by lazy {
        userId ?.let {
            XWikiUserFull.splitId(it)
        }
    }

    /**
     * Lazy initialized EditText which contains first name
     */
    private val firstNameEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactFirstNameEditText)
    }

    /**
     * Lazy initialized EditText which contains last name
     */
    private val lastNameEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactLastNameEditText)
    }

    /**
     * Lazy initialized EditText which contains phone
     */
    private val phoneEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactPhoneEditText)
    }

    /**
     * Lazy initialized EditText which contains email
     */
    private val emailEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactEmailEditText)
    }

    /**
     * Lazy initialized EditText which contains address
     */
    private val addressEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactAddressEditText)
    }

    /**
     * Lazy initialized EditText which contains company name
     */
    private val companyEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactCompanyEditText)
    }

    /**
     * Lazy initialized EditText which contains note text
     */
    private val noteEditText: EditText by lazy {
        findViewById<EditText>(R.id.editContactNoteEditText)
    }

    /**
     * Lazy initialized EditText which contains all edit texts
     */
    private val container: ViewGroup by lazy {
        findViewById<ViewGroup>(R.id.editContactDataLayout)
    }

    /**
     * Base initialization of fields and callbacks. Here will be added callback to
     * floating action button and called {@link #refillData}.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        findViewById<FloatingActionButton>(R.id.editContactSaveButton).setOnClickListener {
            view ->
            saveData(view)
        }

        refillData()
    }

    /**
     * Init save data from form into server and local database. Check form data, send to server,
     * resync contact data and refill form. Will be recalled if need authorization. If will
     * be called more than {@link #reloginTryes} times - think that user have no permissions
     * to edit this contact
     *
     * @param view View to show snackbar
     * @param count Count of tryes, analog of ttl in network
     */
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

            apiManager.xwikiServicesApi.updateUser(
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
                {
                    Snackbar.make(
                        view,
                        getString(R.string.success),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    it ?.also {
                        user ->
                        updateUserInDatabase(user)

                        scope.launch (Dispatchers.Main) {
                            Snackbar.make(
                                view,
                                getString(R.string.success),
                                Snackbar.LENGTH_SHORT
                            ).show()

                            refillData()
                        }
                    } ?:also {
                        manuallyUpdateUserInfo(view)
                    }
                }
            ) {
                if (it?.unauthorized == true) {
                    XWikiHttp.relogin(
                        this@EditContactActivity,
                        accountName
                    )?.subscribe(
                        {
                            saveData(view, count + 1)
                        }
                    ) {
                        Snackbar.make(
                            view,
                            it?.message ?: getString(R.string.authenticationError),
                            Snackbar.LENGTH_LONG
                        ).show()
                        enableContainer()
                    }
                } else {
                    Snackbar.make(
                        view,
                        it?.message ?: getString(R.string.somethingWentWrong),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } ?:also {
            Snackbar.make(view, getString(R.string.checkErrors), Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * @return true if form is correct and data can be saved, false otherwise
     */
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

    /**
     * @return Filled object from form or null if form is incorrect
     *
     * @see EditContactActivity.isCorrect
     */
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

    /**
     * Update form data from database and enable container
     */
    private fun refillData() {
        getUserInfo(
            contentResolver,
            rowId ?: return,
            splittedUserId ?: return
        ).also {
            scope.launch (Dispatchers.Main) {
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

    /**
     * Disable all possible to add data (prohibit input)
     */
    private fun disableContainer() {
        scope.launch (Dispatchers.Main) {
            container.isEnabled = false
            (0 until container.childCount).forEach {
                container[it].isEnabled = false
            }
        }
    }

    /**
     * Enable all possible to add data (permit input)
     */
    private fun enableContainer() {
        scope.launch (Dispatchers.Main) {
            container.isEnabled = true
            (0 until container.childCount).forEach {
                container[it].isEnabled = true
            }
        }
    }

    /**
     * Update user info in database using user
     *
     * @param user Will be used as data source for updating
     */
    private fun updateUserInDatabase(user: XWikiUserFull) {
        rowId ?.also {
            val batchOperation = BatchOperation(
                contentResolver
            )
            user.toContentProviderOperations(
                it
            ).forEach {
                batchOperation.add(it)
            }
            batchOperation.execute()
        }
    }

    /**
     * Manually get user info from server and update in database
     *
     * @see updateUserInDatabase
     */
    private fun manuallyUpdateUserInfo(view: View) {
        scope.launch (Dispatchers.Main) {
            Snackbar.make(
                view,
                getString(R.string.syncContactInfoWithServer),
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }
        splittedUserId ?.let {
            apiManager.xwikiServicesApi.getFullUserDetails(
                it[0],
                it[1],
                it[2]
            ).subscribeOn(
                Schedulers.newThread()
            ).subscribe(
                object : Observer<XWikiUserFull> {
                    override fun onError(e: Throwable?) {
                        scope.launch (Dispatchers.Main) {
                            Snackbar.make(
                                view,
                                e ?. message ?: getString(R.string.cantSyncContact),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onNext(t: XWikiUserFull?) {
                        t ?.let {
                            updateUserInDatabase(it)

                            scope.launch (Dispatchers.Main) {
                                Snackbar.make(
                                    view,
                                    getString(R.string.success),
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                refillData()
                            }
                        } ?:let {
                            onError(null)
                        }
                    }

                    override fun onCompleted() {}
                }
            )
        }
    }
}