package com.example.bookbyte.PDF

import android.content.Context
import android.view.*
import android.widget.ArrayAdapter
import com.example.bookbyte.DEBUG
import com.example.bookbyte.R

class PdfListAdapter(private val classContext: Context, private val pdfList: List<String>) : ArrayAdapter<String>(classContext, 0, pdfList) {

    // This method provides the view for each item in the list
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView: View
        val viewHolder: ViewHolder

        // Check if an existing view is being reused, otherwise inflate a new view (Stops duplicates)
        if (convertView == null) {
            // Inflate a new layout for the list item
            val layoutInflater = LayoutInflater.from(context)
            rowView = layoutInflater.inflate(R.layout.list_item_pdf, parent, false)

            // Using a class to optimize loading views
            viewHolder = ViewHolder(rowView)

            // Store the ViewHolder in the view's tag for future reuse
            rowView.tag = viewHolder
        } else {
            // Reuse an existing view
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        // Get the current PDF URI and set it as the text of the TextView in the ViewHolder
        val pdf = getItem(position)
        viewHolder.textView.text = "Reading Material: ${position + 1}"

        // Set an OnClickListener to handle clicks on the list item

        rowView.setOnClickListener {
            DEBUG.messageBox(classContext, "Test")
        }

        return rowView
    }
}