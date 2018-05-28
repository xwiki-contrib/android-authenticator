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
package org.xwiki.android.authenticator.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.bean.SearchResult;

import java.util.List;

public class UserListAdapter extends BaseAdapter {
    private Context mContext;
    private List<SearchResult> searchResults;

    public UserListAdapter(Context context, List<SearchResult> searchResults) {
        super();
        mContext = context;
        this.searchResults = searchResults;
    }

    public int getCount() {
        return searchResults.size();
    }

    public Object getItem(int position) {
        return searchResults.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.list_item_group, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final SearchResult item = (SearchResult) getItem(position);
        viewHolder.groupNameTextView.setText(item.pageName);
        viewHolder.lastModifiedTime.setText(item.modified.substring(0,10));
        viewHolder.versionTextView.setText(item.wiki);
        viewHolder.checkBox.setVisibility(View.INVISIBLE);
        convertView.setOnClickListener(null);

        return convertView;
    }

    public void refresh(List<SearchResult> searchs) {
        this.searchResults = searchs;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        public TextView groupNameTextView;
        public TextView lastModifiedTime;
        public TextView versionTextView;
        public CheckBox checkBox;

        public ViewHolder(View view) {
            groupNameTextView = view.findViewById(R.id.groupName);
            lastModifiedTime = view.findViewById(R.id.lastModifiedTime);
            versionTextView = view.findViewById(R.id.version);
            checkBox = view.findViewById(R.id.checkbox);
        }
    }

}
