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
package org.xwiki.android.sync.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.bean.ObjectSummary;
import org.xwiki.android.sync.bean.SearchResult;

import java.util.List;

/**
 * {@link android.widget.Adapter} which can be used to show {@link SearchResult} as users.
 *
 * @version $Id$
 */
public class UserListAdapter extends BaseAdapter {

    /**
     * Context which will be used for operations.
     */
    private final Context mContext;

    /**
     * List which currently shows.
     */
    private List<ObjectSummary> searchResults;

    /**
     * Standard constructor.
     *
     * @param context Initial context
     * @param searchResults Initial list
     */
    public UserListAdapter(@NonNull Context context, @NonNull List<ObjectSummary> searchResults) {
        super();
        mContext = context;
        this.searchResults = searchResults;
    }

    /**
     * @return Size of {@link #searchResults}
     */
    public int getCount() {
        return searchResults.size();
    }

    /**
     * @param position Position of item
     * @return Item fron {@link #searchResults} by position
     */
    public ObjectSummary getItem(int position) {
        return searchResults.get(position);
    }

    /**
     * @param position Position of item
     * @return position
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Prepare and return view.
     *
     * @param position Position of item
     * @param convertView Previous {@link View}
     * @param parent Parent where view will be placed
     * @return Result view
     */
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

        final ObjectSummary item = getItem(position);
        viewHolder.groupNameTextView.setText(item.pageName);
        viewHolder.versionTextView.setText(item.wiki);
        viewHolder.checkBox.setVisibility(View.INVISIBLE);
        convertView.setOnClickListener(null);

        return convertView;
    }

    /**
     * Update current list of showing items.
     *
     * @param results New list
     */
    public void refresh(@NonNull List<ObjectSummary> results) {
        if (searchResults != null && !searchResults.equals(results)) {
            searchResults = results;
        }
        notifyDataSetChanged();
    }

    /**
     * Help ViewHolder.
     */
    private static class ViewHolder {
        public final TextView groupNameTextView;
        public final TextView lastModifiedTime;
        public final TextView versionTextView;
        public final CheckBox checkBox;

        public ViewHolder(View view) {
            groupNameTextView = view.findViewById(R.id.groupName);
            lastModifiedTime = view.findViewById(R.id.lastModifiedTime);
            versionTextView = view.findViewById(R.id.version);
            checkBox = view.findViewById(R.id.checkbox);
        }
    }

}
