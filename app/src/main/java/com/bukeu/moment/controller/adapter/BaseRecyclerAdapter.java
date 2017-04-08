package com.bukeu.moment.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by Max on 2015/4/19.
 */
public abstract class BaseRecyclerAdapter<T, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    protected Context mContext;
    protected List<T> mDatas;

    public BaseRecyclerAdapter(Context context, List<T> data) {
        this.mContext = context;
        this.mDatas = mDatas;
    }

    public void addEntity(int position, T entity) {
        mDatas.add(position, entity);
        notifyItemInserted(position);
    }

    public void addEntitys(int position, List<T> entitys) {
        mDatas.addAll(position, entitys);
        notifyItemRangeInserted(position, entitys.size());
    }

    public void deleteEntity(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);
    }

    public void updateEntity(int postion) {
        notifyItemChanged(postion);
    }

    public void moveEntity(int fromPosition, int toPosition) {
        move(mDatas, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void move(List<T> data, int a, int b) {
        T temp = data.remove(a);
        data.add(b, temp);
    }

}
