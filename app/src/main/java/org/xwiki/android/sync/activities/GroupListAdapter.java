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
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.bean.XWikiGroup;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link android.widget.Adapter} which can be used to show groups.
 *
 * @version $Id$
 */
public class GroupListAdapter extends BaseAdapter {

    /**
     * Current context.
     */
    private final Context mContext;

    /**
     * Current list of items.
     */
    private List<XWikiGroup> groupList;

    /**
     * List of selected items.
     */
    private final List<XWikiGroup> selected = new ArrayList<>();

    /**
     * Standard constructor which save context and groups.
     *
     * @param context Context for all operations
     * @param groupList Initial group list
     */
    public GroupListAdapter(@NonNull Context context, @NonNull List<XWikiGroup> groupList) {
        super();
        this.mContext = context;
        this.groupList = groupList;
    }

    /**
     * @return Count of items
     */
    public int getCount() {
        return groupList.size();
    }

    /**
     * @param position Position of object (must be 0 <= position < {@link #getCount()}
     * @return Object ({@link XWikiGroup} if be exactly)
     */
    public XWikiGroup getItem(int position) {
        return groupList.get(position);
    }

    /**
     * @param position Position of object (must be 0 <= position < {@link #getCount()}
     * @return position
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Create and set up view.
     *
     * @param position Position of object (must be 0 <= position < {@link #getCount()}
     * @param convertView Old {@link View}
     * @param parent Parent view where result will be placed
     * @return Result {@link View}
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

        final XWikiGroup group = getItem(position);
        viewHolder.groupNameTextView.setText(group.pageName);
        viewHolder.lastModifiedTime.setText(group.lastModifiedDate.substring(0,10));
        viewHolder.versionTextView.setText(group.wiki);
        viewHolder.checkBox.setChecked(selected.contains(group));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.checkBox.isChecked()) {
                    viewHolder.checkBox.setChecked(false);
                    selected.remove(group);
                } else {
                    viewHolder.checkBox.setChecked(true);
                    selected.add(group);
                }
            }
        });

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.checkBox.isChecked()) {
                    selected.add(group);
                } else {
                    selected.remove(group);
                }
            }
        });
        return convertView;
    }

    /**
     * Init groups which was selected in previous time.
     */
    private void initSelectedGroup() {
        List<String> groupIds = SharedPrefsUtils.getArrayList(mContext, Constants.SELECTED_GROUPS);
        if (groupIds == null || groupIds.size() == 0) {
            return;
        }
        selected.clear();
        for (XWikiGroup item : groupList) {
            if (groupIds.contains(item.id)) {
                selected.add(item);
            }
        }
    }

    /**
     * @return {@link #selected}
     */
    @NonNull
    public List<XWikiGroup> getSelectGroups() {
        return selected;
    }

    /**
     * Save current state of selected groups for future use
     */
    public void saveSelectedGroups() {
        List<String> selectedStrings = new ArrayList<>();

        for (XWikiGroup group : selected) {
            selectedStrings.add(group.id);
        }

        SharedPrefsUtils.putArrayList(mContext, Constants.SELECTED_GROUPS, selectedStrings);
    }

    /**
     * Update groups if new groups have new objects.
     *
     * @param groups new list
     */
    public void refresh(@NonNull List<XWikiGroup> groups) {
        if (groupList != null && !groupList.equals(groups)) {
            groupList = groups;
        }
        initSelectedGroup();
        notifyDataSetChanged();
    }

    /**
     * Help view holder class.
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
