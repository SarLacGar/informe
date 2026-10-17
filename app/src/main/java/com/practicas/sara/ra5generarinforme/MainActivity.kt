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

class MainActivity : AppCompatActivity() {

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
    }

    private fun formatearSalario(salario: Int): String {
        return "%d".format(salario).replace(" ", ".") + " €"
    }

} // <-- AQUÍ SE CIERRA LA CLASE MAINACTIVITY