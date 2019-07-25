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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.utils.GroupsListChangeListener

/**
 * [android.widget.Adapter] which can be used to show groups.
 *
 * @version $Id: 30d07c16a715a1a4929e18f6e5c1fdc7e357a9c5 $
 */

/**
 * Standard constructor which save context and groups.
 *
 * @param context Context for all operations
 * @param groupList Initial group list
 */

class GroupListAdapter(
    private var groupList: List<XWikiGroup>,
    private var groupsListChangeListener: GroupsListChangeListener
) : RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {

    /**
     * List of selected items.
     */
    private val selected = ArrayList<XWikiGroup>()

    /**
     * @return [.selected]
     */
    val selectGroups: List<XWikiGroup>
        get() = selected


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_group, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.groupNameTextView.text = groupList[position].pageName
        viewHolder.lastModifiedTime.text = groupList[position].lastModifiedDate.substring(0, 10)
        viewHolder.versionTextView.text = groupList[position].wiki
        viewHolder.checkBox.isChecked = selected.contains(groupList[position])

        viewHolder.contactContent.setOnClickListener {
            if (viewHolder.checkBox.isChecked) {
                viewHolder.checkBox.isChecked = false
                selected.remove(groupList[position])
            } else {
                viewHolder.checkBox.isChecked = true
                selected.add(groupList[position])
            }
            groupsListChangeListener.onChangeListener()
        }


        viewHolder.checkBox.setOnClickListener {
            if (viewHolder.checkBox.isChecked) {
                selected.add(groupList[position])
            } else {
                selected.remove(groupList[position])
            }
            groupsListChangeListener.onChangeListener()
        }
    }

    /**
     * Init groups which was selected in previous time.
     */
    private fun initSelectedGroup(selectedGroups: MutableList<String>?) {
        val groupIds = selectedGroups
        if (groupIds == null || groupIds.size == 0) {
            return
        }
        selected.clear()
        for (item in groupList) {
            if (groupIds.contains(item.id)) {
                selected.add(item)
            }
        }
    }

    /**
     * Save current state of selected groups for future use
     */
    fun saveSelectedGroups(): MutableList<String> {
        val selectedStrings = ArrayList<String>()

        for (group in selected) {
            selectedStrings.add(group.id)
        }

        return selectedStrings
    }

    /**
     * Update groups if new groups have new objects.
     *
     * @param groups new list
     */
    fun refresh(groups: List<XWikiGroup>, selectedGroups: MutableList<String>?) {
        val diffResult = DiffUtil.calculateDiff(GroupListDiffUtilCallBack(groups, groupList))
        diffResult.dispatchUpdatesTo(this)
        groupList = listOf()
        this.groupList = groups
        initSelectedGroup(selectedGroups)
        groupsListChangeListener.onChangeListener()
    }

    /**
     * Help view holder class.
     */
     class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val groupNameTextView: TextView
        val lastModifiedTime: TextView
        val versionTextView: TextView
        val checkBox: CheckBox
        val contactContent : RelativeLayout

        init {
            groupNameTextView = itemView.findViewById(R.id.groupName)
            lastModifiedTime = itemView.findViewById(R.id.lastModifiedTime)
            versionTextView = itemView.findViewById(R.id.version)
            checkBox = itemView.findViewById(R.id.checkbox)
            contactContent = itemView.findViewById(R.id.contact_content)
        }
    }

    class GroupListDiffUtilCallBack(internal var newList: List<XWikiGroup>, internal var oldList: List<XWikiGroup>) :
        DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return newList[newItemPosition].id.equals(oldList[oldItemPosition].id)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return newList[newItemPosition].id.equals(oldList[oldItemPosition].id)
        }

        @Nullable
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val newModel = newList[newItemPosition]
            val oldModel = oldList[oldItemPosition]

            val diff = Bundle()

            if (newModel.id !== oldModel.id) {
                diff.putString("guid", newModel.id)
            }
            return if (diff.size() == 0) {
                null
            } else diff
        }
    }
}
