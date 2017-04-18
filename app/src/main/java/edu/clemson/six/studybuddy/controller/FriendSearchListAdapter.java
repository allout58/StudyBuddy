package edu.clemson.six.studybuddy.controller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.databinding.FriendSearchViewBinding;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.view.component.CircleTransform;

/**
 * Created by jthollo on 4/10/2017.
 */

public class FriendSearchListAdapter extends RecyclerView.Adapter<FriendSearchListAdapter.FriendViewHolder> {

    public final List<Friend> friendList = new ArrayList<>();

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FriendSearchViewBinding binding = FriendSearchViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FriendViewHolder(parent, binding);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, final int position) {
        final Friend f = friendList.get(position);
        holder.binding.setFriend(f);
        ImageView v = (ImageView) holder.binding.getRoot().findViewById(R.id.imageViewFriend);
        Button btn = (Button) holder.binding.getRoot().findViewById(R.id.btnAddFriend);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Connor, do whatever else you want to do when we click add in the search
                FriendController.getInstance().newFriend(f);
                v.setVisibility(View.GONE);
            }
        });
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
        return friendList.size();
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
