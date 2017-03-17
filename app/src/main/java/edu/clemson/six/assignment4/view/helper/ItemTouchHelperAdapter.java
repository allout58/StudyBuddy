package edu.clemson.six.assignment4.view.helper;

/**
 * Created by James Hollowell on 2/8/2017.
 */

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

}
