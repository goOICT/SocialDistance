package ai.kun.opentrace.ui.api

import androidx.lifecycle.MutableLiveData

class Result<T> {
    var status: Status
        private set

    var data: T?
        private set

    var error: Throwable?
        private set

    fun loading(): Result<T> {
        status = Status.LOADING
        error = null
        return this
    }

    fun success(data: T): Result<T> {
        status = Status.SUCCESS
        this.data = data
        error = null
        return this
    }

    fun error(error: Throwable): Result<T> {
        status = Status.ERROR
        this.error = error
        return this
    }

    enum class Status {
        CREATED, LOADING, ERROR, SUCCESS
    }

    init {
        status = Status.CREATED
        error = null
        data = null
    }
}

class ResultLiveData<T> : MutableLiveData<Result<T>>() {
    /**
     * Use this to put the Data on a LOADING Status
     */
    fun postLoading() {
        postValue(Result<T>().loading())
    }

    /**
     * Use this to put the Data on a ERROR DataStatus
     * @param throwable the error to be handled
     */
    fun postError(throwable: Throwable) {
        postValue(Result<T>().error(throwable))
    }

    /**
     * Use this to put the Data on a SUCCESS DataStatus
     * @param data
     */
    fun postSuccess(data: T) {
        postValue(Result<T>().success(data))
    }

}