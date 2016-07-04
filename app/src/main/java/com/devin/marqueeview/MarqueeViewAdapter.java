package com.devin.marqueeview;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @param <T>
 */
public abstract class MarqueeViewAdapter<T> {

    private List<T> mDatas;
    private OnDataChangeListener listener;

    public MarqueeViewAdapter(List<T> datas) {
        mDatas = datas;
    }

    public MarqueeViewAdapter(T[] datas) {
        mDatas = new ArrayList<>(Arrays.asList(datas));
    }

    public void notifyDataSetChanged() {

        if (listener != null) {
            listener.dataChange();
        }

    }

    void setOnDataChangeListener(OnDataChangeListener listener) {

        this.listener = listener;

    }

    interface OnDataChangeListener {

        void dataChange();
    }

    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    public abstract View getView(MarqueeView parent, int position, T t);

}
