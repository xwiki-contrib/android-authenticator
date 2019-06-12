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
import org.xwiki.android.sync.R
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.SearchResult

/**
 * [android.widget.Adapter] which can be used to show [SearchResult] as users.
 *
 * @version $Id: dcb077b86ade7b7121d3cb21886f740b09534fc5 $
 */
class UserListAdapter
/**
 * Standard constructor.
 *
 * @param context Initial context
 * @param searchResults Initial list
 */
    (private val mContext: Context, private var searchResults: List<ObjectSummary>) : BaseAdapter() {

    /**
     * @return Size of [.searchResults]
     */
    override fun getCount(): Int {
        return searchResults.size
    }

    /**
     * @param position Position of item
     * @return Item fron [.searchResults] by position
     */
    override fun getItem(position: Int): ObjectSummary {
        return searchResults[position]
    }

    /**
     * @param position Position of item
     * @return position
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Prepare and return view.
     *
     * @param position Position of item
     * @param convertView Previous [View]
     * @param parent Parent where view will be placed
     * @return Result view
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

        val item = getItem(position)
        viewHolder.groupNameTextView.text = item.pageName
        viewHolder.versionTextView.text = item.wiki
        viewHolder.checkBox.visibility = View.INVISIBLE
        convertView.setOnClickListener(null)

        return convertView
    }

    /**
     * Update current list of showing items.
     *
     * @param results New list
     */
    fun refresh(results: List<ObjectSummary>) {
        if (searchResults != null && searchResults != results) {
            searchResults = results
        }
        notifyDataSetChanged()
    }

    /**
     * Help ViewHolder.
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
