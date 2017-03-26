package edu.clemson.six.studybuddy.view.helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import java.util.ArrayList;
import java.util.List;

import edu.clemson.six.studybuddy.controller.CarListAdapter;

/**
 * Created by James Hollowell on 2/8/2017.
 */

public class SwipeHelper extends ItemTouchHelper.Callback {
    // Super janky event queue
    private List<ItemTouchHelperAdapter> adapterList = new ArrayList<>();

    public SwipeHelper() {
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof CarListAdapter.CarViewHolder) {
                CarListAdapter.CarViewHolder vh = (CarListAdapter.CarViewHolder) viewHolder;
                vh.binding.getRoot().animate().alpha(0.75f).setDuration(500);
            }
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof CarListAdapter.CarViewHolder) {
            CarListAdapter.CarViewHolder vh = (CarListAdapter.CarViewHolder) viewHolder;
            vh.binding.getRoot().animate().alpha(1).setDuration(500);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        boolean status = true;
        for (ItemTouchHelperAdapter adp :
                adapterList) {
            status &= adp.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return status;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        for (ItemTouchHelperAdapter adp :
                adapterList) {
            adp.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

    public void addAdapter(ItemTouchHelperAdapter adp) {
        adapterList.add(adp);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }
}
