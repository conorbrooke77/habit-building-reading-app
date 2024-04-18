package com.example.bookbyte

interface PdfSegmentInteraction {
    fun deletePdf(fileName: String)
    fun navigateToSegmentedTextViewer(fileName: String, segmentIndex: Int)
}
