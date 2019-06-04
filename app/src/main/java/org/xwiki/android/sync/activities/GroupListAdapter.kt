/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView

import org.xwiki.android.sync.Constants
import org.xwiki.android.sync.R
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.utils.SharedPrefsUtils

import java.util.ArrayList

/**
 * [android.widget.Adapter] which can be used to show groups.
 *
 * @version $Id: 2223f93dfe9e6ead990e750141c20057143cef8f $
 */
class GroupListAdapter
/**
 * Standard constructor which save context and groups.
 *
 * @param context Context for all operations
 * @param groupList Initial group list
 */
(
        /**
         * Current context.
         */
        private val mContext: Context,
        /**
         * Current list of items.
         */
        private var groupList: List<XWikiGroup>?) : BaseAdapter() {

    /**
     * List of selected items.
     */
    private val selected = ArrayList<XWikiGroup>()

    /**
     * @return [.selected]
     */
    val selectGroups: List<XWikiGroup>
        get() = selected

    /**
     * @return Count of items
     */
    override fun getCount(): Int {
        return groupList!!.size
    }

    /**
     * @param position Position of object (must be 0 <= position < [.getCount]
     * @return Object ([XWikiGroup] if be exactly)
     */
    override fun getItem(position: Int): XWikiGroup {
        return groupList!![position]
    }

    /**
     * @param position Position of object (must be 0 <= position < [.getCount]
     * @return position
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Create and set up view.
     *
     * @param position Position of object (must be 0 <= position < [.getCount]
     * @param convertView Old [View]
     * @param parent Parent view where result will be placed
     * @return Result [View]
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = LayoutInflater.from(mContext)
            convertView = inflater.inflate(R.layout.list_item_group, null)
            viewHolder = ViewHolder(convertView!!)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val group = getItem(position)
        viewHolder.groupNameTextView.text = group.pageName
        viewHolder.lastModifiedTime.text = group.lastModifiedDate!!.substring(0, 10)
        viewHolder.versionTextView.text = group.wiki
        viewHolder.checkBox.isChecked = selected.contains(group)

        convertView.setOnClickListener {
            if (viewHolder.checkBox.isChecked) {
                viewHolder.checkBox.isChecked = false
                selected.remove(group)
            } else {
                viewHolder.checkBox.isChecked = true
                selected.add(group)
            }
        }

        viewHolder.checkBox.setOnClickListener {
            if (viewHolder.checkBox.isChecked) {
                selected.add(group)
            } else {
                selected.remove(group)
            }
        }
        return convertView
    }

    /**
     * Init groups which was selected in previous time.
     */
    private fun initSelectedGroup() {
        val groupIds = SharedPrefsUtils.getArrayList(mContext, Constants.SELECTED_GROUPS)
        if (groupIds == null || groupIds.size == 0) {
            return
        }
        selected.clear()
        for (item in groupList!!) {
            if (groupIds.contains(item.id)) {
                selected.add(item)
            }
        }
    }

    /**
     * Save current state of selected groups for future use
     */
    fun saveSelectedGroups() {
        val selectedStrings = ArrayList<String>()

        for (group in selected) {
            selectedStrings.add(group.id!!)
        }

        SharedPrefsUtils.putArrayList(mContext, Constants.SELECTED_GROUPS, selectedStrings)
    }

    /**
     * Update groups if new groups have new objects.
     *
     * @param groups new list
     */
    fun refresh(groups: List<XWikiGroup>) {
        if (groupList != null && groupList != groups) {
            groupList = groups
        }
        initSelectedGroup()
        notifyDataSetChanged()
    }

    /**
     * Help view holder class.
     */
    private class ViewHolder(view: View) {
        val groupNameTextView: TextView
        val lastModifiedTime: TextView
        val versionTextView: TextView
        val checkBox: CheckBox

        init {
            groupNameTextView = view.findViewById(R.id.groupName)
            lastModifiedTime = view.findViewById(R.id.lastModifiedTime)
            versionTextView = view.findViewById(R.id.version)
            checkBox = view.findViewById(R.id.checkbox)
        }
    }

}
