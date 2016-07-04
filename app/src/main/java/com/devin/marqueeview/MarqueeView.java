package com.devin.marqueeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>   Title:
 * <p>   Describe:
 * <p>   Created by devin on 2016/6/30.
 * 原作者github：https://github.com/oubowu/MarqueeLayoutLibrary
 */
public class MarqueeView extends ViewGroup {

    /**
     * 方向 从下往上
     */
    public static final int ORIENTATION_TO_TOP = 1;
    /**
     * 方向 从上往下
     */
    public static final int ORIENTATION_TO_BOTTOM = 2;
    /**
     * 方向 从右往左
     */
    public static final int ORIENTATION_TO_LEFT = 3;
    /**
     * 方向 从左往右
     */
    public static final int ORIENTATION_TO_RIGHT = 4;


    /**
     * 当前方向
     */
    private int currentOrientation;
    /**
     * 切换的间隔时间
     */
    private int intervalTime;
    /**
     * 一次滚动的时间
     */
    private int scrollTime;
    /**
     * 是否开启透明动画
     */
    private boolean enableAlphaAnim;
    /**
     * 是否开始缩放动画
     */
    private boolean enableScaleAnim;


    private Scroller scroller;

    private int scrollDistance;

    private int currentPosition;

    private Timer timer;

    private int itemCount;

    private boolean isStart;

    private OnItemClickListener mOnItemClickListener;

