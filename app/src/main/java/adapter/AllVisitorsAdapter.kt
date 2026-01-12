package adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.housevisitormanagementsystem.R
import com.example.housevisitormanagementsystem.databinding.ItemAllVisitorBinding
import model.VisitorModel

class AllVisitorsAdapter(
    private val context: Context,
    private val visitorList: List<VisitorModel>
) : RecyclerView.Adapter<AllVisitorsAdapter.VisitorViewHolder>() {

    class VisitorViewHolder(val binding: ItemAllVisitorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val binding = ItemAllVisitorBinding.inflate(LayoutInflater.from(context), parent, false)
        return VisitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {
        val visitor = visitorList[position]
        val binding = holder.binding


        // Image Binding
        if (!visitor.visitorPhoto.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(visitor.visitorPhoto, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivVisitorPhoto.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                binding.ivVisitorPhoto.setImageResource(R.drawable.person_image)
            }
        } else {
            binding.ivVisitorPhoto.setImageResource(R.drawable.person_image)
        }
    }

    override fun getItemCount(): Int = visitorList.size
}
