package org.perso.bikerbox.data.repository.firebase

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.repository.LockersRepository
import org.perso.bikerbox.data.services.PricingService
import java.time.ZoneId
import java.util.*

class FirebaseLockersRepository : LockersRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val lockersCollection by lazy { firestore.collection("lockers") }
    private val reservationsCollection by lazy { firestore.collection("reservations") }


    override suspend fun getAvailableLockers(): List<Locker> {
        return try {
            val snapshot = lockersCollection.get().await()

            val lockers = snapshot.documents.mapNotNull { doc ->
                try {

                    val sizesRaw = doc.get("availableSizes") as? List<*>

                    if (sizesRaw == null) {
                        return@mapNotNull null
                    }

                    val sizes = sizesRaw.mapNotNull { sizeStr ->
                        try {
                            LockerSize.valueOf(sizeStr as String)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    val availableCountRaw = doc.get("availableCount") as? Map<*, *>

                    if (availableCountRaw == null) {
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
                        } catch (_: Exception) {
                        }
                    }

                    val name = doc.getString("name")
                    val location = doc.getString("location")
                    val latitude = doc.getDouble("latitude") ?: 0.0
                    val longitude = doc.getDouble("longitude") ?: 0.0

                    if (name == null || location == null) {
                        return@mapNotNull null
                    }

                    Locker(
                        id = doc.id,
                        name = name,
                        location = location,
                        availableSizes = sizes,
                        availableCount = availableCount,
                        latitude = latitude,
                        longitude = longitude
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (lockers.isEmpty()) {
                return getFallbackLockers()
            }

            lockers
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackLockers()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getLockerById(id: String): Locker? {
        return try {
            val document = lockersCollection.document(id).get().await()
            if (!document.exists()) {
                return null
            }

            val sizesRaw = document.get("availableSizes") as? List<*>
            val availableCountRaw = document.get("availableCount") as? Map<*, *>
            val name = document.getString("name")
            val location = document.getString("location")
            val latitude = document.getDouble("latitude") ?: 0.0
            val longitude = document.getDouble("longitude") ?: 0.0

            if (sizesRaw == null || availableCountRaw == null || name == null || location == null) {
                return null
            }

            val sizes = sizesRaw.mapNotNull { sizeStr ->
                try {
                    LockerSize.valueOf(sizeStr as String)
                } catch (_: Exception) {
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
                } catch (_: Exception) {
                }
            }

            Locker(
                id = document.id,
                name = name,
                location = location,
                availableSizes = sizes,
                availableCount = availableCount,
                latitude = latitude,
                longitude = longitude
            )
        } catch (e: Exception) {
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

            val startTimestamp = convertToTimestamp(startDate)
            val endTimestamp = convertToTimestamp(endDate)

            val overlappingReservations = reservationsCollection
                .whereEqualTo("lockerId", lockerId)
                .whereEqualTo("status", "CONFIRMED") // Ignorer les réservations annulées
                .get()
                .await()

            val filteredReservations = overlappingReservations.documents.filter { doc ->
                try {
                    val resStartTimestamp = doc.getLong("startDate") ?: return@filter false
                    val resEndTimestamp = doc.getLong("endDate") ?: return@filter false

                    val overlap = resStartTimestamp < endTimestamp && resEndTimestamp > startTimestamp

                    overlap
                } catch (_: Exception) {
                    false
                }
            }


            val reservedCount = mutableMapOf<LockerSize, Int>()

            filteredReservations.forEach { doc ->
                val sizeStr = doc.getString("size") ?: return@forEach
                try {
                    val size = LockerSize.valueOf(sizeStr)
                    reservedCount[size] = (reservedCount[size] ?: 0) + 1
                } catch (_: Exception) { }
            }

            val availableCount = mutableMapOf<LockerSize, Int>()
            locker.availableCount.forEach { (size, total) ->
                val reserved = reservedCount[size] ?: 0
                availableCount[size] = (total - reserved).coerceAtLeast(0)
            }

            availableCount
        } catch (e: Exception) {
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
        val isAvailable = checkAvailability(lockerId, size, startDate, endDate)
        if (!isAvailable) {
            throw IllegalStateException("Ce casier n'est pas disponible pour les dates demandées")
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Utilisateur non authentifié")

        try {
            val startTimestamp = convertToTimestamp(startDate)
            val endTimestamp = convertToTimestamp(endDate)
            val reservationId = UUID.randomUUID().toString()
            val locker = getLockerById(lockerId)
            val code = "A${(100..999).random()}"
            val price = PricingService.calculatePrice(size, startDate, endDate)
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

            reservationsCollection.document(reservationId).set(reservationData).await()

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
                    val locker = getLockerById(lockerId)
                    val lockerName = locker?.name ?: "Casier inconnu"
                    val sizeStr = doc.getString("size") ?: return@mapNotNull null
                    val size = try {
                        LockerSize.valueOf(sizeStr)
                    } catch (_: Exception) {
                        return@mapNotNull null
                    }

                    val startTimestamp = doc.getLong("startDate") ?: return@mapNotNull null
                    val endTimestamp = doc.getLong("endDate") ?: return@mapNotNull null
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
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun cancelReservation(reservationId: String) {
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("Utilisateur non authentifié")
            val reservationDoc = reservationsCollection.document(reservationId).get().await()

            if (!reservationDoc.exists()) {
                throw IllegalArgumentException("La réservation n'existe pas")
            }

            val reservationUserId = reservationDoc.getString("userId")
            if (reservationUserId != userId) {
                throw SecurityException("Cette réservation n'appartient pas à l'utilisateur courant")
            }

            reservationsCollection.document(reservationId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun getFallbackLockers(): List<Locker> {
        return listOf(
            Locker(
                id = "fallback1",
                name = "Locker de secours A",
                location = "Paris Centre",
                availableSizes = listOf(LockerSize.SMALL, LockerSize.MEDIUM),
                availableCount = mutableMapOf(LockerSize.SMALL to 5, LockerSize.MEDIUM to 3),
                latitude = 48.8566,
                longitude = 2.3522
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToTimestamp(dateTime: LocalDateTime): Long {
        val javaDateTime = java.time.LocalDateTime.of(
            dateTime.year,
            dateTime.monthNumber,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute,
            dateTime.second,
            dateTime.nanosecond
        )

        return javaDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertFromTimestamp(timestamp: Long): LocalDateTime {
        val instant = java.time.Instant.ofEpochMilli(timestamp)
        val javaDateTime = java.time.LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

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