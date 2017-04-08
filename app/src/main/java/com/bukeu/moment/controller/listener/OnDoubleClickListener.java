package com.bukeu.moment.controller.listener;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Max on 2015/3/29.
 */
public abstract class OnDoubleClickListener implements View.OnTouchListener {

    private final int DOUBLE_TAP_TIMEOUT = 200;
    private  MotionEvent mCurrentDownEvent;
    private  MotionEvent mPreviousUpEvent;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mPreviousUpEvent != null
                    && mCurrentDownEvent != null
                    && isConsideredDoubleTap(mCurrentDownEvent,
                    mPreviousUpEvent, event)) {
                onDoubleClick(v);
            }
            mCurrentDownEvent = MotionEvent.obtain(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mPreviousUpEvent = MotionEvent.obtain(event);
        }
        return true;
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown,
                                                 MotionEvent firstUp, MotionEvent secondDown) {
        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }
        int deltaX = (int) firstUp.getX() - (int) secondDown.getX();
        int deltaY = (int) firstUp.getY() - (int) secondDown.getY();
        return deltaX * deltaX + deltaY * deltaY < 10000;
    }

    public abstract void onDoubleClick(View v);
}
