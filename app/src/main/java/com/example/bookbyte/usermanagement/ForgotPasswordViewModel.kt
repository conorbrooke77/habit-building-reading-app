package com.example.bookbyte.usermanagement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbyte.utils.DataResult
import com.example.bookbyte.utils.ValidationUtils
import kotlinx.coroutines.launch

class ForgotPasswordViewModel: ViewModel() {

    private val userRepository = UserRepository()

    private var _forgotPasswordResults = MutableLiveData<DataResult>()
    var forgotPasswordResults = _forgotPasswordResults

    fun sendPasswordResetEmail(email: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _forgotPasswordResults.postValue(DataResult(false,"Email is not valid!"))
            return
        }

        viewModelScope.launch {
            val resetInfo = userRepository.sendPasswordResetEmail(email)

            if (resetInfo.first)
                _forgotPasswordResults.postValue(DataResult(true, resetInfo.second))
            else
                _forgotPasswordResults.postValue(DataResult(false, resetInfo.second))
        }
    }

}