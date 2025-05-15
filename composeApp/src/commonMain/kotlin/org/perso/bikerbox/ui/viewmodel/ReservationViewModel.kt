package org.perso.bikerbox.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.domain.usecases.CancelReservationUseCase
import org.perso.bikerbox.domain.usecases.GetAvailableLockersUseCase
import org.perso.bikerbox.domain.usecases.GetUserReservationsUseCase
import org.perso.bikerbox.domain.usecases.MakeReservationUseCase

class ReservationViewModel(
    private val getAvailableLockersUseCase: GetAvailableLockersUseCase,
    private val makeReservationUseCase: MakeReservationUseCase,
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ReservationState>(ReservationState.Loading)
    val state: StateFlow<ReservationState> = _state.asStateFlow()

    // État pour les réservations de l'utilisateur
    private val _userReservations = MutableStateFlow<Resource<List<Reservation>>>(Resource.Loading)
    val userReservations: StateFlow<Resource<List<Reservation>>> = _userReservations.asStateFlow()

    // État pour stocker les casiers disponibles
    private val _availableLockers = MutableStateFlow<List<Locker>>(emptyList())
    val availableLockers: StateFlow<List<Locker>> = _availableLockers.asStateFlow()

    // Paramètres de réservation
    private var selectedLockerId: String? = null
    private var selectedLockerName: String? = null
    private var selectedSize: LockerSize? = null
    private var selectedStartDate: LocalDateTime? = null
    private var selectedEndDate: LocalDateTime? = null
    private var reservationPrice: Double = 0.0
    private var tempLockerId: String? = null
    private var tempLockerSize: LockerSize? = null
    private var tempStartDate: LocalDateTime? = null
    private var tempEndDate: LocalDateTime? = null
    private var tempPrice: Double = 0.0

    init {
        Log.d("ReservationViewModel", "Initialisation du ViewModel")
        loadLockers()
        loadUserReservations()
    }

    private fun loadLockers() {
        viewModelScope.launch {
            try {
                Log.d("ReservationViewModel", "Début de chargement des casiers - État actuel: ${_state.value}")
                _state.value = ReservationState.Loading
                Log.d("ReservationViewModel", "État mis à Loading")

                val lockers = getAvailableLockersUseCase()
                Log.d("ReservationViewModel", "Casiers chargés via useCase: ${lockers.size}")

                // Mettre à jour les deux états
                _availableLockers.value = lockers
                Log.d("ReservationViewModel", "availableLockers mis à jour avec ${lockers.size} casiers")

                if (lockers.isEmpty()) {
                    Log.d("ReservationViewModel", "Liste de casiers vide, affichage de l'erreur")
                    _state.value = ReservationState.Error("Aucun casier disponible")
                } else {
                    Log.d(
                        "ReservationViewModel",
                        "Liste de casiers non vide (${lockers.size}), mise à jour de l'état vers LockerSelection"
                    )
                    _state.value = ReservationState.LockerSelection(lockers)
                    Log.d("ReservationViewModel", "État mis à LockerSelection avec ${lockers.size} casiers")
                }
            } catch (e: Exception) {
                Log.e("ReservationViewModel", "Erreur lors du chargement des casiers", e)
                _state.value = ReservationState.Error(e.message ?: "Une erreur est survenue")
            }
        }
    }

    fun selectLocker(locker: Locker) {
        Log.d("ReservationViewModel", "Sélection du casier: ${locker.name}")
        selectedLockerId = locker.id
        selectedLockerName = locker.name
        _state.value = ReservationState.SizeSelection(locker)
        Log.d("ReservationViewModel", "État mis à SizeSelection pour le casier ${locker.name}")
    }

    fun selectSize(size: LockerSize) {
        Log.d("ReservationViewModel", "Sélection de la taille: $size")
        selectedSize = size
        val lockerId = selectedLockerId ?: return
        val lockerName = selectedLockerName ?: return
        _state.value = ReservationState.DateSelection(lockerId, lockerName, size)
        Log.d("ReservationViewModel", "État mis à DateSelection avec taille $size")
    }

    fun selectDates(startDate: LocalDateTime, endDate: LocalDateTime, price: Double) {
        Log.d("ReservationViewModel", "Sélection des dates: $startDate à $endDate, prix: $price")
        selectedStartDate = startDate
        selectedEndDate = endDate
        reservationPrice = price

        val lockerId = selectedLockerId ?: return
        val size = selectedSize ?: return
        val lockerName = selectedLockerName ?: return

        _state.value = ReservationState.ConfirmationNeeded(
            lockerId = lockerId,
            lockerName = lockerName,
            size = size,
            startDate = startDate,
            endDate = endDate,
            price = price
        )
        Log.d("ReservationViewModel", "État mis à ConfirmationNeeded")
    }

    fun confirmReservation() {
        Log.d("ReservationViewModel", "Tentative de confirmation de réservation")
        _state.value = ReservationState.Loading
        Log.d("ReservationViewModel", "État changé à Loading")

        if (tempLockerId != null && tempLockerSize != null && tempStartDate != null && tempEndDate != null) {
            Log.d("ReservationViewModel", "Toutes les données nécessaires sont présentes")
            viewModelScope.launch {
                var attemptsLeft = 3
                var success = false
                var lastError: Exception? = null
                var createdReservation: Reservation? = null

                while (attemptsLeft > 0 && !success) {
                    try {
                        Log.d("ReservationViewModel", "Tentative ${4-attemptsLeft}/3")
                        Log.d(
                            "ReservationViewModel",
                            "Paramètres: lockerId=$tempLockerId, size=$tempLockerSize, start=$tempStartDate, end=$tempEndDate"
                        )

                        createdReservation = makeReservationUseCase(
                            tempLockerId!!,
                            tempLockerSize!!,
                            tempStartDate!!,
                            tempEndDate!!
                        )

                        Log.d("ReservationViewModel", "makeReservationUseCase terminé avec succès")
                        Log.d("ReservationViewModel", "Réservation ID: ${createdReservation.id}")

                        success = true
                    } catch (e: Exception) {
                        lastError = e
                        Log.e("ReservationViewModel", "Erreur lors de la tentative ${4-attemptsLeft}/3: ${e.message}")
                        attemptsLeft--

                        if (attemptsLeft > 0) {
                            Log.d("ReservationViewModel", "Nouvelle tentative dans 1 seconde...")
                            delay(1000)
                        }
                    }
                }

                // Charger les réservations peu importe le résultat, car la réservation pourrait avoir été créée
                // même si nous n'avons pas reçu la confirmation correctement
                loadUserReservations()

                // Mise à jour de l'état selon le résultat
                if (success && createdReservation != null) {
                    _state.value = ReservationState.Success(createdReservation)
                    Log.d("ReservationViewModel", "État changé à Success")
                } else {
                    _state.value = ReservationState.Error(
                        lastError?.message ?: "Erreur lors de la création de la réservation"
                    )
                    Log.d("ReservationViewModel", "État changé à Error")
                }
            }
        } else {
            Log.e(
                "ReservationViewModel",
                "Données manquantes: lockerId=$tempLockerId, size=$tempLockerSize, start=$tempStartDate, end=$tempEndDate"
            )
            _state.value = ReservationState.Error("Données de réservation incomplètes")
            Log.d("ReservationViewModel", "État changé à Error (données manquantes)")
        }
    }
    fun reset() {
                Log.d("ReservationViewModel", "Réinitialisation et rechargement des casiers")
                loadLockers()
            }

            fun loadUserReservations() {
                viewModelScope.launch {
                    Log.d("ReservationViewModel", "Chargement des réservations de l'utilisateur")
                    _userReservations.value = Resource.Loading
                    try {
                        val reservations = getUserReservationsUseCase()
                        Log.d("ReservationViewModel", "Réservations chargées: ${reservations.size}")
                        _userReservations.value = Resource.Success(reservations)
                    } catch (e: Exception) {
                        Log.e("ReservationViewModel", "Erreur lors du chargement des réservations", e)
                        _userReservations.value =
                            Resource.Error("Erreur lors du chargement des réservations: ${e.message}")
                    }
                }
            }

            fun cancelReservation(reservationId: String) {
                viewModelScope.launch {
                    try {
                        Log.d("ReservationViewModel", "Annulation de la réservation: $reservationId")
                        cancelReservationUseCase(reservationId)
                        Log.d("ReservationViewModel", "Réservation annulée avec succès")
                        loadUserReservations()
                    } catch (e: Exception) {
                        Log.e("ReservationViewModel", "Erreur lors de l'annulation de la réservation", e)
                    }
                }
            }

            fun setReservationDetails(
                lockerId: String,
                size: LockerSize,
                startDate: LocalDateTime,
                endDate: LocalDateTime,
                price: Double
            ) {
                Log.d(
                    "ReservationViewModel",
                    "Définition des détails de réservation - lockerId: $lockerId, size: $size"
                )
                tempLockerId = lockerId
                tempLockerSize = size
                tempStartDate = startDate
                tempEndDate = endDate
                tempPrice = price
            }

            fun getLockerById(id: String): Locker? {
                return _availableLockers.value.find { locker -> locker.id == id }
            }
        }