package com.pertamina.absensiapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.firebase.auth.FirebaseAuth
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.model.CounterGlobal
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.repository.PermitDataSource
import com.pertamina.absensiapplication.repository.PermitRepository
import com.pertamina.absensiapplication.repository.UserDataSourceFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UserViewModel(
    private val positions: Array<String>,
    private val repository: PermitRepository,
    private val auth: FirebaseAuth
) :
    BaseViewModel() {

    private var supervisorJob = SupervisorJob()
    private val user = MutableLiveData<User>()
    private val counterGlobal = MutableLiveData<CounterGlobal>()
    private val listUser = MutableLiveData<List<User>>()
    private var users: LiveData<PagedList<User>>? = null
    private val networkState = MutableLiveData<NetworkState>()
    private val networkListState = MutableLiveData<NetworkState>()
    private var usersTkjpDataSource = UserDataSourceFactory(repository, ioScope, "tkjp", positions, "")
    private var usersOrganicDataSource = UserDataSourceFactory(repository, ioScope, "organic", positions, "")
    val networkStateManageOrganic: LiveData<NetworkState> =
        Transformations.switchMap(usersOrganicDataSource.source) { it.getNetworkState() }
    val networkStateManageTkjp: LiveData<NetworkState> =
        Transformations.switchMap(usersTkjpDataSource.source) { it.getNetworkState() }

    fun getUsersPagedList(type: String, query: String): LiveData<PagedList<User>>? {
        users = when (type) {
            "search" -> {
                val searchDataSource = UserDataSourceFactory(repository, ioScope, "search", positions, query)
//                searchDataSource.source.value?.invalidate()
                LivePagedListBuilder(searchDataSource, pagedListConfig()).build()
            }
            "organic" -> LivePagedListBuilder(usersOrganicDataSource, pagedListConfig()).build()
            "tkjp" -> LivePagedListBuilder(usersTkjpDataSource, pagedListConfig()).build()
            else -> LivePagedListBuilder(usersOrganicDataSource, pagedListConfig()).build()
        }
        return users
    }

    fun getCounterGlobal(): MutableLiveData<CounterGlobal> {
        networkListState.postValue(NetworkState.RUNNING)
        ioScope.launch(getJobErrorListHandler() + supervisorJob) {
            counterGlobal.postValue(repository.getCounterGlobal())
            networkListState.postValue(NetworkState.SUCCESS)
        }
        return counterGlobal
    }

    fun getUser(uid: String = auth.currentUser?.uid.toString()): MutableLiveData<User> {
        networkListState.postValue(NetworkState.RUNNING)
        ioScope.launch(getJobErrorListHandler() + supervisorJob) {
            user.postValue(repository.getUser(uid))
            networkListState.postValue(NetworkState.SUCCESS)
        }
        return user
    }

    fun getUser(currentUid: String, seniorId: String, ohId: String): MutableLiveData<List<User>> {
        val list = mutableListOf<User>()
        networkListState.postValue(NetworkState.RUNNING)
        ioScope.launch(getJobErrorListHandler() + supervisorJob) {
            repository.getUser(currentUid)?.let { list.add(it) }
            if (seniorId.isNotEmpty()) {
                repository.getUser(seniorId)?.let { list.add(it) }
            } else {
                list.add(User())
            }
            if (ohId.isNotEmpty()) {
                repository.getUser(ohId)?.let { list.add(it) }
            } else {
                list.add(User())
            }
            listUser.postValue(list)
            networkListState.postValue(NetworkState.SUCCESS)
        }
        return listUser
    }

    fun getUsers(type: String): MutableLiveData<List<User>> {
        networkState.postValue(NetworkState.RUNNING)
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            listUser.postValue(repository.getUsers(type = type))
            networkState.postValue(NetworkState.SUCCESS)
        }
        return listUser
    }

    fun getNetwork() = networkState

    fun getListNetwork() = networkListState

    fun createUser(email: String, password: String, user: User): MutableLiveData<User> {
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            val newUser = User(userId = repository.createAccount(email, password))
            Log.d("testing", "uid: ${newUser.userId}")
            repository.createUserData(newUser.userId, user)
            this@UserViewModel.user.postValue(newUser)
        }
        return this.user
    }

    fun updateAccount(uid: String, email: String, password: String): MutableLiveData<User> {
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            repository.updateAccount(uid, email, password)
        }
        return user
    }

    fun updateUser(user: User) {
        networkState.postValue(NetworkState.RUNNING)
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            repository.updateUser(user)
            networkState.postValue(NetworkState.SUCCESS)
        }
    }

    fun signout(user: User) {
        networkState.postValue(NetworkState.RUNNING)
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            repository.updateUser(user)
            auth.signOut()
            networkState.postValue(NetworkState.SUCCESS)
        }
    }

    fun refreshUsersOrganic() = usersOrganicDataSource.source.value?.invalidate()
    private var amountOrganic = MutableLiveData<Int>()
    fun getOrganicLeave(): LiveData<Int> {
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            amountOrganic.postValue(repository.getOrganicLeave())
        }
        return amountOrganic
    }
    fun refreshOrganicLeave() = getOrganicLeave()

    private var amountTkjp = MutableLiveData<Int>()
    fun getTkjpLeave(): LiveData<Int> {
        ioScope.launch(getJobErrorHandler() + supervisorJob) {
            amountTkjp.postValue(repository.getTkjpLeave())
        }
        return amountTkjp
    }
    fun refreshTkjpLeave() = getTkjpLeave()


    fun refreshUsersTkjp() = usersTkjpDataSource.source.value?.invalidate()
    fun refreshUsers(type: String) = getUsers(type)
    fun refreshUser() = getUser()

    fun refreshListUser(currentUid: String, seniorId: String, ohId: String) = getUser(currentUid, seniorId, ohId)

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Log.e(PermitDataSource::class.java.simpleName, "An error happened: $e")
        networkState.postValue(NetworkState.FAILED)
    }

    private fun getJobErrorListHandler() = CoroutineExceptionHandler { _, e ->
        Log.e(PermitDataSource::class.java.simpleName, "An error happened: $e")
        networkListState.postValue(NetworkState.FAILED)
    }

    private fun pagedListConfig() = PagedList.Config.Builder()
        .setInitialLoadSizeHint(7)
        .setEnablePlaceholders(false)
        .setPageSize(7 * 2)
        .build()

    private val search = MediatorLiveData<PagedList<User>>()
    private val querySearch = MutableLiveData<String>()

    fun setQuerySearch(query: String) = querySearch.postValue(query)
    fun getQuerySearch() = querySearch

    fun getSearchResult(query: String): MediatorLiveData<PagedList<User>> {
        val data = LivePagedListBuilder(
            UserDataSourceFactory(repository, ioScope, "search", positions, query),
            pagedListConfig()
        ).build()
        search.addSource(data, { searches -> search.setValue(searches) })
        return search
    }
}