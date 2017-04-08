package com.bukeu.moment.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.controller.listener.OnDoubleClickListener;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.util.UIHelper;
import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.Set;

/**
 * Created by Max on 2015/4/19.
 */
public class MomentDetailFragment extends DialogFragment implements View.OnClickListener{

    public static final String TAG = "MomentDetailFragment";

    public static final String ARG_PARAM_MOMENT = "moment";
    public static final String ARG_PARAM_ISLIKED = "isliked";

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private Activity mActivity;
    private Moment mMoment;
    private boolean isLiked;
    private Set<String> mFollowing;

    private ImageView mAvaterView;
    private TextView mNicknameView;
    private TextView mDateView;
    private TextView mLocationView;
    private ImageView mFollowButton;
    private ImageView mLikeButton;
    private TextView mMomentTextView;
    private ImageView mMomentImageView;

    private FrameLayout vImageRoot;
    private View vBgLike;
    private ImageView ivLike;

    private OnMomentDetailFragmentInteractionListener mListener;

    public static MomentDetailFragment newInstance(Moment moment, boolean isLiked) {
        MomentDetailFragment fragment = new MomentDetailFragment();
        fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme_FragmentDialog);
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_MOMENT, moment);
        args.putBoolean(ARG_PARAM_ISLIKED, isLiked);
        fragment.setArguments(args);
        return fragment;
    }

    public MomentDetailFragment setOnMomentDetailFragmentInteractionListener(OnMomentDetailFragmentInteractionListener listener) {
        this.mListener = listener;
        return this;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMoment = (Moment) getArguments().getSerializable(ARG_PARAM_MOMENT);
            isLiked = getArguments().getBoolean(ARG_PARAM_ISLIKED);
        }
        mFollowing = MomentApplication.getContext().getFollowingUUID();
        if (MomentApplication.getContext().getUser() != null) {
            mFollowing.add(MomentApplication.getContext().getUser().getUuid());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = inflater.inflate(R.layout.fragment_moment_detail, container);

        initView(rootView);
        initData();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        int dialogWidth = UIHelper.getScreenWidth(mActivity);
        getDialog().getWindow().setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void initView(View rootView) {
        mAvaterView = (ImageView) rootView.findViewById(R.id.iv_card_avater);
        mNicknameView = (TextView) rootView.findViewById(R.id.tv_card_nickname);
        mFollowButton = (ImageView) rootView.findViewById(R.id.btn_card_follow);
        mLikeButton = (ImageView) rootView.findViewById(R.id.btn_card_like);
        mDateView = (TextView) rootView.findViewById(R.id.tv_card_date);
        mLocationView = (TextView) rootView.findViewById(R.id.tv_card_location);
        mMomentTextView = (TextView) rootView.findViewById(R.id.tv_card_text);
        mMomentImageView = (ImageView) rootView.findViewById(R.id.iv_card_image);

        vImageRoot = (FrameLayout) rootView.findViewById(R.id.vImageRoot);
        vBgLike = rootView.findViewById(R.id.v_card_like_bg);
        ivLike = (ImageView) rootView.findViewById(R.id.iv_card_like_fg);

        Glide.with(mActivity).load(mMoment.getImage())
                .placeholder(R.drawable.default_moment)
                .error(R.drawable.default_moment)
                .into(mMomentImageView);
        Glide.with(mActivity).load(mMoment.getAvater())
                .placeholder(R.drawable.default_avater)
                .error(R.drawable.default_avater)
                .into(mAvaterView);

        updateLikeButton();
        mLocationView.setText(mMoment.getLocationName() != null ? mMoment.getLocationName() : "未知的地方");
        mNicknameView.setText(mMoment.getNickname());
        mDateView.setText(mMoment.getCreateDate());
        mMomentTextView.setText(mMoment.getText());

        if (mFollowing != null && mFollowing.contains(mMoment.getUuid())) {
            mFollowButton.setVisibility(View.GONE);
        }

    }

    private void initData() {
        mFollowButton.setOnClickListener(this);
        mLikeButton.setOnClickListener(this);
        mMomentImageView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                animateLikeAction();
            }
        });
    }

    private void updateFollowing(final View view, String uuid) {
        view.setVisibility(View.GONE);
        ApiController.updateFollowing(uuid, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                UIHelper.showToastMessage(mActivity, "follow success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                UIHelper.showToastMessage(mActivity, "follow failed");
                view.setVisibility(View.VISIBLE);
            }
        });
    }

    private void animateLikeAction() {
        vBgLike.setVisibility(View.VISIBLE);
        ivLike.setVisibility(View.VISIBLE);

        vBgLike.setScaleY(0.1f);
        vBgLike.setScaleX(0.1f);
        vBgLike.setAlpha(1f);
        ivLike.setScaleY(0.1f);
        ivLike.setScaleX(0.1f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(200);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(200);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(200);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                vBgLike.setVisibility(View.GONE);
                ivLike.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_card_follow:
                if (MomentApplication.getContext().getUser() == null) {
                    UIHelper.startSigninActivity(mActivity);
                } else {
                    updateFollowing(v, mMoment.getUuid());
                }
                break;
            case R.id.btn_card_like:
                if (MomentApplication.getContext().getUser() == null) {
                    UIHelper.startSigninActivity(mActivity);
                } else {
                    isLiked = !isLiked;
                    updateLikeButton();

                    if (mListener != null) {
                        mListener.onMomentDetailLikeButtonClick(isLiked);
                    }

                }
                break;
        }
    }

    private void updateLikeButton() {
        if (isLiked) {
            mLikeButton.setImageResource(R.drawable.ic_like);
        } else {
            mLikeButton.setImageResource(R.drawable.ic_unlike);
        }
    }

    public interface OnMomentDetailFragmentInteractionListener {
        public void onMomentDetailLikeButtonClick(boolean isLiked);
    }

}
