package com.zhan_dui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhan_dui.animetaste.R;
import com.zhan_dui.modal.Category;

/**
 * Created by daimajia on 14-1-19.
 */
public class CategoryListAdapter extends BaseAdapter{

    private ArrayList<Category> mCategories;
    private Context mContext;
    private LayoutInflater mInflater;

    public CategoryListAdapter(Context context, ArrayList<Category> categories){
        mCategories = categories;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        convertView = mInflater.inflate(R.layout.category_item,null);
        TextView name = (TextView)convertView.findViewById(R.id.category_name);
        name.setText(mCategories.get(i).Name);
        convertView.setTag(mCategories.get(i));
        return convertView;
    }
}
