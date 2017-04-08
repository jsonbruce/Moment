package com.bukeu.moment.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.User;
import com.bumptech.glide.Glide;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MIN_ITEMS_COUNT = 2;

    public static final int TYPE_PROFILE_HEADER = 0;
    public static final int TYPE_PROFILE_OPTIONS = 1;
    public static final int TYPE_TIMELINE = 2;

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final int MAX_PHOTO_ANIMATION_DELAY = 600;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    private boolean lockedAnimations = false;
    private long profileHeaderAnimationStartTime = 0;
    private int lastAnimatedItem = 0;

    private final Context mContext;
    private List<Moment> mMoments = new ArrayList<>();

    private OnUserProfileItemClickListener mOnUserProfileItemClickListener;

    public UserProfileAdapter(Context context, List<Moment> moments) {
        this.mContext = context;
        this.mMoments = moments;
    }

    public void setOnInteractionListener(OnUserProfileItemClickListener listener) {
        this.mOnUserProfileItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_PROFILE_HEADER;
        } else if (position == 1) {
            return TYPE_PROFILE_OPTIONS;
        } else {
            return TYPE_TIMELINE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_PROFILE_HEADER == viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_profile_header, parent, false);
            return new ProfileHeaderViewHolder(view);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_profile_options, parent, false);
            return new ProfileOptionsViewHolder(view);
        } else if (TYPE_TIMELINE == viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.item_timeline, parent, false);
            return new TimelineHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (TYPE_PROFILE_HEADER == viewType) {
            bindProfileHeader((ProfileHeaderViewHolder) holder);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            bindProfileOptions((ProfileOptionsViewHolder) holder);
        } else if (TYPE_TIMELINE == viewType) {
            bindTimeline((TimelineHolder) holder, position);
        }
    }

    private void bindProfileHeader(final ProfileHeaderViewHolder holder) {
        User user = MomentApplication.getContext().getUser();

        Glide.with(mContext)
                .load(user.getAvater())
                .placeholder(R.drawable.img_circle_placeholder)
                .into(holder.ivUserProfilePhoto);
        holder.userNicknameView.setText(user.getNickname());

        Long momentsCount = user.getMomentsCount();
        Long followers = user.getFollowers();
        Long following = user.getFollowing();
        if (momentsCount != null) {
            holder.userMomentsCountView.setText(String.valueOf(momentsCount));
        }
        if (followers != null) {
            holder.userFollowersView.setText(String.valueOf(followers));
        }
        if (following != null) {
            holder.userFollowingView.setText(String.valueOf(following));
        }
        holder.momentsCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnUserProfileItemClickListener != null) {
                    mOnUserProfileItemClickListener.onMomentsCountClickListener();
                }
            }
        });
        holder.followersCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnUserProfileItemClickListener != null) {
                    mOnUserProfileItemClickListener.onFollowersCountClickListener();
                }
            }
        });
        holder.followingCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnUserProfileItemClickListener != null) {
                    mOnUserProfileItemClickListener.onFollowingCountClickListener();
                }
            }
        });

        holder.vUserProfileRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.vUserProfileRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                animateUserProfileHeader(holder);
                return false;
            }
        });

    }

    private void bindProfileOptions(final ProfileOptionsViewHolder holder) {
        holder.vButtons.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.vButtons.getViewTreeObserver().removeOnPreDrawListener(this);
                holder.vUnderline.getLayoutParams().width = holder.btnGrid.getWidth();
                holder.vUnderline.requestLayout();
                animateUserProfileOptions(holder);
                return false;
            }
        });
    }

    private void bindTimeline(TimelineHolder holder, int position) {
        Moment moment = mMoments.get(position - MIN_ITEMS_COUNT);

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

        if (lastAnimatedItem < position) {
            lastAnimatedItem = position;
        }
    }


    public void setLockedAnimations(boolean lockedAnimations) {
        this.lockedAnimations = lockedAnimations;
    }

    private void animateUserProfileHeader(ProfileHeaderViewHolder viewHolder) {
        if (!lockedAnimations) {
            profileHeaderAnimationStartTime = System.currentTimeMillis();

            viewHolder.vUserProfileRoot.setTranslationY(-viewHolder.vUserProfileRoot.getHeight());
            viewHolder.ivUserProfilePhoto.setTranslationY(-viewHolder.ivUserProfilePhoto.getHeight());
            viewHolder.vUserDetails.setTranslationY(-viewHolder.vUserDetails.getHeight());
            viewHolder.vUserStats.setAlpha(0);

            viewHolder.vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
            viewHolder.ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
            viewHolder.vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
            viewHolder.vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
        }
    }

    private void animateUserProfileOptions(ProfileOptionsViewHolder viewHolder) {
        if (!lockedAnimations) {
            viewHolder.vButtons.setTranslationY(-viewHolder.vButtons.getHeight());
            viewHolder.vUnderline.setScaleX(0);

            viewHolder.vButtons.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
            viewHolder.vUnderline.animate().scaleX(1).setDuration(200).setStartDelay(USER_OPTIONS_ANIMATION_DELAY + 300).setInterpolator(INTERPOLATOR).start();
        }
    }

    private void animatePhoto(TimelineHolder viewHolder) {
        if (!lockedAnimations) {
            if (lastAnimatedItem == viewHolder.getPosition()) {
                setLockedAnimations(true);
            }

            long animationDelay = profileHeaderAnimationStartTime + MAX_PHOTO_ANIMATION_DELAY - System.currentTimeMillis();
            if (profileHeaderAnimationStartTime == 0) {
                animationDelay = viewHolder.getPosition() * 30 + MAX_PHOTO_ANIMATION_DELAY;
            } else if (animationDelay < 0) {
                animationDelay = viewHolder.getPosition() * 30;
            } else {
                animationDelay += viewHolder.getPosition() * 30;
            }

            viewHolder.flRoot.setScaleY(0);
            viewHolder.flRoot.setScaleX(0);
            viewHolder.flRoot.animate()
                    .scaleY(1)
                    .scaleX(1)
                    .setDuration(200)
                    .setInterpolator(INTERPOLATOR)
                    .setStartDelay(animationDelay)
                    .start();
        }
    }


    @Override
    public int getItemCount() {
        return mMoments == null ? MIN_ITEMS_COUNT : MIN_ITEMS_COUNT + mMoments.size();
    }

    static class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
        Context mContext;

        ImageView ivUserProfilePhoto;
        View vUserDetails;
        View vUserStats;
        View vUserProfileRoot;
        TextView userNicknameView;
        TextView userMomentsCountView;
        TextView userFollowersView;
        TextView userFollowingView;

        LinearLayout momentsCountLayout;
        LinearLayout followersCountLayout;
        LinearLayout followingCountLayout;

        public ProfileHeaderViewHolder(View view) {
            super(view);
            ivUserProfilePhoto = (ImageView) view.findViewById(R.id.iv_profile_avater);
            vUserDetails = view.findViewById(R.id.layout_profile_details);
            vUserStats = view.findViewById(R.id.layout_profile_status);
            vUserProfileRoot = view.findViewById(R.id.layout_profile_root);
            userNicknameView = (TextView) view.findViewById(R.id.tv_user_nickname);
            userMomentsCountView = (TextView) view.findViewById(R.id.tv_user_moments_count);
            userFollowersView = (TextView) view.findViewById(R.id.tv_user_followers);
            userFollowingView = (TextView) view.findViewById(R.id.tv_user_following);

            momentsCountLayout = (LinearLayout) view.findViewById(R.id.layout_profile_momentscount);
            followersCountLayout = (LinearLayout) view.findViewById(R.id.layout_profile_followerscount);
            followingCountLayout = (LinearLayout) view.findViewById(R.id.layout_profile_followingcount);

        }

        public void setContext(Context context) {
            this.mContext = context;
        }

    }

    static class ProfileOptionsViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnGrid;
        ImageButton btnList;
        View vUnderline;
        View vButtons;

        public ProfileOptionsViewHolder(View view) {
            super(view);
            btnGrid = (ImageButton) view.findViewById(R.id.btnGrid);
            btnList = (ImageButton) view.findViewById(R.id.btnList);
            vUnderline = view.findViewById(R.id.vUnderline);
            vButtons = view.findViewById(R.id.vButtons);
        }
    }

    static class TimelineHolder extends RecyclerView.ViewHolder {

        View flRoot;

        TextView mDayView;
        TextView mYearMonthView;

        SwipeLayout swipeLayout;

        TextView mMomentTextView;
        ImageView mMomentImageView;

        public TimelineHolder(View itemView) {
            super(itemView);

            flRoot = itemView.findViewById(R.id.flRoot);
            mDayView = (TextView) itemView.findViewById(R.id.tv_time_day);
            mYearMonthView = (TextView) itemView.findViewById(R.id.tv_time_year_month);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            mMomentTextView = (TextView) itemView.findViewById(R.id.tv_card_text);
            mMomentImageView = (ImageView) itemView.findViewById(R.id.iv_card_image);
        }

        public void setDay(String date) {
            this.mDayView.setText(date.substring(8, 10));
        }

        public void setYearMonth(String date) {
            this.mYearMonthView.setText(date.substring(0, 7));
        }

        public void setMomentText(String text) {
            this.mMomentTextView.setText(text);
        }

    }


    public void updateMoments(List<Moment> moments) {
        this.mMoments = moments;
        notifyDataSetChanged();
    }

    public void loadOldMoments(List<Moment> moments) {
        int positionStart = mMoments.size() + MIN_ITEMS_COUNT;
        this.mMoments.addAll(moments);
        notifyItemRangeInserted(positionStart, moments.size());
    }

    public void updateUserInfo() {
        notifyItemChanged(0);
    }

    public interface OnUserProfileItemClickListener {
        void onMomentsCountClickListener();

        void onFollowersCountClickListener();

        void onFollowingCountClickListener();
    }

}
