package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Service to fetch real-world time from internet endpoints,
 * providing accurate time-of-day greetings even if system clock drifts.
 */
object InternetTimeService {
    private const val TAG = "InternetTimeService"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
    }

    enum class TimeOfDay {
        MORNING,   // 5:00 - 11:59
        AFTERNOON, // 12:00 - 16:59
        EVENING,   // 17:00 - 21:59
        NIGHT      // 22:00 - 4:59
    }

    data class TimeInfo(
        val greeting: String,
        val hour: Int,
        val timeOfDay: TimeOfDay,
        val isInternetVerified: Boolean,
        val source: String
    )

    /**
     * Resolves the current greeting. Starts with system clock for immediate 0ms render,
     * and asynchronously fetches internet time to ensure high fidelity.
     */
    suspend fun fetchInternetTimeInfo(): TimeInfo = withContext(Dispatchers.IO) {
        // Strategy 1: Check WorldTimeAPI for local IP timezone time
        try {
            val request = Request.Builder()
                .url("https://worldtimeapi.org/api/ip")
                .header("User-Agent", "Triton-Android-App/1.0")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val datetimeStr = json.optString("datetime") // e.g. "2026-09-23T19:48:49.123456+02:00"
                        if (datetimeStr.isNotEmpty()) {
                            val hour = parseHourFromIso(datetimeStr)
                            if (hour != null) {
                                Log.d(TAG, "Resolved internet time from WorldTimeAPI: hour=$hour")
                                return@withContext createTimeInfo(hour, isVerified = true, source = "WorldTimeAPI")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "WorldTimeAPI fallback triggered: ${e.message}")
        }

        // Strategy 2: Check standard HTTP Date header from ultra-reliable endpoint
        try {
            val request = Request.Builder()
                .url("https://clients3.google.com/generate_204")
                .header("User-Agent", "Triton-Android-App/1.0")
                .head()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val dateHeader = response.header("Date") // e.g., "Wed, 23 Sep 2026 19:48:49 GMT"
                if (!dateHeader.isNullOrBlank()) {
                    val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                    val date = format.parse(dateHeader)
                    if (date != null) {
                        val localCal = Calendar.getInstance(TimeZone.getDefault())
                        localCal.time = date
                        val hour = localCal.get(Calendar.HOUR_OF_DAY)
                        Log.d(TAG, "Resolved internet time from HTTP Date header: hour=$hour")
                        return@withContext createTimeInfo(hour, isVerified = true, source = "HTTP Date Header")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "HTTP Date fallback triggered: ${e.message}")
        }

        // Strategy 3: Device clock fallback
        val fallbackHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        createTimeInfo(fallbackHour, isVerified = false, source = "Device Clock")
    }

    fun getLocalTimeInfo(): TimeInfo {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return createTimeInfo(hour, isVerified = false, source = "Device Clock")
    }

    private fun parseHourFromIso(isoString: String): Int? {
        return try {
            val timePart = isoString.substringAfter('T').substringBefore('+').substringBefore('-').substringBefore('Z')
            val hourStr = timePart.substringBefore(':')
            hourStr.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun createTimeInfo(hour: Int, isVerified: Boolean, source: String): TimeInfo {
        val timeOfDay = when (hour) {
            in 5..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..21 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }

        val greeting = when (timeOfDay) {
            TimeOfDay.MORNING -> "Good morning"
            TimeOfDay.AFTERNOON -> "Good afternoon"
            TimeOfDay.EVENING -> "Good evening"
            TimeOfDay.NIGHT -> "Good night"
        }

        return TimeInfo(
            greeting = greeting,
            hour = hour,
            timeOfDay = timeOfDay,
            isInternetVerified = isVerified,
            source = source
        )
    }
}
