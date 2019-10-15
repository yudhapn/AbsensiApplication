package com.pertamina.absensiapplication.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.firebase.auth.FirebaseAuth
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.model.Permit
import com.pertamina.absensiapplication.repository.PermitDataSource
import com.pertamina.absensiapplication.repository.PermitDataSourceFactory
import com.pertamina.absensiapplication.repository.PermitRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PermitViewModel(private val repo: PermitRepository, auth: FirebaseAuth) : BaseViewModel() {
    private var supervisorJob = SupervisorJob()
    private val networkState = MutableLiveData<NetworkState>()
    private val permit = MutableLiveData<Permit>()

    fun createPermitManual(userId: String, permitParamp: Permit): MutableLiveData<Permit> {
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            val newPermit = repo.createPermitManual(userId, permitParamp)
            permit.postValue(newPermit)
        }
        return permit
    }

    fun createPermit(permit: Permit) {
        networkState.postValue(NetworkState.RUNNING)
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            if (permit.operationHead.isNotEmpty()) {

                val oh = repo.getUser(permit.operationHead)
                if (oh != null) {
                    if (oh.leave) {
                        permit.operationHead = oh.pjs
                    }
                }
            }
            if (permit.senior.isNotEmpty()) {
                val senior = repo.getUser(permit.senior)
                if (senior != null) {
                    if (senior.leave) {
                        permit.senior = senior.pjs
                    }
                }

            }
            repo.createPermit(permit.userId, permit)
            networkState.postValue(NetworkState.SUCCESS)
        }
    }

    fun updatePermit(permit: Permit) {
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            repo.updatePermit(permit.userId, permit.permitId, permit)
        }
    }

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Log.e(PermitDataSource::class.java.simpleName, "An error happened: $e")
//        networkState.postValue(NetworkState.FAILED)
    }

    private val allDataSource = PermitDataSourceFactory(repo, auth.currentUser?.uid.toString(), "all", ioScope)
    private var allUidDataSource = PermitDataSourceFactory(repo, auth.currentUser?.uid.toString(), "all", ioScope)
    private val reqDataSource = PermitDataSourceFactory(repo, auth.currentUser?.uid.toString(), "request", ioScope)
    private val compDataSource = PermitDataSourceFactory(repo, auth.currentUser?.uid.toString(), "complete", ioScope)
    private val confDataSource = PermitDataSourceFactory(repo, auth.currentUser?.uid.toString(), "confirm", ioScope)
    private var permits: LiveData<PagedList<Permit>>? = null

    fun getPermits(type: String, uid: String = ""): LiveData<PagedList<Permit>>? {
        permits = when (type) {
            "all" -> {
                if (uid.isEmpty()) {
                    LivePagedListBuilder(allDataSource, pagedListConfig()).build()
                } else {
                    LivePagedListBuilder(getAllUidDataSource(uid), pagedListConfig()).build()
                }
            }
            "request" -> LivePagedListBuilder(reqDataSource, pagedListConfig()).build()
            "complete" -> LivePagedListBuilder(compDataSource, pagedListConfig()).build()
            "confirm" -> LivePagedListBuilder(confDataSource, pagedListConfig()).build()
            else -> LivePagedListBuilder(allDataSource, pagedListConfig()).build()
        }
        return permits
    }


    private fun getAllUidDataSource(uid: String): PermitDataSourceFactory {
        allUidDataSource = PermitDataSourceFactory(repo, uid, "all", ioScope)
        return allUidDataSource
    }

    val networkStatePagedList: LiveData<NetworkState> = switchMap(allDataSource.source) { it.getNetworkState() }
    val networkStateUidPagedList: LiveData<NetworkState> = switchMap(allUidDataSource.source) { it.getNetworkState() }
    val networkRequestPagedList: LiveData<NetworkState> = switchMap(reqDataSource.source) { it.getNetworkState() }
    fun refreshFailed() = allDataSource.source.value?.retryFailedQuery()
    fun refreshRequestFailed() = reqDataSource.source.value?.retryFailedQuery()
    fun refresh() = allDataSource.source.value?.invalidate()
    //    fun refreshCreate(permit: Permit) = createPermit(permit)
//    fun refreshRequest() = reqDataSource.source.value?.invalidate()
    private fun pagedListConfig() = PagedList.Config.Builder()
        .setInitialLoadSizeHint(7)
        .setEnablePlaceholders(false)
        .setPageSize(7 * 2)
        .build()
}