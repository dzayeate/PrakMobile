package com.unpas.kuliah.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Selamat Datang Di Perkuliahan"
    }
    val text: LiveData<String> = _text
}