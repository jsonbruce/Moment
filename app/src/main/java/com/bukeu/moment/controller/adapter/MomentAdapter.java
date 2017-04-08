package com.bukeu.moment.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.util.UIHelper;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 2015/4/19.
 */
public class MomentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        View.OnClickListener{

    private static final int VIEW_TYPE_ITEM = 1;

    private Context mContext;
    private List<Moment> mMoments = new ArrayList<>();
    private List<Long> mLikedMomentId = new ArrayList<>();

    private OnMomentInteractionListener mListener;

    public MomentAdapter(Context context, List<Moment> ms) {
        this.mContext = context;
        this.mMoments = ms;

        mLikedMomentId = MomentApplication.getContext().getLikedMomentId();
    }

    public void setOnMomentInteractionListener(OnMomentInteractionListener listener) {
        this.mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (VIEW_TYPE_ITEM == viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_moment, parent, false);
            MomentHolder momentHolder = new MomentHolder(v);

            momentHolder.mLikeButton.setOnClickListener(this);

            return momentHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindMomentItemHolder((MomentHolder) holder, position);
    }

    private void bindMomentItemHolder(final MomentHolder holder, int position) {
        final Moment moment = mMoments.get(position);
        holder.setContext(mContext);

        holder.mLikeButton.setTag(holder);

        holder.setMomentText(moment.getText());
        holder.setNickname(moment.getNickname());

        if (moment.getLikesCount() != null) {
            holder.setLikesCount(mContext.getResources().getQuantityString(
                    R.plurals.likes_count, moment.getLikesCount().intValue(), moment.getLikesCount()));
        }

        if ((mLikedMomentId != null && mLikedMomentId.contains(moment.getId()))) {
            holder.like();
        } else if ((mLikedMomentId != null && !mLikedMomentId.contains(moment.getId()))){
            holder.unlike();
        }

        Glide.with(mContext).load(moment.getAvater())
                .placeholder(R.drawable.default_avater)
                .error(R.drawable.default_avater)
                .into(holder.mAvaterView);
        Glide.with(mContext).load(moment.getImage())
                .placeholder(R.drawable.default_moment)
                .error(R.drawable.default_moment)
                .into(holder.mMomentImageView);

        holder.mMomentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onMomentClick(holder, moment);
                }
            }
        });
        holder.mAvaterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onMomentClick(holder, moment);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mMoments == null ? 0 : mMoments.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ITEM;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_card_like:
                if (MomentApplication.getContext().getUser() == null) {
                    UIHelper.startSigninActivity(mContext);
                } else {
                    MomentHolder holder = (MomentHolder) view.getTag();
                    Moment moment = mMoments.get(holder.getLayoutPosition());

                    updateLikeButton(holder);
                    updateLikesCount(holder, true);
                    MomentApplication.getContext().addLikeOperation(moment.getId());
                }
                break;
        }
    }

    private void updateLikeButton(final MomentHolder holder) {
        if (holder.isLiked) {
            holder.unlike();
            mLikedMomentId.remove(mMoments.get(holder.getLayoutPosition()).getId());
        } else {
            holder.like();
            mLikedMomentId.add(mMoments.get(holder.getLayoutPosition()).getId());
        }
    }

    private void updateLikesCount(MomentHolder holder, boolean animated) {
        Long likescount = mMoments.get(holder.getPosition()).getLikesCount();
        int currentLikesCount = 0;
        if (holder.isLiked) {
            currentLikesCount = likescount == null ? 0 : likescount.intValue() + 1;
        } else {
            currentLikesCount = likescount == null ? 0 : likescount.intValue() - 1;
            currentLikesCount = currentLikesCount < 0 ? 0 : currentLikesCount;
        }
        mMoments.get(holder.getPosition()).setLikesCount((long) currentLikesCount);
        String likesCountText = mContext.getResources().getQuantityString(
                R.plurals.likes_count, currentLikesCount, currentLikesCount
        );

        if (animated) {
            holder.tsLikesCounter.setText(likesCountText);
        } else {
            holder.tsLikesCounter.setCurrentText(likesCountText);
        }
    }


    public void updateMoments(List<Moment> moments) {
        this.mMoments = moments;
        notifyDataSetChanged();
    }

    public void addNewMoment(Moment moment) {
        this.mMoments.add(0, moment);
        notifyItemChanged(0);
    }

    public void loadOldMoments(List<Moment> moments) {
        int positionStart = mMoments.size();
        this.mMoments.addAll(moments);
        notifyItemRangeInserted(positionStart, moments.size());
    }

    public void insertMoments(List<Moment> moments, int position) {
        this.mMoments.addAll(position, moments);
        notifyItemRangeInserted(position, moments.size());
    }


    public void syncLikeButton(MomentHolder holder) {
        onClick(holder.mLikeButton);
    }

    public static class MomentHolder extends RecyclerView.ViewHolder {

        private Context mContext;

        public ImageView mAvaterView;
        public TextView mNicknameView;
        public ImageButton mLikeButton;
        public TextView mLikesCountView;
        public TextView mMomentTextView;
        public ImageView mMomentImageView;

        TextSwitcher tsLikesCounter;

        public boolean isLiked = false;  // this flag is only control by likeButton

        public MomentHolder(View itemView) {
            super(itemView);

            mAvaterView = (ImageView) itemView.findViewById(R.id.iv_card_avater);
            mNicknameView = (TextView) itemView.findViewById(R.id.tv_card_nickname);
            mLikeButton = (ImageButton) itemView.findViewById(R.id.btn_card_like);
            mLikesCountView = (TextView) itemView.findViewById(R.id.tv_card_likescount);
            mMomentTextView = (TextView) itemView.findViewById(R.id.tv_card_text);
            mMomentImageView = (ImageView) itemView.findViewById(R.id.iv_card_image);
            tsLikesCounter = (TextSwitcher) itemView.findViewById(R.id.tsLikesCounter);

        }

        public Context getContext() {
            return mContext;
        }

        public void setContext(Context mContext) {
            this.mContext = mContext;
        }

        public String getNickname() {
            return mNicknameView.getText().toString();
        }

        public void setNickname(String nickname) {
            this.mNicknameView.setText(nickname);
        }

        public String getLikesCount() {
            return mLikesCountView.getText().toString();
        }

        public void setLikesCount(String likesCount) {
            this.tsLikesCounter.setCurrentText(likesCount);
        }

        public String getMomentText() {
            return mMomentTextView.getText().toString();
        }

        public void setMomentText(String momentText) {
            this.mMomentTextView.setText(momentText);
        }

        public void like() {
            this.mLikeButton.setImageResource(R.drawable.ic_heart_red);
            this.isLiked = true;
        }

        public void unlike() {
            this.mLikeButton.setImageResource(R.drawable.ic_heart_outline_grey);
            this.isLiked = false;
        }

    }

    public interface OnMomentInteractionListener {
        void onMomentClick(MomentHolder holder, Moment moment);
    }
}
