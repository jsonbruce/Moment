package com.bukeu.moment.controller.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.model.User;
import com.bukeu.moment.view.widget.FlexibleDividerDecoration;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 2015/4/17.
 */
public class DrawerMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        FlexibleDividerDecoration.PaintProvider,
        FlexibleDividerDecoration.VisibilityProvider {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private static final int MIN_ITEM_COUNT = 1;
    public final String ITEM_HOME;
    public final String ITEM_GROUP;
    public final String ITEM_NEARBY;
    public final String ITEM_SETTINGS;
    public final String ITEM_HELP;

    private Context mContext;
    private OnDrawerMenuClickListener mListener;
    private List<DrawerMenuItem> mDrawerMenuItems = new ArrayList<>();

    public DrawerMenuAdapter(Context context, OnDrawerMenuClickListener listener) {
        this.mContext = context;
        this.mListener = listener;

        ITEM_HOME = mContext.getString(R.string.drawer_menu_home);
        ITEM_GROUP = mContext.getString(R.string.drawer_menu_group);
        ITEM_NEARBY = mContext.getString(R.string.drawer_menu_nearby);
        ITEM_SETTINGS = mContext.getString(R.string.drawer_menu_settings);
        ITEM_HELP = mContext.getString(R.string.drawer_menu_help);

        mDrawerMenuItems.add(new DrawerMenuItem(R.drawable.ic_global_menu_feed, ITEM_HOME));
        mDrawerMenuItems.add(new DrawerMenuItem(R.drawable.ic_heart_outline_grey, ITEM_GROUP));
        mDrawerMenuItems.add(new DrawerMenuItem(R.drawable.ic_global_menu_nearby, ITEM_NEARBY));
        mDrawerMenuItems.add(new DrawerMenuItem(ITEM_SETTINGS));
        mDrawerMenuItems.add(new DrawerMenuItem(ITEM_HELP));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)  {
        if (VIEW_TYPE_HEADER == viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_drawer_menu_header, parent, false);
            return new DrawerMenuHeaderHolder(itemView);

        } else if (VIEW_TYPE_ITEM == viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer_menu, parent, false);
            return new DrawerMenuItemHolder(itemView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (VIEW_TYPE_HEADER == getItemViewType(position)) {
            bindMenuHeader((DrawerMenuHeaderHolder) viewHolder, position);

        } else if (VIEW_TYPE_ITEM == getItemViewType(position)) {
            bindMenuItem((DrawerMenuItemHolder) viewHolder, position);
        }
    }

    private void bindMenuHeader(DrawerMenuHeaderHolder holder, int position) {
        User user = MomentApplication.getContext().getUser();
        if (user != null) {
            Glide.with(mContext)
                    .load(user.getAvater())
                    .placeholder(R.drawable.img_circle_placeholder)
                    .into(holder.mAvaterView);
            holder.mNicknameView.setText(user.getNickname());
            holder.mDescriptionView.setText(user.getDescription());
        }

        holder.mHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDrawerMenuHeaderClick(v);
                }
            }
        });

    }

    private void bindMenuItem(final DrawerMenuItemHolder holder, final int position) {
        DrawerMenuItem menuItem = mDrawerMenuItems.get(position - MIN_ITEM_COUNT);

        if (menuItem.hasIcon) {
            holder.mMenuIcon.setImageResource(menuItem.iconResId);
        } else {
            holder.mMenuIcon.setVisibility(View.GONE);
        }
        holder.mMenuLabel.setText(menuItem.label);
        holder.mMenuItemLayout.setTag(menuItem.label);

        if (holder.mMenuItemLayout.getTag().equals(ITEM_HOME)) {
            holder.mMenuItemLayout.setBackgroundResource(R.color.btn_context_menu_pressed);
        }

        holder.mMenuItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDrawerMenuItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return MIN_ITEM_COUNT + mDrawerMenuItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public Paint dividerPaint(int position, RecyclerView parent) {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(2);
        return paint;
    }

    @Override
    public boolean shouldHideDivider(int position, RecyclerView parent) {
        if (position > 0 && position < 3) {
            return true;
        } else if (position > 3) {
            return true;
        }
        return false;
    }


    public void updateDrawerMenuHeader() {
        notifyItemChanged(0);
    }

    static class DrawerMenuHeaderHolder extends RecyclerView.ViewHolder {

        LinearLayout mHeaderLayout;
        ImageView mAvaterView;
        TextView mNicknameView;
        TextView mDescriptionView;

        public DrawerMenuHeaderHolder(View itemView) {
            super(itemView);

            mHeaderLayout = (LinearLayout) itemView.findViewById(R.id.layout_drawer_menu_header);
            mAvaterView = (ImageView) itemView.findViewById(R.id.iv_drawer_avater);
            mNicknameView = (TextView) itemView.findViewById(R.id.tv_drawer_nickname);
            mDescriptionView = (TextView) itemView.findViewById(R.id.tv_drawer_description);
        }
    }

    static class DrawerMenuItemHolder extends RecyclerView.ViewHolder {

        LinearLayout mMenuItemLayout;
        ImageView mMenuIcon;
        TextView mMenuLabel;

        public DrawerMenuItemHolder(View itemView) {
            super(itemView);

            this.mMenuItemLayout = (LinearLayout) itemView.findViewById(R.id.layout_drawer_menu_item_root);
            this.mMenuIcon = (ImageView) itemView.findViewById(R.id.iv_drawer_menu_item_icon);
            this.mMenuLabel = (TextView) itemView.findViewById(R.id.tv_drawer_menu_item_label);
        }
    }

    static class DrawerMenuItem {
        public int iconResId;
        public String label;
        public boolean hasIcon = true;

        public DrawerMenuItem(String label) {
            this.label = label;
            this.hasIcon = false;
        }

        public DrawerMenuItem(int iconResId, String label) {
            this.iconResId = iconResId;
            this.label = label;
        }

    }

    public interface OnDrawerMenuClickListener {
        void onDrawerMenuHeaderClick(View view);
        void onDrawerMenuItemClick(View view, int position);
    }

}
