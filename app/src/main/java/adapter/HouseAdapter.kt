//package adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.widget.Filter
//import android.widget.Filterable
//import androidx.recyclerview.widget.RecyclerView
//import com.example.housevisitormanagementsystem.databinding.ItemHouseBinding
//import model.HouseModel
//import java.util.Locale
//
//class HouseAdapter(
//    private var houseList: ArrayList<HouseModel>,
//    private val onEditClick: (HouseModel) -> Unit,
//    private val onDeleteClick: (HouseModel) -> Unit
//) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>(), Filterable {
//
//    private var houseListFiltered: ArrayList<HouseModel> = houseList
//
//    inner class HouseViewHolder(val binding: ItemHouseBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
//        val binding = ItemHouseBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return HouseViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
//        val house = houseListFiltered[position]
//        with(holder.binding) {
//            tvTitle.text = "House: ${house.houseNo}"
//            tvSubtitle.text = "Owned by ${house.ownerName}"
//            tvEmail.text = house.ownerEmail
//            tvMembers.text = "${house.members} Members"
//
//            btnEdit.setOnClickListener { onEditClick(house) }
//            btnDelete.setOnClickListener { onDeleteClick(house) }
//        }
//    }
//
//    override fun getItemCount(): Int = houseListFiltered.size
//
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val charString = constraint?.toString() ?: ""
//                houseListFiltered = if (charString.isEmpty()) {
//                    houseList
//                } else {
//                    val filteredList = ArrayList<HouseModel>()
//                    for (row in houseList) {
//                        if (row.houseNo.lowercase(Locale.ROOT).contains(charString.lowercase(Locale.ROOT)) ||
//                            row.ownerName.lowercase(Locale.ROOT).contains(charString.lowercase(Locale.ROOT))
//                        ) {
//                            filteredList.add(row)
//                        }
//                    }
//                    filteredList
//                }
//                val filterResults = FilterResults()
//                filterResults.values = houseListFiltered
//                return filterResults
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                houseListFiltered = results?.values as ArrayList<HouseModel>
//                notifyDataSetChanged()
//            }
//        }
//    }
//
//    fun updateList(newList: ArrayList<HouseModel>) {
//        houseList = newList
//        houseListFiltered = newList
//        notifyDataSetChanged()
//    }
//}
