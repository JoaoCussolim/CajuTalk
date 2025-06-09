package com.app.cajutalk.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.cajutalk.data.repository.MensagemRepository
import com.app.cajutalk.data.repository.SalaRepository
import com.app.cajutalk.network.models.EntrarSalaDto
import com.app.cajutalk.network.models.SalaChatDto
import com.app.cajutalk.network.models.SalaCreateDto
import com.app.cajutalk.network.models.UsuarioDaSalaDto
import com.app.cajutalk.network.models.UsuarioSalaDto
import kotlinx.coroutines.launch

class SalaViewModel(application: Application, private val salaRepository: SalaRepository, private val mensagemRepository: MensagemRepository) : AndroidViewModel(application) { // Updated constructor

    private val _createSalaResult = MutableLiveData<Result<SalaChatDto>>()
    val createSalaResult: LiveData<Result<SalaChatDto>> = _createSalaResult

    private val _salaById = MutableLiveData<Result<SalaChatDto>>()
    val salaById: LiveData<Result<SalaChatDto>> = _salaById

    private val _allSalas = MutableLiveData<Result<List<SalaChatDto>>>()
    val allSalas: LiveData<Result<List<SalaChatDto>>> = _allSalas

    private val _deleteSalaResult = MutableLiveData<Result<Unit>>()
    val deleteSalaResult: LiveData<Result<Unit>> = _deleteSalaResult

    private val _usersInSala = MutableLiveData<Result<List<UsuarioDaSalaDto>>>()
    val usersInSala: LiveData<Result<List<UsuarioDaSalaDto>>> = _usersInSala

    private val _entrarSalaResult = MutableLiveData<Result<UsuarioSalaDto>>()
    val entrarSalaResult: LiveData<Result<UsuarioSalaDto>> = _entrarSalaResult

    private val _sairSalaResult = MutableLiveData<Result<Unit>>()
    val sairSalaResult: LiveData<Result<Unit>> = _sairSalaResult

    private val _banirUsuarioResult = MutableLiveData<Result<UsuarioSalaDto>>()
    val banirUsuarioResult: LiveData<Result<UsuarioSalaDto>> = _banirUsuarioResult

    private val _desbanirUsuarioResult = MutableLiveData<Result<UsuarioSalaDto>>()
    val desbanirUsuarioResult: LiveData<Result<UsuarioSalaDto>> = _desbanirUsuarioResult

    private val _relacaoUsuarioSala = MutableLiveData<Result<UsuarioSalaDto>>()
    val relacaoUsuarioSala: LiveData<Result<UsuarioSalaDto>> = _relacaoUsuarioSala

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun createSala(salaCreateDto: SalaCreateDto) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.createSala(salaCreateDto)
            _createSalaResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun getSalaById(salaId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.getSalaById(salaId)
            _salaById.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun uploadRoomImage(uri: Uri, callback: (Result<String>) -> Unit) {
        viewModelScope.launch {
            // This is a simplified example. You'd call your repository here.
            val result = mensagemRepository.uploadFileAndGetUrl(uri)
            callback(result)
        }
    }

    fun getAllSalas() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.getAllSalas()
            _allSalas.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun deleteSala(salaId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.deleteSala(salaId)
            _deleteSalaResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun getUsersInSala(salaId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.getUsersInSala(salaId)
            _usersInSala.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun entrarSala(entrarSalaDto: EntrarSalaDto) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.entrarSala(entrarSalaDto)
            _entrarSalaResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun sairSala(salaId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.sairSala(salaId)
            _sairSalaResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun banirUsuario(salaId: Int, usuarioIdParaBanir: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.banirUsuario(salaId, usuarioIdParaBanir)
            _banirUsuarioResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun desbanirUsuario(salaId: Int, usuarioIdParaDesbanir: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.desbanirUsuario(salaId, usuarioIdParaDesbanir)
            _desbanirUsuarioResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun getRelacaoUsuarioSala(salaId: Int, usuarioId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = salaRepository.getRelacaoUsuarioSala(salaId, usuarioId)
            _relacaoUsuarioSala.postValue(result)
            _isLoading.postValue(false)
        }
    }
}