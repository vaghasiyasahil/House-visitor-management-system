package services

import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

object SendNotificationService {
    private const val TAG = "FCM_HTTP_V1"
    private const val PROJECT_ID = "house-visitor-management"
    private const val FCM_URL = "https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send"
    private const val SERVICE_ACCOUNT_JSON = """
{
  "type": "service_account",
  "project_id": "house-visitor-management",
  "private_key_id": "22c4a932c220f8302001e39eb63b059eeb34c201",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7gFvDt5ZnmAKQ\nZQ4U7iM2JmxTedSMlKBwVB79KsusYc0FQeQ7GOhKwZy0ddTuA2CcuIHCnj6n+GM8\nj5yT5ACJnQ3PiK1aoF3e101CRZSQWJXifW+OyMYNFBVFeKHpGv8iYUO6meBz3ifw\n23mnuABjvg2ALXzMWiU/HxSp6j0MgbG7uTu24XmMcTTJvhfJMVTpL8C5ZaJdsF9M\nzefRv8gJcbv7/xWwPYomZdX4PDKV+RtWiyqWwQSfCnEXkEK6umwVR3wlfhqlG7pS\n0tEtxD7R60jSi7FjrpUPz6vRb7KN8RcM+kfDP+Aud8ATPOok9xxpkfkTXivDV3mQ\n2Ro2AhgtAgMBAAECggEAALYwQvopqTt/uP4VM1mjCxPSwe9aseznff2nv3S/4Kff\nYBd7rwi9avk4p8uxsvSS0410uqEgokEUWVi/MqWBJgSRVpoRdGBa8ttU2P1MHs8j\nRICO4Zcb/1U17nVSggC++qmtWOI4IqBzfNvMZqlRYQ+J3BRwHpNyBD8RNELmF+vN\nb43aFaJF+lGfRMkYYQC5zP9FILWzo6lgUKDhPYy8PxRl+sOqUyHBe8M52A7UxH0K\n3gVTPScYKDIoymjFIEAJYgRIY9f1QcxKzesQn1rGNRDBcA1XPGtT7iHvX9nxLvrw\nqBtxO43OPmDZf1PIrjmsuKMNe1oviDvM58Q0CZ87IQKBgQDjynWu/nHEmt/YF51I\nYCNC1HsmUHv6uV1sG4yvHg2Pd2Dw6pBuOOFv809edkjFTtfjppgdSVbtiEhYJNQD\nTIZPP9x5IeXIlJ3WDDj6P9blJ5DLqieADQcQpA5kVUrcL5VNGwPbQ/yB2u/ms0DE\nEnpY9dYmZyEqK6s2T/3bozyKIQKBgQDSuJ9CHz1rx4b7BaEG4iEN/f3NHbQJjSb6\nZEEZYq0HGssB9cDk6L1+x5V05l6jPITvlP5c06HcIPs6lQVH5nI9MmKeBxiaWw3d\n4mTL8uwACZZu8pbWw2u5dnFBh6c+6CMLgStVFoIY7Uq+LYZNr1Lhfb5GCPJBOa2p\n14O2IlGEjQKBgHhtmTojG1Kt7pn4IhcZfIrgexsOgf1T8qgtCHusj87BdmboG+Op\n/sWJBPUT4/UsccbRVKpnE6YQ3u56yfvUgU10ncMMwMcCi0+WtqjXB22vNUX3d1qj\niJCrq1AwJ5uxLGXPWoKZtu2QKjT1n9AJVNWjYsx6yXXaEHJuZ++upVsBAoGBAKoN\nA7jArTiFVJDPAhwj8Ljx+BfHmLr8hjAbJ83sq+XH4Xw7budMS2A8gIhOQKFK/Zy6\niGsw/rY+h3/HMvPLLPXl61/MKjamtJITWXSdHYcKNbRjYQb/vbd1tgBEENvbSizG\nmMmZ2HN1S5Qd+PxIOIJwj1TFc3rfqR4fJdOd7lshAoGBALg/k7g95wjS6SVJXDFV\nDn+mVDauPc14meA/PkhboSE9UnyIJnPAuLMPOZesm/9KpL++RAkSzqaIDihOXUIn\n1YlPRo3S31jxXSGBtkCaRvzDcvv8ciQSplEOMyzvwaam5nShOctTqfXGFZJPXSAk\nb+JmkHppRDbbnwrwnGPVC0WG\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-fbsvc@house-visitor-management.iam.gserviceaccount.com",
  "client_id": "103228683096826058155",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40house-visitor-management.iam.gserviceaccount.com",
  "universe_domain": "googleapis.com"
}
"""

    private fun getAccessToken(): String {
        val credentials = GoogleCredentials
            .fromStream(ByteArrayInputStream(SERVICE_ACCOUNT_JSON.toByteArray()))
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    suspend fun sendNotification(
        title: String,
        body: String,
        fcmToken: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            val notification = JSONObject().apply {
                put("title", title)
                put("body", body)
            }
            val message = JSONObject().apply {
                if (fcmToken.isNullOrEmpty()) {
                    put("topic", "allUsers")
                } else {
                    put("token", fcmToken)
                }
                put("notification", notification)
            }
            val payload = JSONObject().apply {
                put("message", message)
            }
            sendRequest(payload.toString(), accessToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
        }
    }

    private fun sendRequest(jsonBody: String, accessToken: String) {
        val client = OkHttpClient()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(FCM_URL)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            Log.d(TAG, "Response: ${response.code} ${response.body?.string()}")
        }
    }
}