package iam.thevoid.noxml.rx2.recycler.pagination

import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

@Deprecated("""
    Second try to load items with paging. Will be removed in release version.
""")
class PaginationLoader<T>(
    private val pageNumberMapper: (Int) -> Int = { it },
    private val nextPage: (Int) -> Single<out Response<T>>
) {
    private val startPage = 0

    private var retrieveDisposable: Disposable? = null

    private var startDisposable: Disposable? = null

    private val refresh by lazy { PublishProcessor.create<Any>().toSerialized() }

    private val rxItems by lazy { BehaviorProcessor.create<List<T>>() }

    private val lastPage by lazy { AtomicBoolean() }

    fun refresh() = refresh.onNext(Any())

    fun clear() = rxItems.onNext(emptyList())

    val items: Flowable<List<T>> by lazy {
        rxItems.doOnSubscribe {
                if (rxItems.value.orEmpty().isEmpty())
                    startDisposable = nextPage(pageNumberMapper(startPage))
                        .subscribeOn(Schedulers.io())
                        .repeatWhen { it.flatMapMaybe { refresh.firstElement() } }
                        .subscribe(::onItemsReceived) {
                            Log.e(javaClass.name, "error when retrieve first page", it)
                        }
            }
    }

    private fun onItemsReceived(response: Response<T>) {
        if (response.page == pageNumberMapper(startPage))
            rxItems.onNext(response.items)
        else
            rxItems.onNext(mutableListOf<T>().apply {
                addAll(rxItems.value.orEmpty())
                addAll(response.items)
            })

        lastPage.set(response.isLastPage)
    }


    fun loadMore(page: Int) {
        if (!lastPage.get()) {
            retrieveDisposable?.dispose()
            retrieveDisposable = nextPage(pageNumberMapper(page))
                .subscribeOn(Schedulers.io())
                .subscribe(::onItemsReceived) {
                    Log.e(javaClass.name, "error when retrieve page $page", it)
                }
        }
    }

    fun release() {
        retrieveDisposable?.dispose()
        startDisposable?.dispose()
    }


    open class Response<T>(
        val page: Int = 1,
        val items: List<T> = emptyList(),
        val isLastPage: Boolean = true
    )
}
