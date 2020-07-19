package org.xwiki.android.sync.bean.notification

class Notification {
    var type: String? = null
    var document: String? = null

    override fun toString(): String {
        return "ObjectSummary{" +
                "type='" + type + '\''.toString() +
                ", document='" + document + '\''.toString() +
                '}'.toString()
    }
}