package com.ltan.layoutmanager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * My Application.com.ltan.layoutmanager
 *
 * @ClassName: CustomLayoutManager
 * @Description:
 * @Author: tanlin
 * @Date: 2019-10-14
 * @Version: 1.0
 */
public class CustomLayoutManager extends RecyclerView.LayoutManager {

    private static final boolean DEBUG = true;
    private static final String TAG = "ltan/MyLayoutManager";
    private static final int MAX_VERTICAL_COUNT = 4;
    private Context mContext;
    private int mItemViewWidth;
    private int mItemViewHeight;
    private int mScrollX = 0;
    private int mScrollOffset = Integer.MAX_VALUE;
    private int mItemCount;

    private boolean mScrollVertical;
    private OrientationHelper mOrientationHelper;

    private float widthScale = 0.9f;
    private float heightScale = 1.5f;

    public CustomLayoutManager(Context context) {
        this(context, false);
    }

    public CustomLayoutManager(Context context, boolean vertical) {
        mContext = context;
        mScrollVertical = vertical;
        mOrientationHelper = OrientationHelper.createOrientationHelper(this,
                mScrollVertical ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        removeAndRecycleAllViews(recycler);
        recycler.clear();
    }

    @Override
    public boolean canScrollVertically() {
        return mScrollVertical;
    }

    @Override
    public boolean canScrollHorizontally() {
        return !mScrollVertical;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollOffset + dy;
        mScrollOffset = Math.min(Math.max(mItemViewHeight, mScrollOffset + dy), mItemCount * mItemViewHeight);
        layoutChildVertical(recycler);
        return mScrollOffset - pendingScrollOffset + dy;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollX + dx;
        int lastPage = mItemCount % MAX_VERTICAL_COUNT == 0 ? 1 : 0;
        int maxOffset = (mItemCount / MAX_VERTICAL_COUNT - lastPage) * mItemViewWidth;
        mScrollX = Math.min(Math.max(0, pendingScrollOffset), maxOffset);
        layoutChildHorizontal(recycler, state);
        return mScrollX - pendingScrollOffset + dx;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || state.isPreLayout()) {
            return;
        }
        mItemViewWidth = (int) (getHorizontalSpace() * widthScale);
        if (mScrollVertical) {
            mItemViewHeight = (int) (mItemViewWidth * heightScale);
        } else {
            mItemViewHeight = getVerticalSpace() / MAX_VERTICAL_COUNT;
        }
        mItemCount = getItemCount();
        if (mScrollVertical) {
            mScrollOffset = Math.min(Math.max(mItemViewHeight, mScrollOffset), mItemCount * mItemViewHeight);
            layoutChildVertical(recycler);
        } else {
            layoutChildHorizontal(recycler, state);
        }
    }

