import kotlinx.datetime.LocalDateTime

fun LocalDateTime.formatToString(): String {
    return "${dayOfMonth.toString().padStart(2, '0')}/${monthNumber.toString().padStart(2, '0')}/$year ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}