package com.godgod.customlayoutmanager

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

class VerticalFlowLayoutManager : RecyclerView.LayoutManager() {

    private var mFirstVisiblePosition: Int = 0
    private var mLastVisiblePosition: Int = 0


    val parentTop: Int
        get() = if (clipToPadding) paddingTop else 0

    val parentBottom: Int
        get() = if (clipToPadding) height - paddingBottom else height

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /** notifyItemsetChanged 호출 시에 호출되는 메소드 */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)
        if (state.itemCount <= 0) return
        fillBottom(recycler, state)
    }

    private fun fillTop(recycler: RecyclerView.Recycler, state: RecyclerView.State) {

        // child count가 0이상 일 때, 현재 그려진 recyclerView child의 첫번째 인덱스를 구해, 그 인덱스를 endIndex로 잡는다.
        if (childCount <= 0) return
        val child = getChildAt(0)!!
        val endPosition = getPosition(child) - 1
        val layoutParam = child.layoutParams as ViewGroup.MarginLayoutParams
        val parentBottom = getDecoratedTop(child)

        var prevChildRight = width - paddingRight
        var prevChildBottom = parentBottom

        mLastVisiblePosition = endPosition
        for (i in endPosition downTo 0) {
            if (parentBottom < 0) return

            val view = recycler.getViewForPosition(i).also {
                addView(it, 0)
                measureChildWithMargins(it, 0, 0)
            }

            // 새로 그려질 뷰의 사이즈가 리사이클러뷰의 사이즈를 넘긴다면 다음 줄로 변경한다.
            if (prevChildRight - view.measuredWidth < 0) {
                prevChildBottom -= view.measuredHeight
                prevChildRight = width - view.paddingRight
            }

            val left = prevChildRight - view.measuredWidth
            val top = prevChildBottom - view.measuredHeight

            if (prevChildBottom < 0) {
                removeView(view)
                return
            }

            layoutDecoratedWithMargins(view, left, top, prevChildRight, prevChildBottom)
            prevChildRight = left
            mFirstVisiblePosition = i
        }
    }

    private fun fillBottom(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val parentTop: Int
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

        mFirstVisiblePosition = startPosition
        for (i in startPosition until state.itemCount) {
            if (prevChildTop > height) return

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
            mLastVisiblePosition = i
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
        /* Log.e(
             "godgod", "scroll vertical  " +
                     "\n viewSpan : ${viewSpan} " +
                     "\n viewSpan - getVerticalSpace : ${viewSpan - getVerticalSpace()} " +
                     "\n getDecoratedBottom : ${getDecoratedBottom(bottomView)} " +
                     "\n getDecoratedTop : ${getDecoratedTop(topView)}"
         )*/


        if (viewSpan < getVerticalSpace()) return 0
        var newDelta: Int

        // scroll down
        if (delta > 0) {
            Log.e("godgod", "${topView.bottom}")
            if (mLastVisiblePosition == state.itemCount - 1) {
                val bottomOffset = height - getDecoratedBottom(bottomView)
                Log.e("godgod", "${delta}   ${bottomOffset}")
                newDelta = -min(delta, if(bottomOffset > 0) 0 else bottomOffset)
            } else {
                fillBottom(recycler, state)
                newDelta = -delta
            }
        }
        // scroll up
        else {
            if (mFirstVisiblePosition == 0) {
                newDelta = -min(abs(delta), getChildAt(0)!!.top)
            } else {
                fillTop(recycler, state)
                newDelta = -delta
            }
        }
        offsetChildrenVertical(newDelta)
        recycleViewsOutOfBounds(recycler)
        return -newDelta
    }

    /**
     * 현재 그려진 뷰들을 탐색하여, 화면 바깥에 넘어간 상단 영역의 뷰, 하단 영역의 뷰를 찾아내어 recyclerView에서 detach 시킨다.
     */
    private fun recycleViewsOutOfBounds(recycler: RecyclerView.Recycler) {
        if (childCount == 0) return
        val childCount = childCount

        var firstVisibleChild = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val layoutParams = child.layoutParams as ViewGroup.MarginLayoutParams
            val top = 0
            if (getDecoratedBottom(child) + layoutParams.bottomMargin < top) {
                firstVisibleChild++
            } else {
                break
            }
        }

        var lastVisibleChild = firstVisibleChild
        for (i in lastVisibleChild until childCount) {
            val child = getChildAt(i)!!
            val layoutParams = child.layoutParams as ViewGroup.MarginLayoutParams
            if (getDecoratedTop(child) - layoutParams.topMargin <= height) {
                lastVisibleChild++
            } else {
                lastVisibleChild--
                break
            }
        }

        for (i in childCount - 1 downTo lastVisibleChild + 1) removeAndRecycleViewAt(i, recycler)
        for (i in firstVisibleChild - 1 downTo 0) removeAndRecycleViewAt(i, recycler)
    }

    private fun getVerticalSpace(): Int = height - paddingBottom - paddingTop


}