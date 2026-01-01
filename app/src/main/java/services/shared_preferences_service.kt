import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesService {
    private const val PREFERENCES_NAME = "house_visitor_management_system"
    private const val IS_LOGIN = "isLogin"

    private lateinit var prefs: SharedPreferences
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getIsLogin(): Boolean {
        return prefs.getBoolean(IS_LOGIN, false);
    }


    fun setIsLogin(value: Boolean?) {
        prefs.edit().putBoolean(IS_LOGIN, value ?: false).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
