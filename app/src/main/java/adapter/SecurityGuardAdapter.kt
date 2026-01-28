//
//
//package adapter
//
//import android.content.Context
//import android.content.res.ColorStateList
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Filter
//import android.widget.Filterable
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.example.housevisitormanagementsystem.R
//import com.example.housevisitormanagementsystem.databinding.ItemSecurityGuardBinding
//import model.SecurityGuardModel
//import java.util.Locale
//
//class SecurityGuardAdapter(
//    private val context: Context,
//    private var guardList: ArrayList<SecurityGuardModel>,
//    private val onEditClick: (SecurityGuardModel) -> Unit,
//    private val onDeleteClick: (SecurityGuardModel) -> Unit
//) : RecyclerView.Adapter<SecurityGuardAdapter.GuardViewHolder>(), Filterable {
//
//    private var guardListFiltered: ArrayList<SecurityGuardModel> = guardList
//
//    inner class GuardViewHolder(val binding: ItemSecurityGuardBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardViewHolder {
//        val binding = ItemSecurityGuardBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return GuardViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: GuardViewHolder, position: Int) {
//        val guard = guardListFiltered[position]
//        with(holder.binding) {
//            tvName.text = guard.name
//            // Using tvId for salary/email or just ID? Let's show Email
//            tvId.text = guard.email
//            tvShiftBadge.text = guard.shift
//            tvLocation.text = "• ${guard.phoneNumber}"
//
//            // Remove/Hide Status Dot as we don't track status yet
//            viewStatusDot.visibility = View.GONE
//
//            ivProfile.setImageResource(R.drawable.ic_person_outline) // Placeholder
//
//            // Shift Badge Styling
//            when (guard.shift.lowercase(Locale.ROOT)) {
//                "morning" -> {
//                    tvShiftBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.badge_blue_bg))
//                    tvShiftBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_blue_text))
//                }
//                "night" -> {
//                    tvShiftBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.badge_purple_bg))
//                    tvShiftBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_purple_text))
//                }
//                else -> {
//                     tvShiftBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.card_dark_bg))
//                     tvShiftBadge.setTextColor(ContextCompat.getColor(context, R.color.text_grey))
//                }
//            }
//
//            btnMore.setOnClickListener { view ->
//                val popup = android.widget.PopupMenu(context, view)
//                popup.menuInflater.inflate(R.menu.menu_guard_options, popup.menu)
//                popup.setOnMenuItemClickListener { item ->
//                    when (item.itemId) {
//                        R.id.action_edit -> {
//                            onEditClick(guard)
//                            true
//                        }
//                        R.id.action_delete -> {
//                            onDeleteClick(guard)
//                            true
//                        }
//                        else -> false
//                    }
//                }
//                popup.show()
//            }
//        }
//    }
//
//    override fun getItemCount(): Int = guardListFiltered.size
//
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val charString = constraint?.toString() ?: ""
//                guardListFiltered = if (charString.isEmpty()) {
//                    guardList
//                } else {
//                    val filteredList = ArrayList<SecurityGuardModel>()
//                    for (row in guardList) {
//                        if (row.name.lowercase(Locale.ROOT).contains(charString.lowercase(Locale.ROOT)) ||
//                            row.email.lowercase(Locale.ROOT).contains(charString.lowercase(Locale.ROOT)) ||
//                            row.phoneNumber.contains(charString)
//                        ) {
//                            filteredList.add(row)
//                        }
//                    }
//                    filteredList
//                }
//                val filterResults = FilterResults()
//                filterResults.values = guardListFiltered
//                return filterResults
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                guardListFiltered = results?.values as ArrayList<SecurityGuardModel>
//                notifyDataSetChanged()
//            }
//        }
//    }
//
//    fun updateList(newList: ArrayList<SecurityGuardModel>) {
//        guardList = newList
//        guardListFiltered = newList
//        notifyDataSetChanged()
//    }
//}
