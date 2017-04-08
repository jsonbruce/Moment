package com.bukeu.moment;

import android.app.Application;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.MomentList;
import com.bukeu.moment.model.User;
import com.bukeu.moment.model.UserList;
import com.bukeu.moment.util.StringUtils;
import com.bukeu.moment.view.service.CoreService;
import com.bukeu.moment.view.service.MomentService;
import com.bukeu.moment.view.service.PushService;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Max on 2015/3/25.
 */
public class MomentApplication extends Application {

    private static MomentApplication mContext;
    private RequestQueue mRequestQueue;

    /** signed user */
    private User mUser;
    /** published moment */
    private Moment mMoment;
    /** user's moments */
    private MomentList mUserMomentList;

    /** uuid set of following */
    private Set<String> mFollowingUUID = new HashSet<>();
    private UserList mFollowingList;
    /** uuid set of followers */
    private Set<String> mFollowersUUID = new HashSet<>();
    private UserList mFollowersList;
    /** user's liked momentId today */
    private List<Long> mLikedMomentId = new ArrayList<>();
    /** the <momentId, likeOperationCount> , op_count&1 == 1, send to server*/
    private Map<Long, Long> mLikeOperation = new HashMap<>();

    /** moments cache for pre-fetch */
    private MomentList mMomentsCache;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        startService(new Intent(this, CoreService.class));
        startService(new Intent(this, PushService.class));
        startService(new Intent(this, MomentService.class));
//        sendBroadcast(new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_START_MOMENTSERVICE));

        mRequestQueue = Volley.newRequestQueue(this);

