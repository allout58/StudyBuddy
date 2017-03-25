package edu.clemson.six.assignment4.controller;

import android.content.Intent;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import edu.clemson.six.assignment4.OnStartDragListener;
import edu.clemson.six.assignment4.R;
import edu.clemson.six.assignment4.databinding.CarViewBinding;
import edu.clemson.six.assignment4.model.Car;
//import edu.clemson.six.assignment4.view.EditCarActivity;
//import edu.clemson.six.assignment4.view.MainActivity;
import edu.clemson.six.assignment4.view.helper.ItemTouchHelperAdapter;

/**
 * Created by jthollo on 2/5/2017.
 */

public class CarListAdapter extends RecyclerView.Adapter<CarListAdapter.CarViewHolder> implements ItemTouchHelperAdapter {

    private static CarListAdapter ourInstance = new CarListAdapter();

    public OnStartDragListener dragListener;

    private CarListAdapter() {
    }

    public static CarListAdapter getInstance() {
        return ourInstance;
    }

    public void setOnStartDragListener(OnStartDragListener dragListener) {
        this.dragListener = dragListener;
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CarViewBinding binding = CarViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        Log.d("CarListAdapter", "Creating new viewholder vt:" + viewType + " parent: " + parent);
        return new CarViewHolder(binding, parent);
    }

    @Override
    public void onBindViewHolder(final CarViewHolder holder, int position) {
        final Car c = CarController.getInstance().get(position);
        Log.d("CarListAdapter", "Size: " + CarController.getInstance().size() + ", pos: " + position + ", name: " + c.getMake() + c.getModel() + ", isDeleted: " + String.valueOf(c.isDeleted()));
        holder.binding.setCar(c);
        holder.binding.executePendingBindings();
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CarController.getInstance().isTrashExportMode()) {
//                    Intent intent = new Intent(holder.parent.getContext(), EditCarActivity.class);
//                    intent.putExtra(MainActivity.INTENT_CAR_ID, c.getId());
//                    intent.putExtra(MainActivity.INTENT_CAR_ORDER, holder.getAdapterPosition());
//                    holder.parent.getContext().startActivity(intent);
                }
            }
        });
        holder.binding.getRoot().findViewById(R.id.viewHandle).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    if (dragListener != null) {
                        dragListener.onStartDrag(holder);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return CarController.getInstance().size();
    }

    @Override
    public void onItemDismiss(int position) {
        if (CarController.getInstance().isTrashExportMode()) {
            CarController.getInstance().unDelete(CarController.getInstance().get(position));
        }
        else {
            CarController.getInstance().remove(position);
        }
//        notifyDataSetChanged();
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (CarController.getInstance().isTrashExportMode()) return false;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                CarController.getInstance().swap(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                CarController.getInstance().swap(i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        public CarViewBinding binding;
        public ViewGroup parent;

        public CarViewHolder(CarViewBinding binding, ViewGroup parent) {
            super(binding.getRoot());
            this.binding = binding;
            this.parent = parent;
        }
    }
}
