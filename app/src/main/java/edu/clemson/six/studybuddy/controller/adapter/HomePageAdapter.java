package edu.clemson.six.studybuddy.controller.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.FriendController;
import edu.clemson.six.studybuddy.controller.UserLocationController;
import edu.clemson.six.studybuddy.databinding.FriendSearchViewBinding;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.view.component.CircleTransform;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.FriendViewHolder> {

    public final List<Friend> nearbyFriendsList = new ArrayList<>();

    public HomePageAdapter() {
        Location loc = UserLocationController.getInstance().getCurrentLocation();
        if(loc != null)
            for(int i = 0; i<FriendController.getInstance().getFriendsCount(); i++)
                if (loc == FriendController.getInstance().getFriends()[i].getLocation())
                    nearbyFriendsList.add(FriendController.getInstance().getFriends()[i]);
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FriendSearchViewBinding binding = FriendSearchViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FriendViewHolder(parent, binding);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, final int position) {
        final Friend f = nearbyFriendsList.get(position);
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
    public int getItemCount() {
        return nearbyFriendsList.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        public final ViewGroup parent;
        public final FriendSearchViewBinding binding;

        public FriendViewHolder(ViewGroup parent, FriendSearchViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.parent = parent;
        }
    }
}