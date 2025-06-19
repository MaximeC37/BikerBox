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

    // State for user reservations
    private val _userReservations = MutableStateFlow<Resource<List<Reservation>>>(Resource.Loading)
    val userReservations: StateFlow<Resource<List<Reservation>>> = _userReservations.asStateFlow()

    // State for UI messages
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // State for storing available lockers
    private val _availableLockers = MutableStateFlow<List<Locker>>(emptyList())
    val availableLockers: StateFlow<List<Locker>> = _availableLockers.asStateFlow()

    // Reservation parameters
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
        Log.d("ReservationViewModel", "START confirmReservation")

        val lockerId = selectedLockerId ?: run {
            Log.e("ReservationViewModel", "lockerId missing")
            _state.value = ReservationState.Error("Error: Locker not selected")
            return
        }

        val size = selectedSize ?: run {
            Log.e("ReservationViewModel", "size missing")
            _state.value = ReservationState.Error("Error: size not selected")
            return
        }

        val startDate = selectedStartDate ?: run {
            Log.e("ReservationViewModel", "startDate missing")
            _state.value = ReservationState.Error("Error: Missing start date")
            return
        }

        val endDate = selectedEndDate ?: run {
            Log.e("ReservationViewModel", "endDate missing")
            _state.value = ReservationState.Error("Error: Missing end date")
            return
        }

        Log.d("ReservationViewModel", "Parameters validated")

        viewModelScope.launch {
            try {
                _state.value = ReservationState.Loading

                // Create reservation
                val reservation = makeReservationUseCase(
                    lockerId = lockerId,
                    size = size,
                    startDate = startDate,
                    endDate = endDate
                )

                Log.d("ReservationViewModel", "Reservation created: ${reservation.id}")

                // Reload user reservations
                loadUserReservations()

                // Move to Success state
                _state.value = ReservationState.Success(reservation)
                _uiMessage.value = "Reservation successfully confirmed!"

                // Clear variables
                clearAllVariables()

            } catch (e: Exception) {
                Log.e("ReservationViewModel", "Confirmation error: ${e.message}", e)
                _state.value = ReservationState.Error("Error while booking: ${e.message}")
                _uiMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    fun loadUserReservations() {
        Log.d("ReservationViewModel", "START loadUserReservations")

        viewModelScope.launch {
            try {
                _userReservations.value = Resource.Loading

                val reservations = getUserReservationsUseCase()

                Log.d("ReservationViewModel", "Reservations loaded: ${reservations.size}")
                reservations.forEachIndexed { index, reservation ->
                    Log.d("ReservationViewModel", "  [$index] ${reservation.id} - ${reservation.lockerName}")
                }

                _userReservations.value = Resource.Success(reservations)

            } catch (e: Exception) {
                Log.e("ReservationViewModel", "Error loading reservations: ${e.message}")
                _userReservations.value = Resource.Error("Error loading: ${e.message}")
            }
        }
    }

    fun cancelReservation(reservationId: String) {
        Log.d("ReservationViewModel", "START cancelReservation: $reservationId")

        viewModelScope.launch {
            try {
                cancelReservationUseCase(reservationId)
                Log.d("ReservationViewModel", "Reservation cancelled")
                loadUserReservations()
                _uiMessage.value = "Reservation successfully cancelled"
            } catch (e: Exception) {
                Log.e("ReservationViewModel", "Cancellation error: ${e.message}")
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

    // 🔥 THIS METHOD MUST STORE DETAILS AND MOVE TO CONFIRMATION
    fun setReservationDetails(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        price: Double
    ) {
        Log.d("ReservationViewModel", "setReservationDetails")
        Log.d("ReservationViewModel", "lockerId: $lockerId, size: $size, price: $price")

        selectedLockerId = lockerId
        selectedSize = size
        selectedStartDate = startDate
        selectedEndDate = endDate
        reservationPrice = price

        // 🔥 MOVE DIRECTLY TO CONFIRMATION STATE
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
        Log.d("ReservationViewModel", "ViewModel reset")
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