package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.housevisitormanagementsystem.R
import model.House
import java.util.Locale

class HouseSearchAdapter(
    context: Context,
    private val allHouses: List<House>
) : ArrayAdapter<House>(context, android.R.layout.simple_dropdown_item_1line, allHouses) {

    private var filteredHouses: List<House> = allHouses

    override fun getCount(): Int = filteredHouses.size

    override fun getItem(position: Int): House? = filteredHouses[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_dropdown_item_1line, parent, false
        )
        val house = getItem(position)
        val text = view.findViewById<TextView>(android.R.id.text1)
        text.text = "${house?.houseNo} - ${house?.ownerName}"
        text.setTextColor(context.getColor(android.R.color.white))
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()?.lowercase(Locale.ROOT)
                val results = FilterResults()
                
                if (queryString.isNullOrEmpty()) {
                    results.values = allHouses
                    results.count = allHouses.size
                } else {
                    val filteredList = allHouses.filter { house ->
                        house.houseNo.lowercase(Locale.ROOT).contains(queryString) ||
                        house.ownerName.lowercase(Locale.ROOT).contains(queryString)
                    }
                    results.values = filteredList
                    results.count = filteredList.size
                }
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredHouses = results?.values as? List<House> ?: emptyList()
                if (results?.count ?: 0 > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? House)?.houseNo ?: ""
            }
        }
    }
}
