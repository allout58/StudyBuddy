package edu.clemson.six.studybuddy.controller.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.FriendController;
import edu.clemson.six.studybuddy.databinding.FriendListingViewBinding;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.view.component.CircleTransform;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.FriendViewHolder> {
    private static final String TAG = "HomePageAdapter";
    private static final HomePageAdapter instance = new HomePageAdapter();

    private HomePageAdapter() {
    }

    public static HomePageAdapter getInstance() {
        return instance;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FriendListingViewBinding binding = FriendListingViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FriendViewHolder(parent, binding);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, final int position) {
        final Friend f = FriendController.getInstance().getNearby()[position];
        Log.d(TAG, String.format("Friend: RN %s ET %s", f.getName(), f.getEndTime()));
        holder.binding.setFriend(f);
        holder.binding.setIsMine(false);
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
    public int getItemCount() {
        return FriendController.getInstance().getNearbyCount();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        public final ViewGroup parent;
        public final FriendListingViewBinding binding;

        public FriendViewHolder(ViewGroup parent, FriendListingViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.parent = parent;
        }
    }
}