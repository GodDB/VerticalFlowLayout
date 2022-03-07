package com.godgod.customlayoutmanager

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class VerticalFlowLayoutManager : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /** notifyItemsetChanged 호출 시에 호출되는 메소드 */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)
        if (state.itemCount <= 0) return
        fillBottom(recycler, state)
    }

    private fun fillBottom(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        var parentTop: Int
        val startPosition: Int

        // child count가 0이상 일 때, 현재 그려진 recyclerView child의 마지막 인덱스를 구해, 그 뒤 인덱스를 startIndex로 잡는다.
        if (childCount > 0) {
            val lastChild = getChildAt(childCount - 1) ?: return
            val lastChildPosition = getPosition(lastChild)
            startPosition = lastChildPosition + 1
            val layoutParam = lastChild.layoutParams as ViewGroup.MarginLayoutParams
            parentTop = getDecoratedBottom(lastChild) + layoutParam.bottomMargin
        } else {
            // childCount가 0이라면 최초 draw이기 때문에 startPosition을 0으로 잡는다.
            startPosition = 0
            parentTop = 0
        }

        var prevChildLeft = paddingLeft
        var prevChildTop = parentTop
        for (i in startPosition until state.itemCount) {

            val view = addViewByIndex(recycler, i)

            // 새로 그려질 뷰의 사이즈가 리사이클러뷰의 사이즈를 넘긴다면 다음 줄로 변경한다.
            if (prevChildLeft + view.measuredWidth > width) {
                prevChildTop += view.measuredHeight
                prevChildLeft = view.paddingLeft
            }

            val left = prevChildLeft
            val right = prevChildLeft + view.measuredWidth
            val bottom = prevChildTop + view.measuredHeight

            if (prevChildTop > height) {
                removeView(view)
                return
            }

            layoutDecoratedWithMargins(view, left, prevChildTop, right, bottom)
            prevChildLeft = right
        }
    }

    private fun addViewByIndex(recycler: RecyclerView.Recycler, index: Int): View {
        return recycler.getViewForPosition(index).also {
            addView(it)
            measureChildWithMargins(it, 0, 0)
        }
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        return scrollBy(dy, recycler, state)
    }

    private fun scrollBy(delta: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (childCount == 0 || delta == 0) return 0

        val topView = getChildAt(0) ?: return 0
        val bottomView = getChildAt(childCount - 1) ?: return 0

        val viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView)

        if (viewSpan < getVerticalSpace()) return 0

        var newDelta: Int
        val maxItemCount = state.itemCount

        // scroll up
        if (delta > 0) {
            //TODO 마지막 인덱스에 도달했을 때의 처리 필요

            newDelta = -delta
        }
        // scroll down
        else {
            //TODO 최 상단 인덱스에 도달했을 때의 처리 필요

            newDelta = -delta
        }

        offsetChildrenVertical(newDelta)
        fillBottom(recycler, state)
        recycleViewsOutOfBounds(recycler)
        return -newDelta
    }

    /**
     * 현재 그려진 뷰들을 탐색하여, 화면 바깥에 넘어간 상단 영역의 뷰, 하단 영역의 뷰를 찾아내어 recyclerView에서 detach 시킨다.
     */
    private fun recycleViewsOutOfBounds(recycler: RecyclerView.Recycler) {
        if(childCount == 0) return
        val childCount = childCount

        var firstVisibleChild = 0
        for(i in 0 until childCount) {
            val child = getChildAt(i)!!
            val layoutParams = child.layoutParams as ViewGroup.MarginLayoutParams
            val top = 0
            if(getDecoratedBottom(child) + layoutParams.bottomMargin < top) {
                firstVisibleChild++
            } else {
                break
            }
        }

        var lastVisibleChild = firstVisibleChild
        for(i in lastVisibleChild until childCount) {
            val child = getChildAt(i)!!
            val layoutParams = child.layoutParams as ViewGroup.MarginLayoutParams
            if(getDecoratedTop(child) - layoutParams.topMargin <= height) {
                lastVisibleChild++
            } else {
                lastVisibleChild--
                break
            }
        }

        for(i in childCount-1 downTo lastVisibleChild +1) removeAndRecycleViewAt(i, recycler)
        for(i in firstVisibleChild -1 downTo 0) removeAndRecycleViewAt(i, recycler)
    }

    private fun getVerticalSpace(): Int = height - paddingBottom - paddingTop


}