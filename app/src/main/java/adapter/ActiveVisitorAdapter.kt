package adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.housevisitormanagementsystem.R
import model.VisitorModel
import com.example.housevisitormanagementsystem.databinding.ItemVisitorActiveBinding

class ActiveVisitorAdapter(
    private val context: Context,
    private val logs: List<VisitorModel>,
    private val onCheckoutClick: (VisitorModel) -> Unit
) : RecyclerView.Adapter<ActiveVisitorAdapter.VisitorViewHolder>() {

    class VisitorViewHolder(val binding: ItemVisitorActiveBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val binding = ItemVisitorActiveBinding.inflate(LayoutInflater.from(context), parent, false)
        return VisitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {
        val log = logs[position]
        val binding = holder.binding

        binding.tvVisitorName.text = log.name
        binding.tvEntryTime.text = log.entryTime.toString()
        binding.tvOwnerName.text = log.name

        // Handle Photo
        if (!log.visitorPhoto.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(log.visitorPhoto, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivVisitorImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                binding.ivVisitorImage.setImageResource(R.drawable.person_image)
            }
        } else {
            binding.ivVisitorImage.setImageResource(R.drawable.person_image)
        }

        binding.btnCheckOut.setOnClickListener {
            onCheckoutClick(log)
        }
    }

    override fun getItemCount(): Int = logs.size
}