        MomentConfig.initLocalStorage();
        Thread.setDefaultUncaughtExceptionHandler(MomentException.getExceptionHandler());

    }

    public synchronized static MomentApplication getContext() {
        return mContext;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public void saveUser() {
        MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_USER)
                .putString(MomentConfig.PREF_KEY_USER_SIGNED, JSON.toJSONString(mUser))
                .apply();
    }

    public User getUser() {
        String user = MomentConfig.getConfig(mContext).getSharedPreferences(MomentConfig.PREF_USER)
                .getString(MomentConfig.PREF_KEY_USER_SIGNED, null);
        if (user != null) {
            mUser = JSON.parseObject(user, User.class);
        }
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;
        if (user != null) {
            saveUser();
        }
    }

    public void checkSignin() {
        mUser = getUser();
        if (mUser != null) {
            ApiController.getUser(mUser.getUuid(), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, Charset.forName("utf-8"));
                    User user = JSON.parseObject(response, User.class);
                    MomentApplication.getContext().setUser(user);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    mUser = null;
                }
            });
        }
    }

    public void deleteSignin() {
        setUser(null);
        MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_USER)
                .putString(MomentConfig.PREF_KEY_USER_SIGNED, null)
                .apply();
    }

    public Moment getMoment() {
        if (mMoment == null) {
            mMoment = new Moment();
        }
        return mMoment;
    }

    public void setMoment(Moment moment) {
        this.mMoment = moment;
    }

    public void syncUserMoments() {
        List<Moment> moments = ApiController.getUserMoments();
        if (mUserMomentList == null) {
            mUserMomentList = new MomentList();
        }
        if (moments != null) {
            mUserMomentList.setMoments(moments);
            if (mUserMomentList != null && moments.size() > 0) {
                MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_USER)
                        .putString(MomentConfig.PREF_KEY_USER_MOMENTS, JSON.toJSONString(mUserMomentList))
                        .apply();
            }
        }
    }

    public MomentList getUserMomentList() {
        if (mUserMomentList != null) {
            String s = MomentConfig.getConfig(mContext).getSharedPreferences(MomentConfig.PREF_USER)
                    .getString(MomentConfig.PREF_KEY_USER_MOMENTS, null);
            mUserMomentList = JSON.parseObject(s, MomentList.class);
        }
        return mUserMomentList;
    }


    public void syncUserState() {
        Moment moment = ApiController.getUserTodayMoment();
        if (moment != null) {
            MomentConfig.getConfig(mContext).setHasPublished(true);
        } else {
            MomentConfig.getConfig(mContext).setHasPublished(false);
        }
    }

    public void syncFollowing() {
        List<User> following = ApiController.getFollowing();
        if (mFollowingList == null) {
            mFollowingList = new UserList();
            mFollowingList.setUsers(following);
        }
        for (User aFollowing : following) {
            mFollowingUUID.add(aFollowing.getUuid());
        }
        if (mFollowingUUID != null && mFollowingUUID.size() > 0) {
            MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_USER)
                    .putStringSet(MomentConfig.PREF_KEY_USER_FOLLOWING, mFollowingUUID)
                    .apply();
        }
    }

    public Set<String> getFollowingUUID() {
        if (mFollowingUUID == null) {
            mFollowingUUID = MomentConfig.getConfig(mContext).getSharedPreferences(MomentConfig.PREF_USER)
                    .getStringSet(MomentConfig.PREF_KEY_USER_FOLLOWING, null);
        }
        return mFollowingUUID;
    }

    public UserList getFollowingList() {
        return mFollowingList;
    }

    public UserList getFollowersList() {
        return mFollowersList;
    }

    public void syncFollowers() {
        List<User> followers = ApiController.getFollowers();
        if (mFollowersList == null) {
            mFollowersList = new UserList();
            mFollowersList.setUsers(followers);
        }
        for (User follower : followers) {
            mFollowersUUID.add(follower.getUuid());
        }
        if (mFollowersUUID != null && mFollowersUUID.size() > 0) {
            MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_USER)
                    .putStringSet(MomentConfig.PREF_KEY_USER_FOLLOWERS, mFollowersUUID)
                    .apply();
        }
    }

    public Set<String> getFollowers() {
        if (mFollowersUUID == null) {
            mFollowersUUID = MomentConfig.getConfig(mContext).getSharedPreferences(MomentConfig.PREF_USER)
                    .getStringSet(MomentConfig.PREF_KEY_USER_FOLLOWERS, null);
        }
        return mFollowersUUID;
    }

    public void syncLikedMomentId() {
        mLikedMomentId = ApiController.getLikedMomentId();

        if (mLikedMomentId != null && mLikedMomentId.size() > 0) {
            MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_USER)
                    .putString(MomentConfig.PREF_KEY_USER_LIKED_MOMENTID, StringUtils.toBase64String(mLikedMomentId))
                    .apply();;
        }
    }

    public List<Long> getLikedMomentId() {
        if (mLikedMomentId == null || mLikedMomentId.size() == 0) {
            String s = MomentConfig.getConfig(mContext).getSharedPreferences(MomentConfig.PREF_USER)
                    .getString(MomentConfig.PREF_KEY_USER_LIKED_MOMENTID, null);
            if (s != null) {
                mLikedMomentId = (List<Long>) StringUtils.fromBase64String(s);
            }
        }
        return mLikedMomentId;
    }

    public Map<Long, Long> getLikeOperation() {
        return mLikeOperation;
    }

    public void addLikeOperation(Long momentId) {
        long op = mLikeOperation.get(momentId) == null ? 0 : mLikeOperation.get(momentId);
        mLikeOperation.put(momentId, ++op);
    }

    public void clearLikeOperation() {
        this.mLikeOperation.clear();
    }

    /**
     *  save moments cache on exit
     */
    public void saveMomentsCache() {
        MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_MOMENT)
                .putString(MomentConfig.PREF_KEY_MOMENTS_CACHE, JSON.toJSONString(mMomentsCache))
                .apply();
    }

    /** pre-fetch moments */
    public MomentList getMomentsCache() {
        if (mMomentsCache == null || mMomentsCache.getMoments().size() == 0) {
            String moments = MomentConfig.getConfig(mContext).getSharedPreferences(MomentConfig.PREF_MOMENT)
                    .getString(MomentConfig.PREF_KEY_MOMENTS_CACHE, null);
            if (!StringUtils.isEmpty(moments)) {
                mMomentsCache = JSON.parseObject(moments, MomentList.class);
            }
        }
        return mMomentsCache;
    }

    public void setMomentsCache(MomentList mMomentsCache) {
        this.mMomentsCache = mMomentsCache;
        saveMomentsCache();
    }


// app info

    public void syncAppVersion(boolean hasUpdate) {
        MomentConfig.getConfig(mContext).getSharedPreferencesEditor(MomentConfig.PREF_APP)
                .putBoolean(MomentConfig.PREF_KEY_APP_UPDATE, hasUpdate)
                .apply();
    }

    public boolean hasNewAppVersion() {
        return MomentConfig.getConfig(mContext).getSharedPreferences(MomentConfig.PREF_APP)
                .getBoolean(MomentConfig.PREF_KEY_APP_UPDATE, false);
    }


}
