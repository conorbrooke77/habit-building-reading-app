package com.example.bookbyte.usermanagement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookbyte.App
import com.example.bookbyte.utils.DataResult
import com.example.bookbyte.utils.ValidationUtils

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()

    // Private mutable LiveData for internal use
    private val _loginResult = MutableLiveData<DataResult>()

    // Public immutable LiveData for external use
    val loginResult: LiveData<DataResult> = _loginResult

    fun login(userCredentials: String, password: String) {

        if (!ValidationUtils.validateCredentials(userCredentials, password))
            _loginResult.postValue(DataResult(false, "Invalid Credentials"))

        if (ValidationUtils.isValidEmail(userCredentials)) {
            userRepository.loginWithEmail(userCredentials, password) { success, message ->
                if (success)
                    App.updateStreakIfNeeded(getApplication())

                _loginResult.postValue(DataResult(success, message))
            }
        } else {
            userRepository.loginWithUsername(userCredentials, password) { success, message ->
                if (success)
                    App.updateStreakIfNeeded(getApplication())

                _loginResult.postValue(DataResult(success, message))
            }
        }
    }

}