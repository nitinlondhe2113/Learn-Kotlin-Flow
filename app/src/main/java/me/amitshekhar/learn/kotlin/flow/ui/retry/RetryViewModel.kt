package me.amitshekhar.learn.kotlin.flow.ui.retry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.amitshekhar.learn.kotlin.flow.data.api.ApiHelper
import me.amitshekhar.learn.kotlin.flow.data.local.DatabaseHelper
import me.amitshekhar.learn.kotlin.flow.utils.Resource
import java.io.IOException

class RetryViewModel(
    val apiHelper: ApiHelper,
    dbHelper: DatabaseHelper
) : ViewModel() {

    private val status = MutableLiveData<Resource<String>>()

    fun startTask() {
        viewModelScope.launch {
            status.postValue(Resource.loading(null))
            // do a long running task
            doLongRunningTask()
                .flowOn(Dispatchers.Default)
                .retry(retries = 3) { cause ->
                    if (cause is IOException) {
                        delay(2000)
                        return@retry true
                    } else {
                        return@retry false
                    }
                }
                .catch {
                    status.postValue(Resource.error("Something Went Wrong", null))
                }
                .collect {
                    status.postValue(Resource.success("Task Completed"))
                }
        }
    }

    fun getStatus(): LiveData<Resource<String>> {
        return status
    }

    private fun doLongRunningTask(): Flow<Int> {
        return flow {
            // your code for doing a long running task
            // Added delay, random number, and exception to simulate

            delay(2000)

            val randomNumber = (0..2).random()

            if (randomNumber == 0) {
                throw IOException()
            } else if (randomNumber == 1) {
                throw IndexOutOfBoundsException()
            }

            delay(2000)
            emit(0)
        }
    }

}