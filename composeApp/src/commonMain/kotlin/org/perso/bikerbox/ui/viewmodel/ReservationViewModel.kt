package org.perso.bikerbox.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.*
import org.perso.bikerbox.domain.usecases.*

class ReservationViewModel(
    private val getAvailableLockersUseCase: GetAvailableLockersUseCase,
    private val makeReservationUseCase: MakeReservationUseCase,
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ReservationState>(ReservationState.Loading)
    val state: StateFlow<ReservationState> = _state.asStateFlow()

    private val _userReservations = MutableStateFlow<Resource<List<Reservation>>>(Resource.Loading)
    val userReservations: StateFlow<Resource<List<Reservation>>> = _userReservations.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _availableLockers = MutableStateFlow<List<Locker>>(emptyList())
    val availableLockers: StateFlow<List<Locker>> = _availableLockers.asStateFlow()

    private var selectedLockerId: String? = null
    private var selectedLockerName: String? = null
    private var selectedSize: LockerSize? = null
    private var selectedStartDate: LocalDateTime? = null
    private var selectedEndDate: LocalDateTime? = null
    private var reservationPrice: Double = 0.0

    init {
        loadLockers()
        loadUserReservations()
    }

    private fun loadLockers() {
        viewModelScope.launch {
            try {
                val lockers = getAvailableLockersUseCase()
                _availableLockers.value = lockers
                _state.value = ReservationState.LockerSelection(lockers)
                Log.d("ReservationViewModel", "Lockers loaded: ${lockers.size}")
            } catch (e: Exception) {
                Log.e("ReservationViewModel", "Error loading lockers: ${e.message}")
                _state.value = ReservationState.Error("Error loading lockers")
            }
        }
    }

    fun selectLocker(locker: Locker) {
        Log.d("ReservationViewModel", "Locker selected: ${locker.name}")
        selectedLockerId = locker.id
        selectedLockerName = locker.name
        _state.value = ReservationState.SizeSelection(locker)
    }

    fun selectSize(size: LockerSize) {
        Log.d("ReservationViewModel", "Size selected: $size")
        selectedSize = size

        _state.value = ReservationState.DateSelection(
            lockerId = selectedLockerId!!,
            lockerName = selectedLockerName!!,
            size = size
        )
    }

    fun confirmReservation() {
        val lockerId = selectedLockerId ?: run {
            _state.value = ReservationState.Error("Error: Locker not selected")
            return
        }

        val size = selectedSize ?: run {
            _state.value = ReservationState.Error("Error: size not selected")
            return
        }

        val startDate = selectedStartDate ?: run {
            _state.value = ReservationState.Error("Error: Missing start date")
            return
        }

        val endDate = selectedEndDate ?: run {
            _state.value = ReservationState.Error("Error: Missing end date")
            return
        }


        viewModelScope.launch {
            try {
                _state.value = ReservationState.Loading

                val reservation = makeReservationUseCase(
                    lockerId = lockerId,
                    size = size,
                    startDate = startDate,
                    endDate = endDate
                )

                loadUserReservations()

                _state.value = ReservationState.Success(reservation)
                _uiMessage.value = "Reservation successfully confirmed!"

                clearAllVariables()

            } catch (e: Exception) {
                _state.value = ReservationState.Error("Error while booking: ${e.message}")
                _uiMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    fun loadUserReservations() {

        viewModelScope.launch {
            try {
                _userReservations.value = Resource.Loading

                val reservations = getUserReservationsUseCase()

                _userReservations.value = Resource.Success(reservations)

            } catch (e: Exception) {
                _userReservations.value = Resource.Error("Error loading: ${e.message}")
            }
        }
    }

    fun cancelReservation(reservationId: String) {

        viewModelScope.launch {
            try {
                cancelReservationUseCase(reservationId)
                loadUserReservations()
                _uiMessage.value = "Reservation successfully cancelled"
            } catch (e: Exception) {
                _uiMessage.value = "Error while canceling: ${e.message}"
            }
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun getLockerById(id: String): Locker? {
        return _availableLockers.value.find { it.id == id }
    }

    fun setReservationDetails(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        price: Double
    ) {
        selectedLockerId = lockerId
        selectedSize = size
        selectedStartDate = startDate
        selectedEndDate = endDate
        reservationPrice = price

        _state.value = ReservationState.ConfirmationNeeded(
            lockerId = lockerId,
            lockerName = getLockerById(lockerId)?.name ?: "Unknown locker",
            size = size,
            startDate = startDate,
            endDate = endDate,
            price = price
        )
    }

    fun reset() {
        clearAllVariables()
        loadLockers()
    }

    private fun clearAllVariables() {
        selectedLockerId = null
        selectedLockerName = null
        selectedSize = null
        selectedStartDate = null
        selectedEndDate = null
        reservationPrice = 0.0
    }
}