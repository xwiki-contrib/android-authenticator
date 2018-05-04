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

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupListAdapter extends BaseAdapter {
    private Context mContext;
    private List<XWikiGroup> groupList;
    List<XWikiGroup> selected = new ArrayList<>();

    public GroupListAdapter(Context context, List<XWikiGroup> groupList) {
        super();
        mContext = context;
        this.groupList = groupList;
    }

    public int getCount() {
        return groupList.size();
    }

    public Object getItem(int position) {
        return groupList.get(position);
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

        final XWikiGroup group = (XWikiGroup) getItem(position);
        viewHolder.groupNameTextView.setText(group.pageName);
        viewHolder.lastModifiedTime.setText(group.lastModifiedDate.substring(0,10));
        viewHolder.versionTextView.setText(group.wiki);
//        viewHolder.checkBox.setChecked(mSparseBooleanArray.get(position));
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

    public void initSelectedGroup() {
        List<String> groupIds = SharedPrefsUtils.getArrayList(mContext, Constants.SELECTED_GROUPS);
        if (groupIds == null || groupIds.size() == 0) return;
        List<XWikiGroup> selectedGroups = new ArrayList<>();
        for (XWikiGroup item : groupList) {
            if (groupIds.contains(item.id)) {
                selectedGroups.add(item);
            }
        }
        this.selected = selectedGroups;
    }

    public List<XWikiGroup> getSelectGroups() {
        return selected;
    }

    public void setSelectGroups(List<XWikiGroup> groups) {
        selected = groups;
    }

    public void refresh(List<XWikiGroup> groups) {
        this.groupList = groups;
        initSelectedGroup();
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
