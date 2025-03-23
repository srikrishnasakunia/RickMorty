package dev.krishna.rickmorty.ui.adapters

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.krishna.rickmorty.ui.adapters.CharacterPagingAdapter.Companion.ITEM_VIEW_TYPE_HEADER

class StickyHeaderItemDecoration: RecyclerView.ItemDecoration() {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val layoutManager = parent.layoutManager as? GridLayoutManager ?: return

        val firstVisiblePos = layoutManager.findFirstVisibleItemPosition()
        if(firstVisiblePos == RecyclerView.NO_POSITION) return

        val headerPos = findHeaderPosition(parent.adapter!!, firstVisiblePos)
        if (headerPos == -1) return

        val headerView = getHeaderView(parent, headerPos)
        c.save()
        c.translate(0f, calculateHeaderOffset(parent, headerView, firstVisiblePos))
        headerView.draw(c)
        c.restore()
    }

    private fun findHeaderPosition(parent: RecyclerView.Adapter<*>, firstVisiblePos: Int): Int {
        for(i in firstVisiblePos downTo 0){
            if(parent.getItemViewType(i) == CharacterPagingAdapter.ITEM_VIEW_TYPE_HEADER){
                return i
            }
        }
        return -1
    }

    private fun getHeaderView(parent: RecyclerView, headerPos: Int): View {
        val holder = parent.adapter!!.createViewHolder(parent,ITEM_VIEW_TYPE_HEADER)
        parent.adapter!!.onBindViewHolder(holder, headerPos)
        return holder.itemView
    }

    private fun calculateHeaderOffset(parent: RecyclerView, headerView: View, firstVisiblePos: Int): Float {
        val nextHeaderPos = findHeaderPosition(parent.adapter!!, firstVisiblePos + 1)
        return if (nextHeaderPos != -1){
            val nextHeader = parent.getChildAt(nextHeaderPos - firstVisiblePos)
            nextHeader.top - headerView.height.toFloat()
        } else 0f
    }
}