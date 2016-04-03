package com.sethi.gurdane.usfhvz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;

import java.util.List;

/**
 * Created by Dane on 4/3/2016.
 */
public class AnnouncementAdapter extends ArrayAdapter<USFHvZ_Announcement> {
    public AnnouncementAdapter(Context context, List<USFHvZ_Announcement> announcements) {
        super(context, 0, announcements);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.announcement_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.itemTitle = (TextView) convertView.findViewById(R.id.announcement_item_title);
            holder.itemBody = (TextView) convertView.findViewById(R.id.announcement_item_body);
            convertView.setTag(holder);
        }
        final USFHvZ_Announcement announcement = (USFHvZ_Announcement) getItem(position);
        final ViewHolder holder = (ViewHolder) convertView.getTag();

        //assign announcement item views
        holder.itemTitle.setText(announcement.getTitle());
        holder.itemBody.setText(announcement.getBody());


        return convertView;
    }

    final class ViewHolder {
        public TextView itemTitle;
        public TextView itemBody;
    }
}
