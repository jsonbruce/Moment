package com.bukeu.moment.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bukeu.moment.R;
import com.bukeu.moment.model.User;
import com.bukeu.moment.model.UserList;
import com.bumptech.glide.Glide;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;

/**
 * Created by Max on 2015/4/14.
 */
public class FollowsAdapter extends RecyclerSwipeAdapter<FollowsAdapter.FollowsHolder> {

    private Context mContext;
    private boolean isFollowingView = false;
    private OnItemClickListener mOnItemClickListener;
    protected SwipeItemRecyclerMangerImpl mItemManager = new SwipeItemRecyclerMangerImpl(this);
    private UserList mUserList;

    public FollowsAdapter(Context context, UserList userList, boolean isFollowingView) {
        this.mContext = context;
        this.mUserList = userList;
        this.isFollowingView = isFollowingView;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public FollowsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_follows, parent, false);
        return new FollowsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FollowsHolder holder, final int position) {
        final User user = mUserList.getUsers().get(position);

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        holder.swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
        holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                mItemManager.closeAllExcept(layout);
            }
        });
        if (isFollowingView) {
            holder.mFollowsAction.setBackgroundResource(android.R.color.holo_green_dark);
            holder.mFollowsAction.setImageResource(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
        }
        holder.mFollowsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onFollowsActionClickListener(user.getUuid(), v, position);
                }
            }
        });

        if (user != null) {
            Glide.with(mContext)
                    .load(user.getAvater())
                    .placeholder(R.drawable.default_avater)
                    .error(R.drawable.default_avater)
                    .into(holder.mAvaterView);
            holder.mNicknameView.setText(user.getNickname());
        }

        mItemManager.bindView(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        if (mUserList != null) {
            return mUserList.getUsers() == null ? 0 : mUserList.getUsers().size();
        }
        return 0;
    }

    public void removeItem(int position) {
        if (mUserList != null && mUserList.getUsers() != null) {
            mUserList.getUsers().remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mUserList.getUsers().size());
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe;
    }

    public static class FollowsHolder extends RecyclerView.ViewHolder {

        SwipeLayout swipeLayout;
        ImageView mAvaterView;
        TextView mNicknameView;
        ImageView mFollowsAction;

        public FollowsHolder(View itemView) {
            super(itemView);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            mAvaterView = (ImageView) itemView.findViewById(R.id.iv_follows_avater);
            mNicknameView = (TextView) itemView.findViewById(R.id.tv_follows_nickname);
            mFollowsAction = (ImageView) itemView.findViewById(R.id.iv_follows_action);
        }
    }

    public interface OnItemClickListener {
        void onFollowsActionClickListener(String uuid, View view, int position);
    }
}
