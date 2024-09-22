package com.example.bookbyte.ui

interface PdfSegmentInteraction {
    fun deletePdf(fileName: String)
    fun navigateToSegmentedTextViewer(fileName: String, segmentIndex: Int)
}
