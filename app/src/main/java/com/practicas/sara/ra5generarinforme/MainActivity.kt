package com.practicas.sara.ra5generarinforme

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import org.json.JSONArray
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private val codigoCrearPdf = 100
    private lateinit var spinnerCiudad: Spinner
    private lateinit var botonGenerarInforme: Button
    private lateinit var botonExportarPdf: Button
    private lateinit var contenedorInforme: LinearLayout
    private lateinit var textoCiudadSeleccionada: TextView
    private lateinit var textoNumeroOfertas: TextView
    private lateinit var textoSalarioTotal: TextView
    private lateinit var textoSalarioMedio: TextView
    private lateinit var textoSalarioMaximo: TextView
    private lateinit var barraValencia: ProgressBar
    private lateinit var barraMadrid: ProgressBar
    private lateinit var barraAlicante: ProgressBar
    private lateinit var textoCantidadValencia: TextView
    private lateinit var textoCantidadMadrid: TextView
    private lateinit var textoCantidadAlicante: TextView
    private lateinit var contenedorOfertas: LinearLayout

    private val ofertas = ArrayList<Oferta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Para que se vea bien en pantallas modernas
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Vincular variables (FASE 5)
        spinnerCiudad = findViewById(R.id.spinnerCiudad)
        botonGenerarInforme = findViewById(R.id.botonGenerarInforme)
        botonExportarPdf = findViewById(R.id.botonExportarPdf)
        contenedorInforme = findViewById(R.id.contenedorInforme)
        textoCiudadSeleccionada = findViewById(R.id.textoCiudadSeleccionada)
        textoNumeroOfertas = findViewById(R.id.textoNumeroOfertas)
        textoSalarioTotal = findViewById(R.id.textoSalarioTotal)
        textoSalarioMedio = findViewById(R.id.textoSalarioMedio)
        textoSalarioMaximo = findViewById(R.id.textoSalarioMaximo)
        barraValencia = findViewById(R.id.barraValencia)
        barraMadrid = findViewById(R.id.barraMadrid)
        barraAlicante = findViewById(R.id.barraAlicante)
        textoCantidadValencia = findViewById(R.id.textoCantidadValencia)
        textoCantidadMadrid = findViewById(R.id.textoCantidadMadrid)
        textoCantidadAlicante = findViewById(R.id.textoCantidadAlicante)
        contenedorOfertas = findViewById(R.id.contenedorOfertas)

        // 3. Configurar Spinner
        configurarSpinner()

        // 4. Cargar las ofertas del JSON (FASE 7)
        ofertas.addAll(cargarOfertas())

        botonGenerarInforme.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)
            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)
        }

        botonExportarPdf.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)

            // Generamos el informe en pantalla primero
            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)

            // contenedorInforme.post espera a que Android dibuje la pantalla antes de crear el PDF
            contenedorInforme.post {
                abrirSelectorParaGuardarPdf()
            }
        }

    } // <-- AQUÍ SE CIERRA EL ONCREATE

    // (FASE 6) Configurar el Spinner (Ahora está al mismo nivel que onCreate)
    private fun configurarSpinner() {
        val ciudades = arrayOf("Todas", "Valencia", "Madrid", "Alicante")
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ciudades
        )
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCiudad.adapter = adaptador
    }

    // (FASE 7) Leer el JSON y convertirlo en objetos Oferta
    private fun cargarOfertas(): ArrayList<Oferta> {
        val listaOfertas = ArrayList<Oferta>()

        // Abrimos el archivo JSON de la carpeta assets y lo leemos como texto
        val textoJson = assets.open("ofertas.json").bufferedReader().use { it.readText() }

        // Lo convertimos en un array JSON
        val arrayJson = JSONArray(textoJson)

        // Recorremos cada objeto del array
        for (i in 0 until arrayJson.length()) {
            val objetoJson = arrayJson.getJSONObject(i)

            // Creamos el objeto Oferta
            val oferta = Oferta(
                objetoJson.getInt("id"),
                objetoJson.getString("puesto"),
                objetoJson.getString("empresa"),
                objetoJson.getString("ciudad"),
                objetoJson.getInt("salario")
            )

            // Lo añadimos a la lista
            listaOfertas.add(oferta)
        }

        return listaOfertas
    }
    private fun filtrarOfertasPorCiudad(ciudad: String): List<Oferta> {
        // filter devuelve una nueva lista solo con las ofertas de la ciudad elegida
        return if (ciudad == "Todas") {
            ofertas
        } else {
            ofertas.filter { it.ciudad == ciudad }
        }
    }

    private fun mostrarResultado(ciudad: String, ofertasFiltradas: List<Oferta>) {
        // Cálculos del informe
        val numeroOfertas = ofertasFiltradas.size
        val salarioTotal = ofertasFiltradas.sumOf { it.salario }
        val salarioMedio = if (ofertasFiltradas.size > 0) {
            ofertasFiltradas.map { it.salario }.average()
        } else {
            0.0
        }
        val salarioMaximo = ofertasFiltradas.maxOfOrNull { it.salario } ?: 0

        // Asignar los textos a los TextView
        textoCiudadSeleccionada.text = "Ciudad seleccionada: $ciudad"
        textoNumeroOfertas.text = "Numero de ofertas: $numeroOfertas"
        textoSalarioTotal.text = "Salario total: ${formatearSalario(salarioTotal)}"
        textoSalarioMedio.text = "Salario medio: ${formatearSalario(salarioMedio.toInt())}"
        textoSalarioMaximo.text = "Salario máximo: ${formatearSalario(salarioMaximo)}"

        // Limpiamos la lista anterior (por si el usuario filtra varias veces)
        contenedorOfertas.removeAllViews()

        // Recorremos el resultado y creamos un TextView para cada oferta
        ofertasFiltradas.forEachIndexed { indice, oferta ->
            val textoOferta = TextView(this)
            textoOferta.text = "${indice + 1}. ${oferta.puesto}\n" +
                    "${oferta.empresa} - ${oferta.ciudad} - ${formatearSalario(oferta.salario)}"
            textoOferta.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
            textoOferta.setPadding(0, 0, 0, 24)
            contenedorOfertas.addView(textoOferta)
        }
        mostrarGrafico(ofertasFiltradas)
    }

    private fun formatearSalario(salario: Int): String {
        return "%d".format(salario).replace(" ", ".") + " €"
    }

    private fun mostrarGrafico(ofertasFiltradas: List<Oferta>) {
        // Contamos cuántas ofertas hay por ciudad
        val ofertasValencia = ofertasFiltradas.count { it.ciudad == "Valencia" }
        val ofertasMadrid = ofertasFiltradas.count { it.ciudad == "Madrid" }
        val ofertasAlicante = ofertasFiltradas.count { it.ciudad == "Alicante" }

        // El valor máximo para las barras (usamos maxOf con 1 para evitar división por cero)
        val valorMaximo = maxOf(ofertasValencia, ofertasMadrid, ofertasAlicante, 1)

        // Configuramos el máximo de las barras
        barraValencia.max = valorMaximo
        barraMadrid.max = valorMaximo
        barraAlicante.max = valorMaximo

        // Asignamos el progreso real a cada barra
        barraValencia.progress = ofertasValencia
        barraMadrid.progress = ofertasMadrid
        barraAlicante.progress = ofertasAlicante

        // Actualizamos los textos con el número
        textoCantidadValencia.text = ofertasValencia.toString()
        textoCantidadMadrid.text = ofertasMadrid.toString()
        textoCantidadAlicante.text = ofertasAlicante.toString()
    }

    private fun abrirSelectorParaGuardarPdf() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_TITLE, "informe_ofertas.pdf")
        startActivityForResult(intent, codigoCrearPdf)
    }

    //   ubicación
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == codigoCrearPdf && resultCode == Activity.RESULT_OK) {
            val uriPdf = data?.data
            if (uriPdf != null) {
                exportarInformePdf(uriPdf)
            }
        }
    }

    // Crear el PDF con PdfDocument
    private fun exportarInformePdf(uriPdf: Uri) {
        val documentoPdf = PdfDocument()
        val pagina = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Tamaño A4 aprox
        val paginaPdf = documentoPdf.startPage(pagina)
        val canvas = paginaPdf.canvas

        // Pintamos el fondo de blanco
        canvas.drawColor(Color.WHITE)

        // Dibujamos el título
        val pinturaTitulo = Paint()
        pinturaTitulo.color = Color.BLACK
        pinturaTitulo.textSize = 24f
        pinturaTitulo.isFakeBoldText = true
        canvas.drawText("Informe de ofertas", 32f, 48f, pinturaTitulo)

        // Reutilizamos el informe que ya está en pantalla
        val anchoDisponible = 531f
        val altoDisponible = 760f
        val anchoInforme = contenedorInforme.width
        val altoInforme = contenedorInforme.height

        if (anchoInforme > 0 && altoInforme > 0) {
            // Calculamos la escala para que quepa en el PDF
            val escala = min(anchoDisponible / anchoInforme, altoDisponible / altoInforme)

            canvas.save()
            canvas.translate(32f, 72f) // Margen
            canvas.scale(escala, escala) // Escalar
            contenedorInforme.draw(canvas) // Dibujar el XML en el PDF
            canvas.restore()
        }

        documentoPdf.finishPage(paginaPdf)

        // Guardar el archivo
        try {
            val salida = contentResolver.openOutputStream(uriPdf)
            if (salida != null) {
                salida.use {
                    documentoPdf.writeTo(salida)
                }
                Toast.makeText(this, "PDF guardado", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "No se pudo guardar el PDF", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo guardar el PDF", Toast.LENGTH_LONG).show()
        } finally {
            documentoPdf.close()
        }
    }

} // <-- AQUÍ SE CIERRA LA CLASE MAINACTIVITY