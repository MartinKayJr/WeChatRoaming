package cn.martinkay.wechatroaming.utils.ui.fling;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.martinkay.wechatroaming.utils.LayoutHelper;


public class SimpleFlingInterceptLayout extends FrameLayout {

    public interface SimpleOnFlingHandler {

        void onFlingLeftToRight();

        void onFlingRightToLeft();
    }

    // no fling
    private static final int FLING_STATE_RESET = 0;
    // potential fling
    private static final int FLING_STATE_LISTEN = 1;
    // fling now
    private static final int FLING_STATE_ATTACHED = 2;

    private SimpleOnFlingHandler mHandler;
    private boolean mInterceptEnabled = true;
    private int mFlingState = 0;
    private final int mThreshold;
    private int mPointDownX = -1;
    private int mPointDownY = -1;

    public SimpleFlingInterceptLayout(@NonNull Context context) {
        this(context, null);
    }

    public SimpleFlingInterceptLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleFlingInterceptLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mThreshold = LayoutHelper.dip2px(context, 10);
    }

    public SimpleFlingInterceptLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mThreshold = LayoutHelper.dip2px(context, 10);
    }

    public void setOnFlingHandler(@Nullable SimpleOnFlingHandler handler) {
        mHandler = handler;
    }

    @Nullable
    public SimpleOnFlingHandler getOnFlingHandler() {
        return mHandler;
    }

    public void setInterceptEnabled(boolean enabled) {
        mInterceptEnabled = enabled;
    }

    public boolean isInterceptEnabled() {
        return mInterceptEnabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return super.onInterceptTouchEvent(ev) || mFlingState == FLING_STATE_ATTACHED;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void handleTouchEvent(MotionEvent ev) {
        SimpleOnFlingHandler h = mHandler;
        if (!mInterceptEnabled || h == null) {
            return;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointDownX = (int) ev.getX();
                mPointDownY = (int) ev.getY();
                mFlingState = FLING_STATE_LISTEN;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mFlingState = FLING_STATE_RESET;
                break;
            case MotionEvent.ACTION_MOVE: {
                if (mFlingState == FLING_STATE_LISTEN) {
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    // if abs(dx) > threshold && abs(dy) < threshold then intercept
                    if (Math.abs(x - mPointDownX) > mThreshold && Math.abs(y - mPointDownY) < mThreshold) {
                        mFlingState = FLING_STATE_ATTACHED;
                    } else if (Math.abs(x - mPointDownX) < mThreshold && Math.abs(y - mPointDownY) > mThreshold) {
                        mFlingState = FLING_STATE_RESET;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mFlingState = FLING_STATE_RESET;
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mFlingState == FLING_STATE_ATTACHED) {
                    int x = (int) ev.getX();
                    if (x > mPointDownX + mThreshold) {
                        h.onFlingLeftToRight();
                    } else if (x < mPointDownX - mThreshold) {
                        h.onFlingRightToLeft();
                    }
                }
                mFlingState = FLING_STATE_RESET;
                break;
            }
            default:
                break;
        }
    }
}
