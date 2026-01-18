package services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.housevisitormanagementsystem.R
import com.google.android.material.imageview.ShapeableImageView
import java.io.ByteArrayOutputStream

object base64Service {
    fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

}
fun ShapeableImageView.setBase64Image(base64: String?) {
    if (base64.isNullOrEmpty()) {
        setImageResource(R.drawable.person_image)
        return
    }
    try {
        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        setImageBitmap(bitmap)
    } catch (e: Exception) {
        setImageResource(R.drawable.person_image)
    }
}