package com.example.bookbyte.segmentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbyte.PdfSegmentInteraction
import com.example.bookbyte.R
import com.google.firebase.storage.FirebaseStorage

class PdfSegmentAdapter(private val context: Context, private val interaction: PdfSegmentInteraction) : RecyclerView.Adapter<PdfSegmentAdapter.PdfSegmentViewHolder>() {
    private var items = ArrayList<PdfSegment>()

    class PdfSegmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pdfFileName: TextView = view.findViewById(R.id.pdf_file_name)
        val pdfAuthor: TextView = view.findViewById(R.id.pdf_author)
        val deleteButton: ImageView = view.findViewById(R.id.delete)
        val textListener: LinearLayout = view.findViewById(R.id.textListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfSegmentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.segment_item, parent, false)
        return PdfSegmentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PdfSegmentViewHolder, position: Int) {
        val item = items[position]
        holder.pdfFileName.text = item.pdfName
        holder.pdfAuthor.text = "By ${item.author}"

        holder.deleteButton.setOnClickListener {
            interaction.deletePdf(item.fileName)
        }

        holder.textListener.setOnClickListener {
            interaction.navigateToSegmentedTextViewer(item.fileName, item.segmentIndex)
        }
    }

    override fun getItemCount() = items.size

    fun addItem(fileName: String, fileUri: String) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(fileUri)
        ref.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val content = String(bytes)
            val parts = content.split(" ")
            if (parts.size >= 2) {
                val (author, pdfName) = processFileName(parts[0])
                val segmentIndexFromFile = parts[1].toIntOrNull() ?: 0
                items.add(PdfSegment(parts[0], pdfName, author, segmentIndexFromFile))
                notifyItemInserted(items.size - 1)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load segment info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processFileName(fileName: String): Pair<String, String> {
        val parts = fileName.split("-")

        // Assuming the author's name is always the first two parts split by '-'
        val author = if (parts.size >= 2) "${parts[0]} ${parts[1]}" else fileName

        // Combine the rest of the parts to form the full title and then take the first 50 characters
        val fullTitle = parts.drop(2).joinToString("-").dropLast(4)  // Drop the last 4 characters to remove '.pdf'
        var pdfName = if (fullTitle.length > 40) fullTitle.substring(0, 40) else fullTitle
        if (pdfName.length == 40) pdfName += "..."  // Add ellipsis if the title was truncated
        return Pair(author, pdfName)
    }

    data class PdfSegment(
        val fileName: String,
        val pdfName: String,
        val author: String,
        val segmentIndex: Int
    )
}
