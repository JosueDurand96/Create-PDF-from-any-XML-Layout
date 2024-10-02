package com.durand.create_pdf_from_any_xml_layout

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    val REQUEST_CODE: Int = 1232
    var btnCreatePdf: Button? = null
    var btnXmlToPdf: Button? = null
    var shareToWSP: Button? = null

    override fun onStart() {
        super.onStart()
        askPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnCreatePdf = findViewById(R.id.btnCreatePdf)
        btnCreatePdf!!.setOnClickListener(View.OnClickListener { createPDF() })
        btnXmlToPdf = findViewById(R.id.btnXmlToPdf)
        btnXmlToPdf!!.setOnClickListener(View.OnClickListener { convertXmlToPdf() })
        shareToWSP = findViewById(R.id.shareToWSP)
        shareToWSP!!.setOnClickListener(View.OnClickListener { shareWsp() })
    }

    private fun shareWsp() {
        val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFile = File(downloadDirectory, "exampleXML1.pdf")  // Asegúrate de que este archivo exista
        sharePdfViaWhatsApp(pdfFile)
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun sharePdfViaWhatsApp(pdfFile: File) {
        // Obtener la URI del archivo usando FileProvider para Android 7.0 y superiores
        val pdfUri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",  // Asegúrate de que el provider esté configurado en el manifest
            pdfFile
        )

        // Crear un intent para compartir el archivo
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"  // MIME type para archivos PDF
            putExtra(Intent.EXTRA_STREAM, pdfUri)  // URI del archivo a compartir
            setPackage("com.whatsapp")  // Asegurarte que el intent sea manejado por WhatsApp
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // Verificar que WhatsApp esté instalado y pueda manejar el intent
        if (shareIntent.resolveActivity(packageManager) == null) {
            startActivity(Intent.createChooser(shareIntent, "Compartir PDF con"))
        } else {
            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPDF() {
        val document = PdfDocument()
        val pageInfo = PageInfo.Builder(1080, 1920, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas

        val paint = Paint()
        paint.color = Color.RED
        paint.textSize = 42f

        val text = "Hello, World"
        val x = 500f
        val y = 900f

        canvas.drawText(text, x, y, paint)
        document.finishPage(page)

        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "example.pdf"
        val file = File(downloadsDir, fileName)
        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
            Toast.makeText(this, "Written Successfully!!!", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Log.d("mylog", "Error while writing $e")
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun convertXmlToPdf() {
        // Inflate the XML layout file
        val view = LayoutInflater.from(this).inflate(R.layout.activity_main, null)
        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.display!!.getRealMetrics(displayMetrics)
        } else this.windowManager.defaultDisplay.getMetrics(displayMetrics)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
        )
        Log.d("mylog", "Width Now " + view.measuredWidth)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        // Create a new PdfDocument instance
        val document = PdfDocument()

        // Obtain the width and height of the view
        //int viewWidth = view.getMeasuredWidth();
        //int viewHeight = view.getMeasuredHeight();
        val viewWidth = 1080
        val viewHeight = 1920
        //Log.d("mylog", "Width: " + viewWidth);
        // Create a PageInfo object specifying the page attributes
        val pageInfo = PageInfo.Builder(viewWidth, viewHeight, 1).create()

        // Start a new page
        val page = document.startPage(pageInfo)

        // Get the Canvas object to draw on the page
        val canvas = page.canvas

        // Create a Paint object for styling the view
        val paint = Paint()
        paint.color = Color.WHITE

        // Draw the view on the canvas
        view.draw(canvas)

        // Finish the page
        document.finishPage(page)

        // Specify the path and filename of the output PDF file
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "exampleXML1.pdf"
        val filePath = File(downloadsDir, fileName)

        try {
            // Save the document to a file
            val fos = FileOutputStream(filePath)
            document.writeTo(fos)
            document.close()
            fos.close()
            // PDF conversion successful
            Toast.makeText(this, "XML to PDF Conversion Successful", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Log.d("josue", "hubo un error")
            Log.d("josue", "e :$e")
            e.printStackTrace()
            // Error occurred while converting to PDF
        }
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }
}