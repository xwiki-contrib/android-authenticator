package org.xwiki.android.sync.bean

data class InternalXWikiUserInfo(
    val wiki: String,
    val space: String,
    val pageName: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val email: String?,
    val country: String?,
    val city: String?,
    val address: String?,
    val company: String?,
    val comment: String?
)
