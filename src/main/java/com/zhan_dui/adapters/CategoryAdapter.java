package com.zhan_dui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import com.zhan_dui.fragments.CategoryFragment;
import com.zhan_dui.modal.Category;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daimajia on 14-2-4.
 */
public class CategoryAdapter extends FragmentPagerAdapter{
    private ArrayList<Category> mCategories;
    private HashMap<Integer,CategoryFragment> mFragmentsHashMap;

    public CategoryAdapter(ArrayList<Category> categories,FragmentManager fm) {
        super(fm);
        mCategories = categories;
        mFragmentsHashMap = new HashMap<Integer, CategoryFragment>();
    }

    @Override
    public Fragment getItem(int i) {
        if(!mFragmentsHashMap.containsKey(i)){
            mFragmentsHashMap.put(i,CategoryFragment.newInstance(mCategories.get(i)));
        }
        return mFragmentsHashMap.get(i);
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mCategories.get(position).Name;
    }
}
