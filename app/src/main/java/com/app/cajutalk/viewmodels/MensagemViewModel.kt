package com.app.cajutalk.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.cajutalk.data.repository.MensagemRepository
import com.app.cajutalk.network.models.MensagemDto
import kotlinx.coroutines.launch

class MensagemViewModel(application: Application, private val mensagemRepository: MensagemRepository) : AndroidViewModel(application) { // Updated constructor

    private val _enviarMensagemResult = MutableLiveData<Result<MensagemDto>>()
    val enviarMensagemResult: LiveData<Result<MensagemDto>> = _enviarMensagemResult

    private val _mensagensDaSala = MutableLiveData<Result<List<MensagemDto>>>()
    val mensagensDaSala: LiveData<Result<List<MensagemDto>>> = _mensagensDaSala

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun enviarMensagem(
        idSala: Int,
        conteudoTexto: String?,
        tipoMensagem: String?,
        arquivoUri: Uri?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = mensagemRepository.enviarMensagem(idSala, conteudoTexto, tipoMensagem, arquivoUri)
            _enviarMensagemResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun getMensagensPorSala(salaId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = mensagemRepository.getMensagensPorSala(salaId)
            _mensagensDaSala.postValue(result)
            _isLoading.postValue(false)
        }
    }
}