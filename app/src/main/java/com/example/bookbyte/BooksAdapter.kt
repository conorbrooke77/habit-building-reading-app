package com.example.bookbyte

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class BooksAdapter(private val context: Context, private val books: List<Book>) : BaseAdapter() {

    override fun getCount(): Int = books.size

    override fun getItem(position: Int): Any = books[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_book_cover, parent, false)
        val book = getItem(position) as Book
        val imageView = view.findViewById<ImageView>(R.id.bookCoverImageView)
        view.findViewById<TextView>(R.id.bookTitle).text = book.title
        // Use Glide or Picasso to load the book cover into the imageView
        Glide.with(context).load(book.coverUrl).into(imageView)
        return view
    }
}
