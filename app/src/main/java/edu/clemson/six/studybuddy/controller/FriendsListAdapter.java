package edu.clemson.six.studybuddy.controller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.databinding.FriendListingViewBinding;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;
import edu.clemson.six.studybuddy.view.component.CircleTransform;

public class FriendsListAdapter extends SectionedRecyclerViewAdapter<FriendsListAdapter.FriendViewHolder> {
    public static final int SECTION_NEAR = 0;
    public static final int SECTION_OTHER = 1;
    public static final int SECTION_REQUEST = 2;
    private static final FriendsListAdapter instance = new FriendsListAdapter();

    private FriendsListAdapter() {
    }

    public static FriendsListAdapter getInstance() {
        return instance;
    }

    @Override
    public int getSectionCount() {
        return 3;
    }

    @Override
    public int getItemCount(int section) {
        switch (section) {
            case SECTION_NEAR:
                return 1;
            case SECTION_OTHER:
                return 5;
            case SECTION_REQUEST:
                return 1;
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
        }
        ((TextView) holder.parent.findViewById(R.id.txtViewHeader)).setText(sID);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int section, int relativePosition, int absolutePosition) {
        Location w = new Location(1, 0, 0, 0, "Watt");
        SubLocation cc = new SubLocation(1, "Command and Control", w);
        Calendar c = Calendar.getInstance();
        c.set(2017, 3, 29, 17, 18);
//        https://lh6.googleusercontent.com/-wvYp75r5oeA/AAAAAAAAAAI/AAAAAAAAAQs/imhoKGVc7Jw/s96-c/photo.jpg
        Friend f = new Friend("00001", "https://lh6.googleusercontent.com/-wvYp75r5oeA/AAAAAAAAAAI/AAAAAAAAAQs/imhoKGVc7Jw/s96-c/photo.jpg", "Jimmy John", w, SubLocation.OTHER, "Bla", c.getTime(), true);
        holder.binding.setFriend(f);
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
