package com.example.bookbyte.usermanagement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookbyte.utils.DataResult
import com.example.bookbyte.utils.ValidationUtils

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private var _registerResults = MutableLiveData<DataResult>()
    private val userRepository = UserRepository()
    val registerResults: LiveData<DataResult> = _registerResults

    fun register(username: String, email: String, password: String, confirmPassword: String) {

        val validationInfo = ValidationUtils.validateCredentials(username, email, password, confirmPassword)
        if (!validationInfo.first) {
            _registerResults.postValue(DataResult(false, validationInfo.second))
            return
        }

        userRepository.createUser(username, email, password) {success, message ->
                _registerResults.postValue(DataResult(success, message))
        }
    }
}