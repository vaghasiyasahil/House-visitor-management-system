import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import services.NotificationService

object SharedPreferencesService {
    private const val PREFERENCES_NAME = "house_visitor_management_system"
    private const val IS_LOGIN = "isLogin"
    private const val USER_ROLE = "userRole"
    private const val USER_ID = "userId"
    private const val USER_EMAIL = "userEmail"
    private const val USER_HOUSE_NO = "userHouseNo"
    private const val FCM_TOKEN = "fcmToken"
    private const val USER_NAME = "userName"

    private lateinit var prefs: SharedPreferences
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getIsLogin(): Boolean {
        return prefs.getBoolean(IS_LOGIN, false);
    }
    fun getUserRole(): String {
        return prefs.getString(USER_ROLE, "")?:"";
    }
    fun getUserId():String{
        return prefs.getString(USER_ID,"")?:"";
    }
    fun getUserEmail():String{
        return prefs.getString(USER_EMAIL,"")?:"";
    }
    fun getUserHouseNo():String{
        return prefs.getString(USER_HOUSE_NO,"")?:"";
    }
    fun getFcmToken():String{
        return prefs.getString(FCM_TOKEN,"")?:"";
    }
    fun getUserName():String{
        return prefs.getString(USER_NAME,"")?:"";
    }


    fun setIsLogin(value: Boolean?) {
        prefs.edit().putBoolean(IS_LOGIN, value ?: false).apply()
    }
    fun setUserRole(value: String?) {
        prefs.edit().putString(USER_ROLE, value ?: "").apply()
    }
    fun setUserId(value: String?){
        prefs.edit().putString(USER_ID,value?:"").apply()
    }
    fun setUserEmail(value: String?){
        prefs.edit().putString(USER_EMAIL,value?:"").apply()
    }
    fun setUserHouseNo(value: String?){
        prefs.edit().putString(USER_HOUSE_NO,value?:"").apply()
    }
    fun setFcmToken(value: String?){
        prefs.edit().putString(FCM_TOKEN,value?:"").apply()
    }
    fun setUserName(value: String?){
        prefs.edit().putString(USER_NAME,value?:"").apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
        NotificationService().setNewToken();
    }
}