    private MarqueeViewAdapter adapter;


    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeView);
        intervalTime = typedArray.getInt(R.styleable.MarqueeView_intervalTime, 2000);
        currentOrientation = typedArray.getInt(R.styleable.MarqueeView_orientation, ORIENTATION_TO_TOP);
        scrollTime = typedArray.getInt(R.styleable.MarqueeView_scrollTime, 1000);
        enableAlphaAnim = typedArray.getBoolean(R.styleable.MarqueeView_enableAlphaAnim, false);
        enableScaleAnim = typedArray.getBoolean(R.styleable.MarqueeView_enableScaleAnim, false);
        typedArray.recycle();
        scroller = new Scroller(context, new AccelerateDecelerateInterpolator());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // 声明临时变量存储父容器的期望值
        int parentDesireHeight = 0;
        int parentDesireWidth = 0;

        int tmpWidth = 0;
        int tmpHeight = 0;

        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                // 获取子元素的布局参数
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                // 测量子元素并考虑外边距
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                // 计算父容器的期望值
                parentDesireWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                // 取子控件最大宽度
                tmpWidth = Math.max(tmpWidth, parentDesireWidth);
                parentDesireHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                // 取子控件最大高度
                tmpHeight = Math.max(tmpHeight, parentDesireHeight);
            }
            parentDesireWidth = tmpWidth;
            parentDesireHeight = tmpHeight;
            // 考虑父容器内边距
            parentDesireWidth += getPaddingLeft() + getPaddingRight();
            parentDesireHeight += getPaddingTop() + getPaddingBottom();
            // Log.e("TAG", "MarqueeLayout-100行-onMeasure(): " + parentDesireWidth + ";" + parentDesireHeight + ";" + getSuggestedMinimumWidth() + ";" + getSuggestedMinimumHeight());
            // 尝试比较建议最小值和期望值的大小并取大值
            parentDesireWidth = Math.max(parentDesireWidth, getSuggestedMinimumWidth());
            parentDesireHeight = Math.max(parentDesireHeight, getSuggestedMinimumHeight());
        }
        // 设置最终测量值
        setMeasuredDimension(resolveSize(parentDesireWidth, widthMeasureSpec), resolveSize(parentDesireHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 声明一个临时变量存储高度倍增值
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        if (currentOrientation == ORIENTATION_TO_BOTTOM || currentOrientation == ORIENTATION_TO_TOP) {
            final int height = getMeasuredHeight();
            scrollDistance = height;
            int multiHeight = 0;
            // 垂直方向跑马灯
            for (int i = 0; i < getChildCount(); i++) {
                // 遍历子元素并对其进行定位布局
                final View child = getChildAt(i);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                if (i == 0 && multiHeight == 0 && currentOrientation == ORIENTATION_TO_BOTTOM) {
                    multiHeight = -height;
                    currentPosition = 1;
                }
                // 垂直方向的话，因为布局高度定死为子控件最大的高度，所以子控件一律位置垂直居中，paddingTop和marginTop均失效
                child.layout(paddingLeft + lp.leftMargin, (height - child.getMeasuredHeight()) / 2 + multiHeight, child.getMeasuredWidth() + paddingLeft + lp.leftMargin,
                        (height - child.getMeasuredHeight()) / 2 + child.getMeasuredHeight() + multiHeight);
                multiHeight += height;
            }
        } else {
            final int width = getMeasuredWidth();
            scrollDistance = width;
            int multiWidth = 0;
            // 水平方向跑马灯
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                if (i == 0 && multiWidth == 0 && currentOrientation == ORIENTATION_TO_RIGHT) {
                    multiWidth = -width;
                    currentPosition = 1;
                }
                // 水平方向，因为布局宽度定死为子控件最大的宽度，所以子控件一律位置水平居中，paddingLeft和marginLeft均失效
                child.layout((width - child.getMeasuredWidth()) / 2 + multiWidth, paddingTop + lp.topMargin,
                        (width - child.getMeasuredWidth()) / 2 + child.getMeasuredWidth() + multiWidth, child.getMeasuredHeight() + paddingTop + lp.topMargin);
                multiWidth += width;
            }
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (currentOrientation == ORIENTATION_TO_BOTTOM || currentOrientation == ORIENTATION_TO_TOP) {
                scrollTo(0, scroller.getCurrY());
                handleScrollAnim();
            } else {
                scrollTo(scroller.getCurrX(), 0);
                handleScrollAnim();
            }
            invalidate();
        } else if (timer != null) {
            switch (currentOrientation) {
                case ORIENTATION_TO_TOP:
                    if (currentPosition >= itemCount - 1) {
                        // 滚动到最后一个时，迅速回到第一个，造成轮播的假象
                        fastScroll(-currentPosition * scrollDistance);
                        currentPosition = 0;
                    }
                    break;
                case ORIENTATION_TO_BOTTOM:
                    if (currentPosition <= 0) {
                        fastScroll((itemCount - 1) * scrollDistance);
                        currentPosition = itemCount - 1;
                    }
                    break;
                case ORIENTATION_TO_LEFT:
                    if (currentPosition >= itemCount - 1) {
                        fastScroll(-currentPosition * scrollDistance);
                        currentPosition = 0;
                    }
                    break;
                case ORIENTATION_TO_RIGHT:
                    if (currentPosition <= 0) {
                        fastScroll((itemCount - 1) * scrollDistance);
                        currentPosition = itemCount - 1;
                    }
                    break;
            }
            invalidate();
        }
    }

    private void smoothScroll(int distance) {
        if (currentOrientation == ORIENTATION_TO_BOTTOM || currentOrientation == ORIENTATION_TO_TOP) {
            scroller.startScroll(0, scroller.getFinalY(), 0, distance, scrollTime);
        } else {
            scroller.startScroll(scroller.getFinalX(), 0, distance, 0, scrollTime);
        }
    }

    private void fastScroll(int distance) {
        if (currentOrientation == ORIENTATION_TO_BOTTOM || currentOrientation == ORIENTATION_TO_TOP) {
            scroller.startScroll(0, scroller.getFinalY(), 0, distance, 0);
        } else {
            scroller.startScroll(scroller.getFinalX(), 0, distance, 0, 0);
        }
    }

    private void handleScrollAnim() {
        if (!enableAlphaAnim && !enableScaleAnim) {
            return;
        }

        float rate = 0;
        boolean notReachBorder = false;
        int relativeChildPosition = 0;

        switch (currentOrientation) {
            case ORIENTATION_TO_TOP:
                rate = (scroller.getCurrY() - scroller.getStartY()) * 1.0f / (scroller.getFinalY() - scroller.getStartY()) / 2.0f + 0.5f;
                notReachBorder = currentPosition != 0;
                relativeChildPosition = currentPosition - 1;
                break;
            case ORIENTATION_TO_BOTTOM:
                rate = (scroller.getCurrY() - scroller.getStartY()) * 1.0f / (scroller.getFinalY() - scroller.getStartY()) / 2.0f + 0.5f;
                notReachBorder = currentPosition != itemCount - 1;
                relativeChildPosition = currentPosition + 1;
                break;
            case ORIENTATION_TO_LEFT:
                rate = (scroller.getCurrX() - scroller.getStartX()) * 1.0f / (scroller.getFinalX() - scroller.getStartX()) / 2.0f + 0.5f;
                notReachBorder = currentPosition != 0;
                relativeChildPosition = currentPosition - 1;
                break;
            case ORIENTATION_TO_RIGHT:
                rate = (scroller.getCurrX() - scroller.getStartX()) * 1.0f / (scroller.getFinalX() - scroller.getStartX()) / 2.0f + 0.5f;
                notReachBorder = currentPosition != itemCount - 1;
                relativeChildPosition = currentPosition + 1;
                break;
        }

        if (notReachBorder) {
            playAnim(getChildAt(currentPosition), enableAlphaAnim, enableScaleAnim, rate);
            playAnim(getChildAt(relativeChildPosition), enableAlphaAnim, enableScaleAnim, 1.5f - rate);
        } else {
            playAnim(getChildAt(currentPosition), enableAlphaAnim, enableScaleAnim, 1);
        }

    }

    private void playAnim(View view, boolean enableAlphaAnim, boolean enableScaleAnim, float rate) {
        if (enableAlphaAnim) {
            ViewCompat.setAlpha(view, rate);
        }
        if (enableScaleAnim) {
            ViewCompat.setScaleX(view, rate);
            ViewCompat.setScaleY(view, rate);
        }
    }

    // 生成默认的布局参数
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    // 生成布局参数,将布局参数包装成我们的
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    // 生成布局参数,从属性配置中生成我们的布局参数
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    // 查当前布局参数是否是我们定义的类型这在code声明布局参数时常常用到
    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }


    private class SwitchTimerTask extends TimerTask {

        @Override
        public void run() {
            switch (currentOrientation) {
                case ORIENTATION_TO_TOP:
                    currentPosition++;
                    if (currentPosition >= itemCount) {
                        currentPosition = 0;
                        fastScroll(-getScrollY());
                    } else {
                        smoothScroll(scrollDistance);
                    }
                    postInvalidate();
                    break;
                case ORIENTATION_TO_BOTTOM:
                    currentPosition--;
                    if (currentPosition < 0) {
                        currentPosition = 1;
                        fastScroll(-getScrollY());
                    } else {
                        smoothScroll(-scrollDistance);
                    }
                    postInvalidate();
                    break;
                case ORIENTATION_TO_LEFT:
                    currentPosition++;
                    if (currentPosition >= itemCount) {
                        currentPosition = 0;
                        fastScroll(-getScrollX());
                    } else {
                        smoothScroll(scrollDistance);
                    }
                    postInvalidate();
                    break;
                case ORIENTATION_TO_RIGHT:
                    currentPosition--;
                    if (currentPosition < 0) {
                        currentPosition = 1;
                        fastScroll(-getScrollX());
                    } else {
                        smoothScroll(-scrollDistance);
                    }
                    postInvalidate();
                    break;

            }

        }
    }

    /**
     * 开始轮播
     */
    public void start() {

        stop();

        if (getChildCount() <= 1 || timer != null) {
            // 小于等于1没必要轮播
            return;
        }
        isStart = true;
        timer = new Timer();
        timer.schedule(new SwitchTimerTask(), intervalTime, intervalTime);
    }

    /**
     * 停止轮播
     */
    public void stop() {
        if (timer == null) {
            return;
        }
        isStart = false;
        timer.cancel();
        timer.purge();
        timer = null;
    }


    public void setAdapter(MarqueeViewAdapter adapter) {

        this.adapter = adapter;

        adapter.setOnDataChangeListener(new MarqueeViewAdapter.OnDataChangeListener() {
            @Override
            public void dataChange() {
                initChildView();
            }
        });

        initChildView();

    }


    /**
     * 初始化子view
     */
    private void initChildView() {

        removeAllViews();

        for (int i = 0; i < adapter.getCount(); i++) {
            final View view = adapter.getView(this, i, adapter.getItem(i));
            addView(view);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(MarqueeView.this, view, finalI);
                    }
                }
            });

        }


        if (adapter.getCount() == 1) {
            scroller.forceFinished(true);
            scrollTo(0, 0);
        } else if (adapter.getCount() > 1) {
            View view;

            switch (currentOrientation) {
                case MarqueeView.ORIENTATION_TO_TOP:
                case MarqueeView.ORIENTATION_TO_LEFT:

                    view = adapter.getView(this, 0, adapter.getItem(0));
                    addView(view);

                    break;
                case MarqueeView.ORIENTATION_TO_BOTTOM:
                case MarqueeView.ORIENTATION_TO_RIGHT:

                    view = adapter.getView(this, adapter.getCount() - 1, adapter.getItem(adapter.getCount() - 1));
                    addView(view);
                    break;
            }
        }

        itemCount = getChildCount();

        start();

    }


    public interface OnItemClickListener {

        void onItemClick(MarqueeView parent, View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        mOnItemClickListener = listener;

    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            carryOn();
        } else {
            pause();
        }
    }

    private void carryOn() {
        if (isStart && timer == null) {
            timer = new Timer();
            timer.schedule(new SwitchTimerTask(), intervalTime, intervalTime);
        }
    }

    private void pause() {
        if (isStart && timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


}
