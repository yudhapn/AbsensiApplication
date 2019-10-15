package com.pertamina.absensiapplication.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.model.User
import kotlinx.coroutines.*

class UserDataSource(
    private val positions: Array<String>,
    private val type: String,
    private val repository: PermitRepository,
    private val scope: CoroutineScope,
    private val query: String
) : PageKeyedDataSource<Int, User>() {

    private var supervisorJob = SupervisorJob()
    private val networkState = MutableLiveData<NetworkState>()
    private var retryQuery: (() -> Any)? = null

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, User>) {
        retryQuery = { loadInitial(params, callback) }
        getEmployee {
            callback.onResult(it, 0, it.size, null, null)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
        retryQuery = { loadAfter(params, callback) }
        getEmployee {
            callback.onResult(it, null)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {}

    private fun getEmployee(callback: (List<User>) -> Unit) {
        networkState.postValue(NetworkState.RUNNING)
        scope.launch(getJobErrorHandler() + supervisorJob) {
            delay(200)
            var list = mutableListOf<User>()
            when (type) {
                "organic" -> {
                    val ranges = 1..6
                    for (i in ranges) {
                        list.add(User(""))
                    }
                    val users = repository.getUsers("organic").toMutableList()
                    users.forEach {
                        when (it.position) {
                            positions[0] -> list[0] = it
                            positions[1] -> list[1] = it
                            positions[2] -> list[2] = it
                            positions[3] -> list[3] = it
                            positions[4] -> list[4] = it
                            positions[5] -> list[5] = it
                            else -> list.add(it)
                        }
                    }
                    list = list.filter { it.name.isNotEmpty() && it.name.trim() != "admin" }.toMutableList()
                }
                "search" -> list = repository.getUsers("all").filter { it.name.contains(query, true) }.toMutableList()

                else -> list = repository.getUsers("tkjp").toMutableList()
            }
            Log.d("testing", "type: $type query: $query size list: ${list.size}")
            retryQuery = null
            networkState.postValue(NetworkState.SUCCESS)
            callback(list)
        }
    }

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Log.e(UserDataSource::class.java.simpleName, "An error happened: $e")
        networkState.postValue(NetworkState.FAILED)
    }

    override fun invalidate() {
        super.invalidate()
        supervisorJob.cancelChildren()
    }

    fun getNetworkState(): LiveData<NetworkState> = networkState

}
