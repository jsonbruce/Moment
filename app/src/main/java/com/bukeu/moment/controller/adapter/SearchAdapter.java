package com.bukeu.moment.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.User;
import com.bukeu.moment.util.UIHelper;
import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.List;
import java.util.Set;

/**
 * Created by Max on 2015/4/11.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {

    private Context mContext;

    private Set<String> mFollowing;
    private List<User> mUsers;

    public SearchAdapter(Context context, List<User> users) {
        this.mContext = context;
        this.mFollowing = MomentApplication.getContext().getFollowingUUID();
        this.mUsers = users;
    }

    @Override
    public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user, parent, false);
        return new SearchHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchHolder holder, int position) {
        final User user = mUsers.get(position);

        if (user != null) {
            Glide.with(mContext)
                    .load(user.getAvater())
                    .placeholder(R.drawable.default_avater)
                    .error(R.drawable.default_avater)
                    .into(holder.mAvaterView);
            holder.mNicknameView.setText(user.getNickname());

            if (mFollowing != null && mFollowing.contains(user.getUuid())) {
                holder.mFollowButton.setVisibility(View.GONE);
            }
            holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MomentApplication.getContext().getUser() == null) {
                        UIHelper.startSigninActivity(mContext);
                    } else {
                        v.setVisibility(View.GONE);
                        updateFollowing(v, user.getUuid());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mUsers == null ? 0 : mUsers.size();
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

    public static class SearchHolder extends RecyclerView.ViewHolder {

        protected ImageView mAvaterView;
        private TextView mNicknameView;
        private ImageButton mFollowButton;

        public SearchHolder(View itemView) {
            super(itemView);

            mAvaterView = (ImageView) itemView.findViewById(R.id.iv_card_avater);
            mNicknameView = (TextView) itemView.findViewById(R.id.tv_card_nickname);
            mFollowButton = (ImageButton) itemView.findViewById(R.id.btn_card_follow);
        }
    }

}
