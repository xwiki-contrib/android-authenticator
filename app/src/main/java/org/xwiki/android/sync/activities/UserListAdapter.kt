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
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.bean.ObjectSummary

/**
 * [android.widget.Adapter] which can be used to show [SearchResult] as users.
 *
 * @version $Id: dcb077b86ade7b7121d3cb21886f740b09534fc5 $
 */

/**
 * Standard constructor.
 *
 * @param context Initial context
 * @param searchResults Initial list
 */

class UserListAdapter(private var searchResults: List<ObjectSummary>)
    : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_group, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.groupNameTextView.text = searchResults.get(position).pageName
        viewHolder.versionTextView.text = searchResults.get(position).wiki
        viewHolder.checkBox.visibility = View.INVISIBLE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val o = payloads[0] as Bundle
            for (key in o.keySet()) {
                if (key == "guid") {
                    holder.groupNameTextView.setText(searchResults.get(position).pageName)
                    holder.versionTextView.setText(searchResults.get(position).wiki)
                }
            }
        }
    }

    /**
     * Update current list of showing items.
     *
     * @param results New list
     */
    fun refresh(results: List<ObjectSummary>) {
        val diffResult = DiffUtil.calculateDiff(MyDiffUtilCallBack(results, searchResults))
        diffResult.dispatchUpdatesTo(this)
        searchResults = listOf()
        this.searchResults = results
    }

    /**
     * Help ViewHolder.
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val groupNameTextView: TextView
        val lastModifiedTime: TextView
        val versionTextView: TextView
        val checkBox: CheckBox

        init {
            groupNameTextView = itemView.findViewById(R.id.groupName)
            lastModifiedTime = itemView.findViewById(R.id.lastModifiedTime)
            versionTextView = itemView.findViewById(R.id.version)
            checkBox = itemView.findViewById(R.id.checkbox)
        }
    }

    class MyDiffUtilCallBack(internal var newList: List<ObjectSummary>, internal var oldList: List<ObjectSummary>) :
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

            if (newModel.guid !== oldModel.guid) {
                diff.putString("guid", newModel.guid)
            }
            return if (diff.size() == 0) {
                null
            } else diff
        }
    }
}
