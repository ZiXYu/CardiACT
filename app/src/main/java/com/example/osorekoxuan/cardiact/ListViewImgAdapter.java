package com.example.osorekoxuan.cardiact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by osorekoxuan on 16/3/11.
 */
public class ListViewImgAdapter extends ArrayAdapter<ListViewItem> {
    private Context context;
    private List<ListViewItem> ListViewItems;

    public ListViewImgAdapter(Context context, List<ListViewItem> ListViewItems) {
        super(context, R.layout.listview_img_item, ListViewItems);
        this.context = context;
        this.ListViewItems = ListViewItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listview_img_item, parent, false);
        TextView infoTitle = (TextView) rowView.findViewById(R.id.info_title);
        infoTitle.setText(ListViewItems.get(position).Title);
        return rowView;
    }
}
