package com.sierra.vhr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sierra.vhr.R
import com.sierra.vhr.adapter.AdapterJuegos
import com.sierra.vhr.databinding.FragmentGameListBinding
import com.sierra.vhr.network.ClienteRetrofit
import com.sierra.vhr.network.model.RespuestaJuego
import com.sierra.vhr.utils.SessionManager
import kotlinx.coroutines.launch


class ListaJuegosFragment : Fragment() {

    private var _binding: FragmentGameListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adaptador: AdapterJuegos
    private lateinit var gestorSesion: SessionManager
    private var coleccionCompleta: List<RespuestaJuego> = emptyList()

    // filtros activos
    private var filtroPlataforma: String? = null
    private var filtroFormato: String? = null
    private var filtroNotaMinima: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,

    ): View {
        _binding = FragmentGameListBinding.inflate(inflater, container, false)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gestorSesion = SessionManager(requireContext())
        adaptador = AdapterJuegos(emptyList()) { juego ->
            val bundle = Bundle()
            bundle.putInt("gameId", juego.id)
            findNavController().navigate(R.id.lista2detalle, bundle)
        }

        binding.recyclerJuegos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerJuegos.adapter = adaptador

        binding.btnAddGame.setOnClickListener {
            val opciones = arrayOf("Añadir juego", "Filtros", "Cerrar sesión")
            AlertDialog.Builder(requireContext())
                .setItems(opciones) { _, indice ->
                    when (indice) {
                        0 -> findNavController().navigate(R.id.Lista2Aniadir)
                        1 -> mostrarDialogoFiltros()
                        2 -> {
                            gestorSesion.cerrarSesion()
                            findNavController().navigate(R.id.lista2login)
                        }
                    }
                }
                .show()
        }

        // busqueda en tiempo real
        binding.vistaBusqueda.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(texto: String?): Boolean {
                aplicarFiltros(texto ?: "")
                return true
            }
        })

        cargarColeccion()
    }

    private fun cargarColeccion() {
        lifecycleScope.launch {
            try {
                val respuesta = ClienteRetrofit.api.recuperarColeccion(gestorSesion.pedirToken())
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    coleccionCompleta = respuesta.body()!!
                    aplicarFiltros(binding.vistaBusqueda.query.toString())
                } else {
                    Toast.makeText(requireContext(),
                        "Error al cargar la colección", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun aplicarFiltros(textoBusqueda: String) {
        var resultado = coleccionCompleta
        //buscar por titulo con la barra
        if (textoBusqueda.isNotEmpty()) {
            val textoLower = textoBusqueda.lowercase()
            resultado = resultado.filter { juego ->
                juego.titulo.lowercase().contains(textoLower) ||
                        juego.plataforma.lowercase().contains(textoLower) ||
                        juego.formato.lowercase().contains(textoLower) ||
                        (juego.tienda?.lowercase()?.contains(textoLower) == true)
            }
        }
        //filtro plataforma
        filtroPlataforma?.let { plataforma ->
            resultado = resultado.filter { it.plataforma.lowercase() == plataforma.lowercase() }
        }
        //filtro formato
        filtroFormato?.let { formato ->
            resultado = resultado.filter { it.formato.lowercase() == formato.lowercase() }
        }
        //filtro nota
        filtroNotaMinima?.let { nota ->
            resultado = resultado.filter { it.nota >= nota }
        }

        adaptador.actualizarLista(resultado)

    }

    private fun mostrarDialogoFiltros() {
        // listar plataformas de la coleccion
        val plataformas = listOf("Todas") +
                coleccionCompleta.map { it.plataforma }.distinct().sorted()
        val formatos = listOf("Todos", "Físico", "Digital")
        val notas = listOf("Cualquiera", "5 o más", "7 o más", "9 o más")

        // los indices a 0 (todos) por defecto
        var plataformaSeleccionada = plataformas.indexOf(filtroPlataforma ?: "Todas")
            .takeIf { it >= 0 } ?: 0
        var formatoSeleccionado = formatos.indexOf(filtroFormato ?: "Todos")
            .takeIf { it >= 0 } ?: 0
        var notaSeleccionada = when (filtroNotaMinima) {
            5 -> 1; 7 -> 2; 9 -> 3; else -> 0
        }



        // dialogo manual con opciones
        val opciones = arrayOf(
            "Plataforma: ${plataformas[plataformaSeleccionada]}",
            "Formato: ${formatos[formatoSeleccionado]}",
            "Nota mínima: ${notas[notaSeleccionada]}"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar colección")
            .setItems(opciones) { _, opcion ->
                when (opcion) {
                    0 -> mostrarSelectorPlataforma(plataformas)
                    1 -> mostrarSelectorFormato(formatos)
                    2 -> mostrarSelectorNota(notas)
                }
            }
            .setNeutralButton("Quitar filtros") { _, _ ->
                filtroPlataforma = null
                filtroFormato = null
                filtroNotaMinima = null
                aplicarFiltros(binding.vistaBusqueda.query.toString())
                Toast.makeText(requireContext(), "Filtros eliminados", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun mostrarSelectorPlataforma(plataformas: List<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona plataforma")
            .setItems(plataformas.toTypedArray()) { _, indice ->
                filtroPlataforma = if (indice == 0) null else plataformas[indice]
                aplicarFiltros(binding.vistaBusqueda.query.toString())
            }
            .show()
    }

    private fun mostrarSelectorFormato(formatos: List<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona formato")
            .setItems(formatos.toTypedArray()) { _, indice ->
                filtroFormato = if (indice == 0) null else formatos[indice]
                aplicarFiltros(binding.vistaBusqueda.query.toString())
            }
            .show()
    }

    private fun mostrarSelectorNota(notas: List<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Nota mínima")
            .setItems(notas.toTypedArray()) { _, indice ->
                filtroNotaMinima = when (indice) {
                    1 -> 5; 2 -> 7; 3 -> 9; else -> null
                }
                aplicarFiltros(binding.vistaBusqueda.query.toString())
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}