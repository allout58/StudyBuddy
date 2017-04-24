package edu.clemson.six.studybuddy.controller.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.FriendController;
import edu.clemson.six.studybuddy.controller.SyncController;
import edu.clemson.six.studybuddy.databinding.FriendListingViewBinding;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.view.component.CircleTransform;

public class FriendsListAdapter extends SectionedRecyclerViewAdapter<FriendsListAdapter.FriendViewHolder> {
    public static final int SECTION_NEAR = 0;
    public static final int SECTION_REQUEST = 1;
    public static final int SECTION_OTHER = 2;
    public static final int SECTION_PENDING = 3;
    private static final FriendsListAdapter instance = new FriendsListAdapter();

    private FriendsListAdapter() {
    }

    public static FriendsListAdapter getInstance() {
        return instance;
    }

    @Override
    public int getSectionCount() {
        return 4;
    }

    @Override
    public int getItemCount(int section) {
        switch (section) {
            case SECTION_NEAR:
                // TODO Determine nearby friends
                return 0;
            case SECTION_OTHER:
                return FriendController.getInstance().getFriendsCount();
            case SECTION_PENDING:
                return FriendController.getInstance().getMyRequestsCount();
            case SECTION_REQUEST:
                return FriendController.getInstance().getRequestsCount();
        }
        return 0;
    }

    @Override
    public void onBindHeaderViewHolder(FriendViewHolder holder, int section) {
        int sID = -1;
        switch (section) {
            case SECTION_NEAR:
                sID = R.string.section_near;
                break;
            case SECTION_OTHER:
                sID = R.string.section_other;
                break;
            case SECTION_REQUEST:
                sID = R.string.section_request;
                break;
            case SECTION_PENDING:
                sID = R.string.section_pending;
        }
        ((TextView) holder.parent.findViewById(R.id.txtViewHeader)).setText(sID);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int section, int relativePosition, final int absolutePosition) {
        final Friend f;
        boolean isMine = false;
        switch (section) {
            case SECTION_OTHER:
                f = FriendController.getInstance().getFriends()[relativePosition];
                break;
            case SECTION_REQUEST:
                f = FriendController.getInstance().getRequests()[relativePosition];
                break;
            case SECTION_PENDING:
                f = FriendController.getInstance().getMyRequests()[relativePosition];
                isMine = true;
                break;
            default:
                f = null;
        }
        Button btn = (Button) holder.binding.getRoot().findViewById(R.id.btnConfirmFriend);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendController.getInstance().confirmFriend(f);
                v.setVisibility(View.GONE);
            }
        });

        btn = (Button) holder.binding.getRoot().findViewById(R.id.btnDeleteFriend);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendController.getInstance().deleteFriend(f);
                SyncController.getInstance().syncFriends(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });

        holder.binding.setFriend(f);
        holder.binding.setIsMine(isMine);
        ImageView v = (ImageView) holder.binding.getRoot().findViewById(R.id.imageViewFriend);
        if (!f.getImageURL().isEmpty())
            Picasso.with(holder.parent.getContext())
                    .load(f.getImageURL())
                    .resizeDimen(R.dimen.person_view_size, R.dimen.person_view_size)
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_person_white_150dp)
                    .into(v);
        else
            Picasso.with(holder.parent.getContext())
                    .load(R.drawable.ic_person_white_150dp)
                    .resizeDimen(R.dimen.person_view_size, R.dimen.person_view_size)
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_person_white_150dp)
                    .into(v);
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyler_header, parent, false);
            return new FriendViewHolder(v);
        } else {
            FriendListingViewBinding binding = FriendListingViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FriendViewHolder(binding, parent);
        }
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        public final FriendListingViewBinding binding;
        public final ViewGroup parent;

        public FriendViewHolder(View view) {
            super(view);
            this.parent = (ViewGroup) view;
            this.binding = null;
        }

        public FriendViewHolder(FriendListingViewBinding binding, ViewGroup parent) {
            super(binding.getRoot());
            this.binding = binding;
            this.parent = parent;
        }
    }
}
