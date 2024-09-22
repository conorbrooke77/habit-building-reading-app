package com.example.bookbyte.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridView
import com.example.bookbyte.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GutenbergLibraryActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private var books: MutableList<Book> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        gridView = findViewById(R.id.gridView)
        fetchBooks()

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedBook = books[position]
            val intent = Intent(this, BookDetailActivity::class.java).apply {
                putExtra("title", selectedBook.title)
                putExtra("authors", selectedBook.authors)
                putExtra("coverUrl", selectedBook.coverUrl)
                putExtra("filename", selectedBook.filename)
                // Pass other data as needed
            }
            startActivity(intent)
        }

    }

    private fun fetchBooks() {
        fetchBookMetadata { fetchedBooks ->
            books.addAll(fetchedBooks)
            runOnUiThread {
                gridView.adapter = BooksAdapter(this@GutenbergLibraryActivity, books)
            }
        }
    }

    private fun fetchBookMetadata(callback: (List<Book>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val books = mutableListOf<Book>()
            try {
                val storageReference = FirebaseStorage.getInstance().reference.child("bookImages")
                val listResult = storageReference.listAll().await()

                listResult.items.forEach { fileRef ->
                    try {
                        val coverUrl = fileRef.downloadUrl.await().toString() // Asynchronously get the download URL
                        var fileName = fileRef.name
                        fileName = fileName.replace(".jpg", ".pdf")

                        val (author, title) = parseFilename(fileRef.name)

                        // Assuming BookImage is a data class that holds the filename and URL
                        val book = Book(title, author, coverUrl, fileName)
                        books.add(book)
                    } catch (e: Exception) {
                        // Handle individual file errors
                        Log.e("fetchBookImages", "Error fetching image details", e)
                    }
                }

                // Callback on the main thread
                CoroutineScope(Dispatchers.Main).launch {
                    callback(books)
                }
            } catch (e: Exception) {
                // Handle errors, such as network issues or permission problems
                Log.e("fetchBookImages", "Failed to fetch book images", e)
            }
        }
    }

    private fun parseFilename(filename: String): Pair<String, String> {
        // Remove the '.pdf' extension
        val baseName = filename.removeSuffix(".jpg")

        // Split the filename into parts based on the hyphen separator
        val parts = baseName.split("-")

        // Check if there are at least two parts to form the author's name
        if (parts.size >= 2) {
            val author = "${parts[0]} ${parts[1]}"
            val title = parts.drop(2).joinToString(" ")

            return Pair(author, title)
        }

        // Return a default value if the format is not as expected
        return Pair("Unknown", "Unknown Title")
    }
    fun navigateToSettings(view: View) {

        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

}
