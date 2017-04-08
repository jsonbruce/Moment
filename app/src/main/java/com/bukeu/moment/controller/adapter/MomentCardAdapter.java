package com.bukeu.moment.controller.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.controller.listener.OnDoubleClickListener;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.util.UIHelper;
import com.bumptech.glide.Glide;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Max on 2015/3/26.
 */
public class MomentCardAdapter extends RecyclerSwipeAdapter<MomentCardAdapter.MomentCardHolder>
        implements View.OnClickListener {

    private static final int VIEW_TYPE_DEFAULT = 1;
    private static final int VIEW_TYPE_LOADER = 2;

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private static final int ANIMATED_ITEMS_COUNT = 2;

    private final Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
    private int lastAnimatedPosition = -1;
    private boolean animateItems = false;
    private boolean showLoadingView = false;

    private Context mContext;
    private List<Moment> mMoments = new ArrayList<>();

    private Set<String> mFollowing;
    private List<Long> mLikedMomentId = new ArrayList<>();

    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(UIHelper.getScreenHeight(mContext));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    public MomentCardAdapter(Context context, List<Moment> ms) {
        this.mContext = context;
        this.mMoments = ms;
        mFollowing = MomentApplication.getContext().getFollowingUUID();
        mLikedMomentId = MomentApplication.getContext().getLikedMomentId();
        if (MomentApplication.getContext().getUser() != null) {
            mFollowing.add(MomentApplication.getContext().getUser().getUuid());
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe;
    }

    @Override
    public MomentCardHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_moment, viewGroup, false);
        MomentCardHolder momentCardHolder = new MomentCardHolder(v);
        if (viewType == VIEW_TYPE_DEFAULT) {
            momentCardHolder.mAvaterView.setOnClickListener(this);
            momentCardHolder.mFollowButton.setOnClickListener(this);
            momentCardHolder.mLikeButton.setOnClickListener(this);
//            momentCardHolder.mMomentImageView.setOnTouchListener(doubleClickListenerOnTouch);
        } else if (viewType == VIEW_TYPE_LOADER) {
            View bgView = new View(mContext);
            bgView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ));
            bgView.setBackgroundColor(0x77ffffff);
            momentCardHolder.vImageRoot.addView(bgView);
        }
        return momentCardHolder;
    }

    @Override
    public void onBindViewHolder(MomentCardHolder momentCardHolder, int position) {
        runEnterAnimation(momentCardHolder.itemView, position);
        if (getItemViewType(position) == VIEW_TYPE_DEFAULT) {
            bindViewHolderItem(momentCardHolder, position);
        } else if (getItemViewType(position) == VIEW_TYPE_LOADER) {
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return mMoments == null ? 0 : mMoments.size();
    }

    private void bindViewHolderItem(final MomentCardHolder momentCardHolder, int position) {
        Moment moment = mMoments.get(position);
        momentCardHolder.setContext(mContext);

        momentCardHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        momentCardHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
            }
        });
        momentCardHolder.swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                if (MomentApplication.getContext().getUser() == null) {
                    UIHelper.startSigninActivity(mContext);
                } else {
                    if (!momentCardHolder.isLiked) {
                        updateLikeButton(momentCardHolder);
                        updateLikesCount(momentCardHolder, true);
                        MomentApplication.getContext().addLikeOperation(
                                mMoments.get(momentCardHolder.getLayoutPosition()).getId());
                    }
                    updateLikeAnimation(momentCardHolder);
                }
            }
        });

        if (mFollowing != null && mFollowing.contains(moment.getUuid())) {
            momentCardHolder.mFollowButton.setVisibility(View.GONE);
        }

        if ((mLikedMomentId != null && mLikedMomentId.contains(moment.getId()))) {
            momentCardHolder.like();
            momentCardHolder.mLikeButton.setImageResource(R.drawable.ic_heart_red);
        } else if ((mLikedMomentId != null && !mLikedMomentId.contains(moment.getId()))){
            momentCardHolder.unlike();
            momentCardHolder.mLikeButton.setImageResource(R.drawable.ic_heart_outline_grey);
        }

        momentCardHolder.setNickname(moment.getNickname());
        momentCardHolder.setLocation(moment.getLocationName());
        momentCardHolder.setMomentText(moment.getText());
        if (moment.getLikesCount() != null) {
            momentCardHolder.setLikesCount(mContext.getResources().getQuantityString(
                    R.plurals.likes_count, moment.getLikesCount().intValue(), moment.getLikesCount()));
        }

