package services
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object MailService {

    private const val TAG = "MailService"
    private const val SENDER_EMAIL = "vaghasiyasahil12@gmail.com"
    private const val SENDER_PASSWORD = "bmmz aaal ojnz feaz"

    suspend fun sendLoginIdAndPassword(
        userRole: String,
        loginEmail: String,
        loginPassword: String
    ): Boolean = withContext(Dispatchers.IO) {

        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL, "House Visitor Management System - Caring Mail"))
                setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(loginEmail)
                )
                subject = "🔐 Login Details for $userRole"

                setContent(
                    """
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family: Arial; padding: 20px;">

                        <h2 style="color:#0F2B45;">👋 Hello $userRole</h2>

                        <p>Your account has been created successfully in 
                        <b>House Visitor Management App</b>.</p>

                        <p style="font-size:18px;">📧 <b>Email:</b> $loginEmail</p>
                        <p style="font-size:18px;">🔑 <b>Password:</b> $loginPassword</p>

                        <p style="margin-top:20px;">
                            Please keep your login details safe and do not share with anyone.
                        </p>

                        <br>
                        <p>With care,</p>
                        <h3>House Visitor Management System</h3>

                    </body>
                    </html>
                    """.trimIndent(),
                    "text/html; charset=utf-8"
                )
            }

            Transport.send(message)
            Log.d(TAG, "Email sent successfully to $loginEmail")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Mail send error", e)
            false
        }
    }
}
