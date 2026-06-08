package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStreamReader

object PdfTextExtractor {
    private const val TAG = "PdfTextExtractor"

    fun extractText(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        // Try getting MIME type
        val mimeType = contentResolver.getType(uri) ?: ""
        val uriPath = uri.path ?: ""
        
        Log.d(TAG, "Extracting text from uri: $uri, mimeType: $mimeType, path: $uriPath")
        
        // If it's a PDF file (mime check or extension helper)
        if (mimeType.contains("pdf", ignoreCase = true) || uriPath.endsWith(".pdf", ignoreCase = true)) {
            try {
                contentResolver.openInputStream(uri).use { inputStream ->
                    if (inputStream == null) {
                        Log.e(TAG, "InputStream was null for Uri: $uri")
                        return null
                    }
                    PDDocument.load(inputStream).use { pdDocument ->
                        val stripper = PDFTextStripper()
                        return stripper.getText(pdDocument)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting text from PDF file Uri: $uri", e)
                // Fallback to text reading in case the extension/mime detection matched incorrectly
            }
        }
        
        // Fallback for TXT files or other text files
        try {
            contentResolver.openInputStream(uri).use { inputStream ->
                if (inputStream == null) return null
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        line = reader.readLine()
                    }
                    return stringBuilder.toString()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting plain text from file Uri: $uri", e)
            return null
        }
    }
}
