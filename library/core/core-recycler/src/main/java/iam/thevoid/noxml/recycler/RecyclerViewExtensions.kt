@file:Suppress("unused")

package iam.thevoid.noxml.recycler

import androidx.recyclerview.widget.RecyclerView
import iam.thevoid.noxml.adapterview.ItemBindings

@Suppress("UNCHECKED_CAST")
fun <T : Any> RecyclerView.setItems(
    items: List<T>,
    bindings: ItemBindings,
    callbackFactory: ((old: List<T>, new: List<T>) -> DiffCallback<T>) = diffCallback()
) {
    (adapter as? StandaloneRecyclerAdapter<T>)?.apply {
        data = items.toMutableList()
    } ?: run {
        StandaloneRecyclerAdapter(items).apply {
            this@apply.bindings = bindings
            this@apply.diffCallbackFactory = callbackFactory
            adapter = this
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <T : Any, reified A : StandaloneRecyclerAdapter<T>> RecyclerView.setItems(
    items: List<T>,
    itemBindings: ItemBindings,
    foreignAdapterFactory: (List<T>) -> A? = { null },
    noinline diffCallbackFactory: ((old: List<T>, new: List<T>) -> DiffCallback<T>) = diffCallback()
) {
    (adapter as? StandaloneRecyclerAdapter<T>)?.apply {
        data = items.toMutableList()
    } ?: run {
        (foreignAdapterFactory(items) ?: StandaloneRecyclerAdapter(items)).apply {
            this@apply.bindings = itemBindings
            this@apply.diffCallbackFactory = diffCallbackFactory
            adapter = this
        }
    }
}


private val RecyclerView.endlessScrollDelegate
    get() = getTag(R.id.recyclerEndlessScroll) as? iam.thevoid.noxml.recycler.delegate.EndlessScrollDelegate
        ?: iam.thevoid.noxml.recycler.delegate.EndlessScrollDelegate().also {
            setTag(R.id.recyclerEndlessScroll, it)
            addOnScrollListener(it)
        }

fun RecyclerView.resetEndlessScrollState() {
    endlessScrollDelegate.resetListeners()
}

fun RecyclerView.setLoadMore(action: (Int) -> Unit) {
    endlessScrollDelegate.addListener(object : EndlessScrollListener() {
        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
            action(page)
        }
    })
}

/**
 * Spacing bindings
 */

val RecyclerView.startSpacing
    get() = (getTag(R.id.recyclerStartSpacing) as? StartEndPaddingRecyclerDecoration)
        ?: StartEndPaddingRecyclerDecoration().also {
            addItemDecoration(it)
            setTag(R.id.recyclerStartSpacing, it)
        }

fun RecyclerView.setStartSpacing(spacing: Int) {
    val decoration = startSpacing
    removeItemDecoration(decoration)
    decoration.start = spacing
    addItemDecoration(decoration)
}

val RecyclerView.endSpacing
    get() = (getTag(R.id.recyclerEndSpacing) as? StartEndPaddingRecyclerDecoration)
        ?: StartEndPaddingRecyclerDecoration().also {
            addItemDecoration(it)
            setTag(R.id.recyclerEndSpacing, it)
        }

fun RecyclerView.setEndSpacing(spacing: Int) {
    val decoration = endSpacing
    removeItemDecoration(decoration)
    decoration.end = spacing
    addItemDecoration(decoration)
}


/**
 * On RecyclerView scroll reverse binding
 */

val RecyclerView.onRecyclerScroll
    get() = (getTag(R.id.recyclerScroll) as? iam.thevoid.noxml.recycler.delegate.OnRecyclerScrollDelegate)
        ?: iam.thevoid.noxml.recycler.delegate.OnRecyclerScrollDelegate()
            .also {
                setTag(R.id.recyclerScroll, it)
                addOnScrollListener(it)
            }