//        momentCardHolder.mAvaterView.setTag(momentCardHolder);
//        momentCardHolder.mMomentImageView.setTag(momentCardHolder);
        momentCardHolder.mLikeButton.setTag(momentCardHolder);
        momentCardHolder.mFollowButton.setTag(moment);

//        ImageManager.getInstance(mContext, mRootView).loadBitmaps(momentCardHolder.mAvaterView, moment.getAvater());
//        ImageManager.getInstance(mContext, mRootView).loadBitmaps(momentCardHolder.mMomentImageView, moment.getImage());

        Glide.with(mContext).load(moment.getAvater())
                .placeholder(R.drawable.default_avater)
                .error(R.drawable.default_avater)
                .into(momentCardHolder.mAvaterView);
        Glide.with(mContext).load(moment.getImage())
                .placeholder(R.drawable.default_moment)
                .error(R.drawable.default_moment)
                .into(momentCardHolder.mMomentImageView);

        if (likeAnimations.containsKey(momentCardHolder)) {
            likeAnimations.get(momentCardHolder).cancel();
        }
        resetLikeAnimationState(momentCardHolder);
    }

    @Override
    public void onClick(View item) {
        final int viewId = item.getId();
        switch (viewId) {
            case R.id.iv_card_image:
                UIHelper.showToastMessage(mContext, "single click");
                break;
            case R.id.btn_card_like:
                if (MomentApplication.getContext().getUser() == null) {
                    UIHelper.startSigninActivity(mContext);
                } else {
                    MomentCardHolder holder = (MomentCardHolder) item.getTag();
                    Moment moment = mMoments.get(holder.getLayoutPosition());

                    updateLikeButton(holder);
                    updateLikesCount(holder, true);
                    MomentApplication.getContext().addLikeOperation(moment.getId());
                }
                break;
            case R.id.btn_card_follow:
                if (MomentApplication.getContext().getUser() == null) {
                    UIHelper.startSigninActivity(mContext);
                } else {
                    Moment moment = (Moment) item.getTag();
                    item.setVisibility(View.GONE);
                    updateFollowing(item, moment.getUuid());
                }
                break;
            case R.id.iv_card_avater:
                break;
        }
    }

    public OnDoubleClickListener doubleClickListenerOnTouch = new OnDoubleClickListener() {
        @Override
        public void onDoubleClick(View v) {
            MomentCardHolder holder = (MomentCardHolder) v.getTag();
            if (!holder.isLiked) {
                updateLikeButton(holder);
                updateLikesCount(holder, true);
                updateLikeAnimation(holder);
                MomentApplication.getContext().addLikeOperation(mMoments.get(holder.getLayoutPosition()).getId());
            } else {
                updateLikeAnimation(holder);
            }
        }
    };

    public void updateMoments(List<Moment> moments) {
        updateMoments(moments, true);
    }

    public void updateMoments(List<Moment> moments, boolean animated) {
        this.mMoments = moments;
        animateItems = animated;
        notifyDataSetChanged();
    }

    public void addMoment(Moment moment) {
        this.mMoments.add(0, moment);
        notifyItemInserted(0);
    }

    public void addMoments(List<Moment> moments) {
        this.mMoments.addAll(mMoments);
        notifyItemRangeInserted(mMoments.size(), moments.size());
    }

    public void insertMoments(List<Moment> moments, int position) {
        this.mMoments.addAll(position, moments);
        notifyItemRangeInserted(position, moments.size());
    }

    private void updateFollowing(final View view, String uuid) {
        ApiController.updateFollowing(uuid, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                UIHelper.showToastMessage(mContext, "follow success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                UIHelper.showToastMessage(mContext, "follow failed");
                view.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * update likescount when click like button
     *
     * @param holder
     * @param animated
     */
    private void updateLikesCount(MomentCardHolder holder, boolean animated) {
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

    /**
     * update likebutton when click likebutton / doubleclick
     *
     * @param holder
     */
    private void updateLikeButton(final MomentCardHolder holder) {
        if (holder.isLiked) {
            holder.mLikeButton.setImageResource(R.drawable.ic_heart_outline_grey);
            holder.unlike();
            mLikedMomentId.remove(mMoments.get(holder.getLayoutPosition()).getId());
        } else {
            holder.mLikeButton.setImageResource(R.drawable.ic_heart_red);
            holder.like();
            mLikedMomentId.add(mMoments.get(holder.getLayoutPosition()).getId());
        }
    }

    /**
     * double click like animation
     *
     * @param holder
     */
    private void updateLikeAnimation(final MomentCardHolder holder) {
        if (!likeAnimations.containsKey(holder)) {
            holder.vBgLike.setVisibility(View.VISIBLE);
            holder.ivLike.setVisibility(View.VISIBLE);

            holder.vBgLike.setScaleY(0.1f);
            holder.vBgLike.setScaleX(0.1f);
            holder.vBgLike.setAlpha(1f);
            holder.ivLike.setScaleY(0.1f);
            holder.ivLike.setScaleX(0.1f);

            AnimatorSet animatorSet = new AnimatorSet();
            likeAnimations.put(holder, animatorSet);

            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
            bgScaleYAnim.setDuration(200);
            bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
            bgScaleXAnim.setDuration(200);
            bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
            bgAlphaAnim.setDuration(200);
            bgAlphaAnim.setStartDelay(150);
            bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
            imgScaleUpYAnim.setDuration(300);
            imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
            imgScaleUpXAnim.setDuration(300);
            imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
            imgScaleDownYAnim.setDuration(300);
            imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
            imgScaleDownXAnim.setDuration(300);
            imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

            animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
            animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetLikeAnimationState(holder);
                }
            });
            animatorSet.start();
        }
    }

    private void resetLikeAnimationState(MomentCardHolder holder) {
        likeAnimations.remove(holder);
        holder.vBgLike.setVisibility(View.GONE);
        holder.ivLike.setVisibility(View.GONE);
    }

    public static class MomentCardHolder extends RecyclerView.ViewHolder {

        private Context mContext;

        protected ImageView mAvaterView;
        private TextView mNicknameView;
        private TextView mLocationView;
        private ImageButton mFollowButton;
        protected ImageButton mLikeButton;
        TextSwitcher tsLikesCounter;
        private TextView mLikesCountView;
        private TextView mMomentTextView;
        protected ImageView mMomentImageView;

        protected SwipeLayout swipeLayout;

        protected FrameLayout vImageRoot;
        protected View vBgLike;
        protected ImageView ivLike;

        protected boolean isLiked = false;  // this flag is only control by likeButton

        public MomentCardHolder(View itemView) {
            super(itemView);

            mAvaterView = (ImageView) itemView.findViewById(R.id.iv_card_avater);
            mNicknameView = (TextView) itemView.findViewById(R.id.tv_card_nickname);
            mLocationView = (TextView) itemView.findViewById(R.id.tv_card_location);
            mFollowButton = (ImageButton) itemView.findViewById(R.id.btn_card_follow);
            mLikeButton = (ImageButton) itemView.findViewById(R.id.btn_card_like);
            mLikesCountView = (TextView) itemView.findViewById(R.id.tv_card_likescount);
            mMomentTextView = (TextView) itemView.findViewById(R.id.tv_card_text);
            mMomentImageView = (ImageView) itemView.findViewById(R.id.iv_card_image);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

            vImageRoot = (FrameLayout) itemView.findViewById(R.id.vImageRoot);
            vBgLike = (View) itemView.findViewById(R.id.v_card_like_bg);
            ivLike = (ImageView) itemView.findViewById(R.id.iv_card_like_fg);
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

        public String getLocation() {
            return mLocationView.getText().toString();
        }

        public void setLocation(String location) {
            this.mLocationView.setText(location);
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
            this.isLiked = true;
        }

        public void unlike() {
            this.isLiked = false;
        }

    }

}
