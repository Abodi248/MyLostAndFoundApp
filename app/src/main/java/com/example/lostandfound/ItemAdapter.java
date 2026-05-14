package com.example.lostandfound;

import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import java.util.List;

public class ItemAdapter extends BaseAdapter {

    private final Context    context;
    private final List<Item> items;

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items   = items;
    }

    @Override public int     getCount()             { return items.size(); }
    @Override public Object  getItem(int pos)        { return items.get(pos); }
    @Override public long    getItemId(int pos)      { return items.get(pos).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_row, parent, false);
        }

        Item item = items.get(position);

        TextView tvTitle     = convertView.findViewById(R.id.tvRowTitle);
        TextView tvSubtitle  = convertView.findViewById(R.id.tvRowSubtitle);
        TextView tvTimestamp = convertView.findViewById(R.id.tvRowTimestamp);
        ImageView ivThumb    = convertView.findViewById(R.id.ivRowThumb);

        tvTitle.setText("[" + item.getPostType() + "] " + item.getCategory() + " — " + item.getName());
        tvTitle.setTextColor("Lost".equalsIgnoreCase(item.getPostType())
                ? android.graphics.Color.parseColor("#FF6B6B")
                : android.graphics.Color.parseColor("#69F0AE"));
        tvSubtitle.setText(item.getDescription());
        tvTimestamp.setText(item.getTimestamp() != null ? item.getTimestamp() : item.getDate());

        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivThumb.setImageURI(Uri.parse(item.getImagePath()));
            } catch (Exception e) {
                ivThumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ivThumb.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            ivThumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ivThumb.setImageResource(R.drawable.ic_placeholder);
        }
        ivThumb.setVisibility(View.VISIBLE);

        return convertView;
    }
}