    /**
     * @param recycler recycler
     * @param state    s
     */
    private void layoutChildHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            return;
        }
        int remainSpace = getVerticalSpace();
        // mItemViewWidth = 200, mScrollX = 10 ----> left = 10
        // mItemViewWidth = 200, mScrollX = mItemViewWidth + 2 ----> left = 2
        int itemScrollOffset = mScrollX % mItemViewWidth;
        ArrayList<ItemViewInfoH> layoutInfos = new ArrayList<>();
        final int childCount = getChildCount();
        /*
         * item-1- +++ -item-5
         * item-2- +++ -item-6
         * item-3- +++ -item-7
         * item-4- +++ -null
         */
        int containerCount = MAX_VERTICAL_COUNT * 2;
        for (int i = 0; i < containerCount && i < mItemCount; i++) {
            // 1234前4个的top等于mItemViewHeight * position，后4个item的top又从顶部开始排
            int top = mItemViewHeight * (i % MAX_VERTICAL_COUNT);
            // 1234前4个item的左边为0，后4个item的left为 mItemViewWidth
            int leftTmp = i < MAX_VERTICAL_COUNT ? 0 : mItemViewWidth;
            // 横向滑动后，left跟随scrollX
            int left = leftTmp - itemScrollOffset;

            ItemViewInfoH info = new ItemViewInfoH(top, left);
            layoutInfos.add(info);
            remainSpace -= mItemViewHeight;
            if (remainSpace <= 0) {
                if (DEBUG) {
                    Log.d(TAG, "layoutChildHorizontal: remain space less than 0, " + remainSpace);
                }
            }
        }

        int infoSize = layoutInfos.size();
        final int startPos = (int) Math.floor(mScrollX / mItemViewWidth) * MAX_VERTICAL_COUNT;
        final int endPos = (startPos + infoSize) > mItemCount ? mItemCount - 1 : startPos + infoSize - 1;
        // more than one screen
        if (startPos + infoSize > mItemCount) {
            for (int i = startPos + infoSize - 1; i > endPos; i--) {
                layoutInfos.remove(--infoSize);
            }
            if (DEBUG) {
                Log.d(TAG, "layoutChildHorizontal: after fix item count: " + infoSize + " to " + layoutInfos);
            }
        }
        // recycle items outside of screen
        int layoutCount = layoutInfos.size();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            assert childView != null;
            int pos = getPosition(childView);
            if (pos > endPos || pos < startPos) {
                removeAndRecycleView(childView, recycler);
            }
        }
        detachAndScrapAttachedViews(recycler);
        // arrange items
        for (int i = 0; i < layoutCount; i++) {
            View view = recycler.getViewForPosition(startPos + i);
            ItemViewInfoH layoutInfo = layoutInfos.get(i);
            addView(view);
            measureChildWithExactlySize(view);
            // layout: [left, top, right, bottom]
            int top = layoutInfo.top;
            int left = layoutInfo.left;
            int right = left + mItemViewWidth;
            int bottom = top + mItemViewHeight;
            layoutDecoratedWithMargins(view, left, top, right, bottom);
        }
        // todo orientation helper to recycle the items
        // ref: LinearLayoutManager
    }

    /**
     * https://github.com/DingMouRen/LayoutManagerGroup/
     * file: src/main/java/com/dingmouren/layoutmanagergroup/echelon/EchelonLayoutManager.java
     *
     * @param recycler recycler
     */
    private void layoutChildVertical(RecyclerView.Recycler recycler) {
        if (mItemCount == 0) {
            return;
        }
        int bottomItemPosition = (int) Math.floor(mScrollOffset / mItemViewHeight);
        int remainSpace = getVerticalSpace() - mItemViewHeight;

        int bottomItemVisibleHeight = mScrollOffset % mItemViewHeight;
        final float offsetPercentRelativeToItemView = bottomItemVisibleHeight * 1.0f / mItemViewHeight;

        ArrayList<ItemViewInfo> layoutInfos = new ArrayList<>();
        for (int i = bottomItemPosition - 1, j = 1; i >= 0; i--, j++) {
            double maxOffset = (getVerticalSpace() - mItemViewHeight) / 2 * Math.pow(0.8, j);
            int start = (int) (remainSpace - offsetPercentRelativeToItemView * maxOffset);
            float mScale = 0.9f;
            float scaleXY = (float) (Math.pow(mScale, j - 1) * (1 - offsetPercentRelativeToItemView * (1 - mScale)));
            float positionOffset = offsetPercentRelativeToItemView;
            float layoutPercent = start * 1.0f / getVerticalSpace();
            ItemViewInfo info = new ItemViewInfo(start, scaleXY, positionOffset, layoutPercent);
            layoutInfos.add(0, info);
            remainSpace = (int) (remainSpace - maxOffset);
            if (remainSpace <= 0) {
                info.setTop((int) (remainSpace + maxOffset));
                info.setPositionOffset(0);
                info.setLayoutPercent(info.getTop() / getVerticalSpace());
                info.setScaleXY((float) Math.pow(mScale, j - 1));
                break;
            }
        }
        if (bottomItemPosition < mItemCount) {
            final int start = getVerticalSpace() - bottomItemVisibleHeight;
            layoutInfos.add(new ItemViewInfo(start, 1.0f, bottomItemVisibleHeight * 1.0f / mItemViewHeight, start * 1.0f / getVerticalSpace()).setIsBottom());
        } else {
            bottomItemPosition = bottomItemPosition - 1;//99
        }

        int layoutCount = layoutInfos.size();
        final int startPos = bottomItemPosition - (layoutCount - 1);
        final int endPos = bottomItemPosition;
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int pos = getPosition(childView);
            if (pos > endPos || pos < startPos) {
                removeAndRecycleView(childView, recycler);
            }
        }
        detachAndScrapAttachedViews(recycler);

        for (int i = 0; i < layoutCount; i++) {
            View view = recycler.getViewForPosition(startPos + i);
            ItemViewInfo layoutInfo = layoutInfos.get(i);
            addView(view);
            measureChildWithExactlySize(view);
            int left = (getHorizontalSpace() - mItemViewWidth) / 2;
            layoutDecoratedWithMargins(view, left, layoutInfo.getTop(), left + mItemViewWidth, layoutInfo.getTop() + mItemViewHeight);
            view.setPivotX(view.getWidth() / 2);
            view.setPivotY(0);
            view.setScaleX(layoutInfo.getScaleXY());
            view.setScaleY(layoutInfo.getScaleXY());
        }
    }

    private void measureChildWithExactlySize(View child) {
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(mItemViewWidth, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(mItemViewHeight, View.MeasureSpec.UNSPECIFIED);
        child.measure(widthSpec, heightSpec);
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    /**
     * horizontal item
     */
    static class ItemViewInfoH {
        public int top;
        public int left;

        public ItemViewInfoH(int top, int left) {
            this.top = top;
            this.left = left;
        }
    }

    /**
     * vertical scale item info
     */
    static class ItemViewInfo {
        private float mScaleXY;
        private float mLayoutPercent;
        private float mPositionOffset;
        private int mTop;
        private boolean mIsBottom;

        public ItemViewInfo(int top, float scaleXY, float positonOffset, float percent) {
            this.mTop = top;
            this.mScaleXY = scaleXY;
            this.mPositionOffset = positonOffset;
            this.mLayoutPercent = percent;
        }

        public ItemViewInfo setIsBottom() {
            mIsBottom = true;
            return this;
        }

        public float getScaleXY() {
            return mScaleXY;
        }

        public void setScaleXY(float mScaleXY) {
            this.mScaleXY = mScaleXY;
        }

        public float getLayoutPercent() {
            return mLayoutPercent;
        }

        public void setLayoutPercent(float mLayoutPercent) {
            this.mLayoutPercent = mLayoutPercent;
        }

        public float getPositionOffset() {
            return mPositionOffset;
        }

        public void setPositionOffset(float mPositionOffset) {
            this.mPositionOffset = mPositionOffset;
        }

        public int getTop() {
            return mTop;
        }

        public void setTop(int mTop) {
            this.mTop = mTop;
        }
    }
}
