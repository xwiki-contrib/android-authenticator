package org.xwiki.android.sync.utils

private const val postfix = "@temp"
private val postfixRegex = Regex("@temp$")

val String.tempName: String
    get() = "$this$postfix"
val String.isTemp: Boolean
    get() = contains(postfixRegex)
val String.destructTempName: String
    get() {
        var current = this
        while (current.isTemp) {
            current = current.replace(postfixRegex, "")
        }
        return current
    }
