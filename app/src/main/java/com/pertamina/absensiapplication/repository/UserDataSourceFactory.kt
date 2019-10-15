package com.pertamina.absensiapplication.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.pertamina.absensiapplication.model.User
import kotlinx.coroutines.CoroutineScope

class UserDataSourceFactory(
        private val repository: PermitRepository,
        private val scope: CoroutineScope,
        private val type: String,
        private val positions: Array<String>,
        private val query: String
) : DataSource.Factory<Int, User>() {

    val source = MutableLiveData<UserDataSource>()

    override fun create(): DataSource<Int, User> {
        val source = UserDataSource(positions, type, repository, scope, query)
        this.source.postValue(source)
        return source
    }

}