package org.perso.bikerbox.data.repository.firebase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalTime
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.repository.LockersRepository
import org.perso.bikerbox.data.services.PricingService
import java.time.ZoneId
import java.util.UUID

class FirebaseLockersRepository : LockersRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val lockersCollection = firestore.collection("lockers")
    private val reservationsCollection = firestore.collection("reservations")
    private val TAG = "FirebaseLockersRepo"

    override suspend fun getAvailableLockers(): List<Locker> {
        return try {
            Log.d(TAG, "Tentative de récupération des casiers depuis Firestore")
            val snapshot = lockersCollection.get().await()
            Log.d(TAG, "Nombre de documents récupérés: ${snapshot.documents.size}")

            val lockers = snapshot.documents.mapNotNull { doc ->
                try {
                    // Affichage du document complet pour le débogage
                    Log.d(TAG, "Document ID ${doc.id}: ${doc.data}")

                    // Récupération des tailles disponibles
                    val sizesRaw = doc.get("availableSizes") as? List<String>
                    Log.d(TAG, "Tailles disponibles pour ${doc.id}: $sizesRaw")

                    if (sizesRaw == null) {
                        Log.e(TAG, "availableSizes est null pour le document ${doc.id}")
                        return@mapNotNull null
                    }

                    val sizes = sizesRaw.mapNotNull { sizeStr ->
                        try {
                            LockerSize.valueOf(sizeStr)
                        } catch (e: Exception) {
                            Log.e(TAG, "Erreur lors de la conversion de la taille $sizeStr: ${e.message}")
                            null
                        }
                    }

                    // Récupération des compteurs par taille
                    val availableCountRaw = doc.get("availableCount") as? Map<*, *>
                    Log.d(TAG, "Compteurs disponibles pour ${doc.id}: $availableCountRaw")

                    if (availableCountRaw == null) {
                        Log.e(TAG, "availableCount est null pour le document ${doc.id}")
                        return@mapNotNull null
                    }

                    val availableCount = mutableMapOf<LockerSize, Int>()

                    availableCountRaw.forEach { (key, value) ->
                        val sizeStr = key.toString()
                        try {
                            val size = LockerSize.valueOf(sizeStr)
                            val count = when (value) {
                                is Long -> value.toInt()
                                is Double -> value.toInt()
                                is String -> value.toIntOrNull() ?: 0
                                else -> 0
                            }
                            availableCount[size] = count
                        } catch (e: Exception) {
                            Log.e(TAG, "Erreur lors de la conversion du compteur pour $sizeStr: ${e.message}")
                        }
                    }

                    val name = doc.getString("name")
                    val location = doc.getString("location")

                    if (name == null || location == null) {
                        Log.e(TAG, "name ou location manquant pour ${doc.id}")
                        return@mapNotNull null
                    }

                    Log.d(
                        TAG,
                        "Création du casier: name=$name, location=$location, sizes=$sizes, counts=$availableCount"
                    )

                    Locker(
                        id = doc.id,
                        name = name,
                        location = location,
                        availableSizes = sizes,
                        availableCount = availableCount
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la conversion du document ${doc.id}: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }

            Log.d(TAG, "Nombre de casiers récupérés avec succès: ${lockers.size}")
            if (lockers.isEmpty()) {
                Log.w(TAG, "Aucun casier récupéré, retour de données fictives")
                return getFallbackLockers()
            }

            lockers
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des casiers: ${e.message}")
            e.printStackTrace()
            getFallbackLockers()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getLockerById(id: String): Locker? {
        return try {
            val document = lockersCollection.document(id).get().await()
            if (!document.exists()) {
                Log.e(TAG, "Le casier avec l'ID $id n'existe pas")
                return null
            }

            val sizesRaw = document.get("availableSizes") as? List<*>
            val availableCountRaw = document.get("availableCount") as? Map<*, *>
            val name = document.getString("name")
            val location = document.getString("location")

            if (sizesRaw == null || availableCountRaw == null || name == null || location == null) {
                Log.e(TAG, "Données manquantes pour le casier $id")
                return null
            }

            val sizes = sizesRaw.mapNotNull { sizeStr ->
                try {
                    LockerSize.valueOf(sizeStr as String)
                } catch (e: Exception) {
                    null
                }
            }

            val availableCount = mutableMapOf<LockerSize, Int>()
            availableCountRaw.forEach { (key, value) ->
                try {
                    val sizeStr = key.toString()
                    val size = LockerSize.valueOf(sizeStr)
                    val count = when (value) {
                        is Long -> value.toInt()
                        is Double -> value.toInt()
                        is String -> value.toIntOrNull() ?: 0
                        else -> 0
                    }
                    availableCount[size] = count
                } catch (e: Exception) {
                    // Ignorer les entrées incorrectes
                }
            }

            Locker(
                id = document.id,
                name = name,
                location = location,
                availableSizes = sizes,
                availableCount = availableCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du casier $id: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAvailability(
        lockerId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Map<LockerSize, Int> {
        return try {
            val locker = getLockerById(lockerId) ?: return emptyMap()

            // Convertir les dates pour la requête Firestore
            val startTimestamp = convertToTimestamp(startDate)
            val endTimestamp = convertToTimestamp(endDate)

            Log.d(TAG, "Recherche des réservations pour $lockerId entre $startDate et $endDate")
            Log.d(TAG, "Timestamps: start=$startTimestamp, end=$endTimestamp")

            // Trouver les réservations qui se chevauchent avec la période demandée
            val overlappingReservations = reservationsCollection
                .whereEqualTo("lockerId", lockerId)
                .whereEqualTo("status", "CONFIRMED") // Ignorer les réservations annulées
                .get()
                .await()

            Log.d(TAG, "Nombre total de réservations pour ce casier: ${overlappingReservations.size()}")

            // Filtrer manuellement les réservations qui se chevauchent
            val filteredReservations = overlappingReservations.documents.filter { doc ->
                try {
                    val resStartTimestamp = doc.getLong("startDate") ?: return@filter false
                    val resEndTimestamp = doc.getLong("endDate") ?: return@filter false

                    // Chevauchement si la réservation commence avant la fin de notre période
                    // ET se termine après le début de notre période
                    val overlap = resStartTimestamp < endTimestamp && resEndTimestamp > startTimestamp

                    if (overlap) {
                        Log.d(TAG, "Réservation ${doc.id} chevauche la période demandée")
                        Log.d(TAG, "Période réservée: $resStartTimestamp à $resEndTimestamp")
                    }

                    overlap
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors du filtrage d'une réservation: ${e.message}")
                    false
                }
            }

            Log.d(TAG, "Nombre de réservations qui se chevauchent: ${filteredReservations.size}")

            // Compter les réservations par taille
            val reservedCount = mutableMapOf<LockerSize, Int>()

            // CHANGEMENT ICI: Utiliser filteredReservations au lieu de overlappingReservations.documents
            filteredReservations.forEach { doc ->
                val sizeStr = doc.getString("size") ?: return@forEach
                try {
                    val size = LockerSize.valueOf(sizeStr)
                    reservedCount[size] = (reservedCount[size] ?: 0) + 1
                } catch (e: Exception) {
                    Log.e(TAG, "Taille de casier non reconnue: $sizeStr")
                }
            }

            // Calculer les disponibilités
            val availableCount = mutableMapOf<LockerSize, Int>()
            locker.availableCount.forEach { (size, total) ->
                val reserved = reservedCount[size] ?: 0
                availableCount[size] = (total - reserved).coerceAtLeast(0)
            }

            availableCount
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des disponibilités: ${e.message}")
            e.printStackTrace()
            emptyMap()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun makeReservation(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Reservation {
        // Vérifier la disponibilité
        val isAvailable = checkAvailability(lockerId, size, startDate, endDate)
        if (!isAvailable) {
            throw IllegalStateException("Ce casier n'est pas disponible pour les dates demandées")
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Utilisateur non authentifié")

        try {
            // Convertir les dates en timestamps
            val startTimestamp = convertToTimestamp(startDate)
            val endTimestamp = convertToTimestamp(endDate)

            // Créer l'ID de la réservation
            val reservationId = UUID.randomUUID().toString()

            // Récupérer le locker pour obtenir le nom
            val locker = getLockerById(lockerId)
            val lockerName = locker?.name ?: "Casier inconnu"

            // Générer un code de réservation aléatoire
            val code = "A${(100..999).random()}"

            // Calculer le prix en utilisant le PricingService existant
            val price = PricingService.calculatePrice(size, startDate, endDate)

            // Créer les données de la réservation
            val reservationData = hashMapOf(
                "id" to reservationId,
                "userId" to userId,
                "lockerId" to lockerId,
                "size" to size.name,
                "startDate" to startTimestamp,
                "endDate" to endTimestamp,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to "CONFIRMED",
                "code" to code,
                "price" to price
            )

            // Sauvegarder la réservation
            reservationsCollection.document(reservationId).set(reservationData).await()

            // Renvoyer l'objet Reservation
            return Reservation(
                id = reservationId,
                lockerId = lockerId,
                lockerName = locker?.name ?: "Casier inconnu",
                size = size,
                startDate = startDate,
                endDate = endDate,
                status = "CONFIRMED",
                code = code,
                price = price
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création de la réservation: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun checkAvailability(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Boolean {
        val availability = getAvailability(lockerId, startDate, endDate)
        val availableCount = availability[size] ?: 0
        return availableCount > 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getUserReservations(): List<Reservation> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = reservationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val lockerId = doc.getString("lockerId") ?: return@mapNotNull null

                    // Récupérer le nom du casier
                    val locker = getLockerById(lockerId)
                    val lockerName = locker?.name ?: "Casier inconnu"

                    // Récupérer la taille
                    val sizeStr = doc.getString("size") ?: return@mapNotNull null
                    val size = try {
                        LockerSize.valueOf(sizeStr)
                    } catch (e: Exception) {
                        Log.e(TAG, "Taille de casier non reconnue: $sizeStr")
                        return@mapNotNull null
                    }

                    // Récupérer les dates
                    val startTimestamp = doc.getLong("startDate") ?: return@mapNotNull null
                    val endTimestamp = doc.getLong("endDate") ?: return@mapNotNull null

                    // Convertir les timestamps en LocalDateTime
                    val startDateTime = convertFromTimestamp(startTimestamp)
                    val endDateTime = convertFromTimestamp(endTimestamp)

                    val status = doc.getString("status") ?: "CONFIRMED"
                    val code = doc.getString("code") ?: "A${(100..999).random()}"
                    val price = doc.getDouble("price") ?: 0.0


                    Reservation(
                        id = id,
                        lockerId = lockerId,
                        lockerName = lockerName,
                        size = size,
                        startDate = startDateTime,
                        endDate = endDateTime,
                        status = status,
                        code = code,
                        price = price
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la conversion d'une réservation: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des réservations: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun cancelReservation(reservationId: String) {
        try {
            // Vérifier que la réservation existe et appartient à l'utilisateur
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("Utilisateur non authentifié")
            val reservationDoc = reservationsCollection.document(reservationId).get().await()

            if (!reservationDoc.exists()) {
                throw IllegalArgumentException("La réservation n'existe pas")
            }

            val reservationUserId = reservationDoc.getString("userId")
            if (reservationUserId != userId) {
                throw SecurityException("Cette réservation n'appartient pas à l'utilisateur courant")
            }

            // Mettre à jour le statut de la réservation
            val updates = hashMapOf<String, Any>(
                "status" to "CANCELLED",
                "cancelledAt" to FieldValue.serverTimestamp()
            )

            reservationsCollection.document(reservationId).update(updates).await()
            Log.d(TAG, "Réservation $reservationId annulée avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'annulation de la réservation: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // Méthode de secours qui retourne des casiers fictifs en cas d'erreur
    private fun getFallbackLockers(): List<Locker> {
        Log.d(TAG, "Utilisation des casiers de secours")
        return listOf(
            Locker(
                id = "fallback1",
                name = "Casier Gare (Secours)",
                location = "Gare Centrale",
                availableSizes = listOf(LockerSize.SINGLE, LockerSize.DOUBLE),
                availableCount = mutableMapOf(
                    LockerSize.SINGLE to 5,
                    LockerSize.DOUBLE to 3
                )
            ),
            Locker(
                id = "fallback2",
                name = "Casier Centre (Secours)",
                location = "Centre Commercial",
                availableSizes = listOf(LockerSize.SINGLE, LockerSize.DOUBLE),
                availableCount = mutableMapOf(
                    LockerSize.SINGLE to 8,
                    LockerSize.DOUBLE to 4
                )
            )
        )
    }

    // Méthodes utilitaires pour la conversion de dates
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToTimestamp(dateTime: LocalDateTime): Long {
        // Convertir kotlinx.datetime.LocalDateTime en java.time.LocalDateTime
        val javaDateTime = java.time.LocalDateTime.of(
            dateTime.year,
            dateTime.monthNumber,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute,
            dateTime.second,
            dateTime.nanosecond
        )

        // Convertir en timestamp
        return javaDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertFromTimestamp(timestamp: Long): LocalDateTime {
        // Convertir timestamp en java.time.LocalDateTime
        val instant = java.time.Instant.ofEpochMilli(timestamp)
        val javaDateTime = java.time.LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        // Convertir en kotlinx.datetime.LocalDateTime
        return LocalDateTime(
            javaDateTime.year,
            javaDateTime.monthValue,
            javaDateTime.dayOfMonth,
            javaDateTime.hour,
            javaDateTime.minute,
            javaDateTime.second,
            javaDateTime.nano / 1000000
        )
    }
}