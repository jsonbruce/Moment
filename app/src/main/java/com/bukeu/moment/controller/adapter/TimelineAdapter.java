package com.bukeu.moment.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bukeu.moment.R;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.MomentList;
import com.bumptech.glide.Glide;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;

/**
 * Created by Max on 2015/4/13.
 */
public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private MomentList mMomentList;

    public TimelineAdapter(Context context, MomentList mMomentList) {
        this.mContext = context;
        this.mMomentList = mMomentList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.item_timeline, parent, false);
            return new TimelineHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            bindTimeline((TimelineHolder) holder, position);
    }

    private void bindTimeline(TimelineHolder holder, int position) {
        Moment moment = mMomentList.getMoments().get(position);

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
            }
        });

        if (moment != null) {
            holder.setDay(moment.getCreateDate());
            holder.setYearMonth(moment.getCreateDate());
            holder.setMomentText(moment.getText());
            Glide.with(mContext).load(moment.getImage())
                    .placeholder(R.drawable.default_moment)
                    .error(R.drawable.default_moment)
                    .into(holder.mMomentImageView);
        }
    }

    @Override
    public int getItemCount() {
        if (mMomentList != null) {
            return mMomentList.getMoments() == null ? 0 : mMomentList.getMoments().size();
        }
        return 0;
    }

    public static class TimelineHolder extends RecyclerView.ViewHolder {

        TextView mDayView;
        TextView mYearMonthView;

        SwipeLayout swipeLayout;

        TextView mMomentTextView;
        ImageView mMomentImageView;

        public TimelineHolder(View itemView) {
            super(itemView);

            mDayView = (TextView) itemView.findViewById(R.id.tv_time_day);
            mYearMonthView = (TextView) itemView.findViewById(R.id.tv_time_year_month);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            mMomentTextView = (TextView) itemView.findViewById(R.id.tv_card_text);
            mMomentImageView = (ImageView) itemView.findViewById(R.id.iv_card_image);
        }

        public void setDay(String date) {
            this.mDayView.setText(date.substring(8,10));
        }

        public void setYearMonth(String date) {
            this.mYearMonthView.setText(date.substring(0, 7));
        }

        public void setMomentText(String text) {
            this.mMomentTextView.setText(text);
        }

    }

}
