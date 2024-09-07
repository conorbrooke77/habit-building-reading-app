package com.example.bookbyte.segmentation

import android.content.Context
import android.util.Log
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
            Log.d("ProblemWithFile", "File name is ${item.fileName} when deleting")

            interaction.deletePdf(item.fileName)
        }

        holder.textListener.setOnClickListener {
            Log.d("ProblemWithFile", "File name is ${item.fileName} when sending to segmented text viewer")
            interaction.navigateToSegmentedTextViewer(item.fileName, item.segmentIndex)
        }
    }

    override fun getItemCount() = items.size

    fun addItem(fileName: String, fileUri: String) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(fileUri)
        ref.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val content = String(bytes)

            // Split the content string at the last space to isolate the segment index
            val lastIndex = content.lastIndexOf(" ")
            if (lastIndex != -1) {
                val text = content.substring(0, lastIndex)
                val segmentIndexStr = content.substring(lastIndex + 1)

                // Convert the segment index from string to integer
                val segmentIndexFromFile = segmentIndexStr.toIntOrNull() ?: 0

                val (author, pdfName) = processFileName(fileName)

                // Create and add the new item
                items.add(PdfSegment(text, pdfName, author, segmentIndexFromFile))
                notifyItemInserted(items.size - 1)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load segment info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processFileName(fileName: String): Pair<String, String> {
        // Normalize the fileName by replacing all non-alphanumeric characters (except for periods marking extensions) with spaces
        val normalizedFileName = fileName.replace(Regex("[\\s-_]+"), " ").trim()

        // Handling file extension removal
        val fileWithoutExtension = if (normalizedFileName.endsWith(".txt")) normalizedFileName.dropLast(4) else normalizedFileName

        // Split by space after removing the extension
        val parts = fileWithoutExtension.split(Regex("\\s+"))

        // Assuming the author's name is always the first two parts
        val author = if (parts.size >= 2) "${parts[0]} ${parts[1]}" else fileWithoutExtension

        // Combine the rest of the parts to form the full title
        val fullTitle = parts.drop(2).joinToString(" ")

        // Check if the full title is less than or equal to 40 characters
        var pdfName = if (fullTitle.length <= 40) fullTitle else {
            val truncatedTitle = fullTitle.substring(0, 40)
            if (truncatedTitle.length == 40) "$truncatedTitle..." else truncatedTitle // Add ellipsis if truncated
        }

        return Pair(author, pdfName)
    }



    data class PdfSegment(
        val fileName: String,
        val pdfName: String,
        val author: String,
        val segmentIndex: Int
    )
}
