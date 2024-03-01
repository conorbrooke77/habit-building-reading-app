package com.example.bookbyte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import android.widget.TextView
import com.example.bookbyte.segmentation.SegmentedTextViewerActivity
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class LibraryActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private var books: MutableList<Book> = mutableListOf()
    private lateinit var readingStreak: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        readingStreak = findViewById(R.id.readingStreak)
        readingStreak.text = App.displayReadingStreak(this)

        gridView = findViewById(R.id.gridView)
        fetchBooks()

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedBook = books[position]
            val intent = Intent(this, BookDetailActivity::class.java).apply {
                putExtra("title", selectedBook.title)
                putExtra("authors", selectedBook.authors)
                putExtra("coverUrl", selectedBook.coverUrl)
                // Pass other data as needed
            }
            startActivity(intent)
        }

    }

    private fun fetchBooks() {
        fetchBookMetadata { fetchedBooks ->
            books.addAll(fetchedBooks)
            runOnUiThread {
                gridView.adapter = BooksAdapter(this@LibraryActivity, books)
            }
        }
    }

    private fun fetchBookMetadata(callback: (List<Book>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val booksMeta = mutableListOf<Book>()
            try {
                val storageReference = FirebaseStorage.getInstance().reference.child("booksMeta")
                val listResult = storageReference.listAll().await()

                listResult.items.forEach { fileRef ->
                    try {
                        val bytes = fileRef.getBytes(Long.MAX_VALUE).await()
                        val jsonStr = String(bytes)
                        val jsonObject = JSONObject(jsonStr)

                        val title = jsonObject.getString("title")
                        val authors = jsonObject.getString("authors")
                        val coverUrl = jsonObject.getString("coverUrl")
                        // You can add more fields as needed, based on your metadata structure

                        val book = Book(title, authors, coverUrl)
                        booksMeta.add(book)
                    } catch (e: Exception) {
                        // Handle individual file errors
                        e.printStackTrace()
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    callback(booksMeta)
                }
            } catch (e: Exception) {
                // Handle errors, such as network issues or permission problems
                e.printStackTrace()
            }
        }
    }
}
