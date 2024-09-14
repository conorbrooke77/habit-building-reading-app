package com.example.bookbyte.usermanagement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookbyte.usermanagement.data.repository.UserRepository
import com.example.bookbyte.shared.utils.data.models.DataResult
import com.example.bookbyte.shared.utils.validation.ValidationUtils
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private var _registerResults = MutableLiveData<DataResult>()
    private val userRepository = UserRepository()
    val registerResults: LiveData<DataResult> = _registerResults

    fun register(username: String, email: String, password: String, confirmPassword: String) {

        viewModelScope.launch {
            // Check if the username is unique asynchronously
            val isUnique = userRepository.isUniqueUsername(username)

            if (!isUnique) {
                _registerResults.postValue(DataResult(false, "Username already exists!"))
                return@launch
            }

            // Perform synchronous validation
            val validationInfo =
                ValidationUtils.validateCredentials(username, email, password, confirmPassword)
            if (!validationInfo.first) {
                _registerResults.postValue(DataResult(false, validationInfo.second))
                return@launch
            }

            // Create the user and post the result asynchronously
            val createUserResults = userRepository.createUser(username, email, password)

            _registerResults.postValue(DataResult(createUserResults.first, createUserResults.second))
        }
    }
}