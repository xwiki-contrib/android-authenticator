package org.xwiki.android.sync.bean

data class MutableInternalXWikiUserInfo(
    val wiki: String,
    val space: String,
    val pageName: String,
    var firstName: String? = null,
    var lastName: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var country: String? = null,
    var city: String? = null,
    var address: String? = null,
    var company: String? = null,
    var comment: String? = null
)
