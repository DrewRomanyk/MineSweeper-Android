package app.drewromanyk.com.minesweeper.views.adapters;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.models.NavDrawerHeaderInfo;

/**
 * Created by Drew on 5/14/2015.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private NavDrawerHeaderInfo headerInfo;
    private String[] mNavTitles;
    private int[] mNavIcons;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        int holderID;
        // Header
        ImageView mCoverLayout;
        View mCoverOverlay;
        TextView mNameView;
        TextView mEmailView;
        ImageView mAvatarView;
        // Item
        TextView itemView;

        public ViewHolder(View v, int viewType) {
            super(v);

            if(viewType == TYPE_HEADER) {
                mCoverLayout = (ImageView) v.findViewById(R.id.cover);
                mCoverOverlay = v.findViewById(R.id.coverOverlay);
                mAvatarView = (ImageView) v.findViewById(R.id.avatar);
                mNameView = (TextView) v.findViewById(R.id.name);
                mEmailView = (TextView) v.findViewById(R.id.email);
            } else {
                itemView = (TextView) v;
            }
            holderID = viewType;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NavDrawerAdapter(NavDrawerHeaderInfo headerInfo, String[] navItemTitles, int[] navItemIcons) {
        this.headerInfo = headerInfo;
        mNavTitles = navItemTitles;
        mNavIcons = navItemIcons;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NavDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v("adapter", "" + viewType);
        // create a new view
        View v;
        if(viewType == TYPE_HEADER) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.nav_header, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_nav_item, parent, false);
        }
        return new ViewHolder(v, viewType);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(holder.holderID == TYPE_HEADER) {
            holder.mNameView.setText(headerInfo.getName());
            holder.mEmailView.setText(headerInfo.getEmail());
            holder.mAvatarView.setImageBitmap(headerInfo.getAvatar());
            holder.mCoverLayout.setImageBitmap(headerInfo.getCover());
        } else {
            holder.itemView.setText(mNavTitles[position - 1]);
            holder.itemView.setCompoundDrawablesWithIntrinsicBounds(mNavIcons[position - 1], 0, 0, 0);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mNavTitles.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return TYPE_HEADER;
        return TYPE_ITEM;
    }
}
