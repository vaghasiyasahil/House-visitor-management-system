import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesService {
    private const val PREFERENCES_NAME = "house_visitor_management_system"
    private const val IS_LOGIN = "isLogin"
    private const val USER_ROLE = "userRole"

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


    fun setIsLogin(value: Boolean?) {
        prefs.edit().putBoolean(IS_LOGIN, value ?: false).apply()
    }
    fun setUserRole(value: String?) {
        prefs.edit().putString(USER_ROLE, value ?: "").apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
