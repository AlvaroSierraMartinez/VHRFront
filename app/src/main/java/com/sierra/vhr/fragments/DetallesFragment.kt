package com.sierra.vhr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sierra.vhr.R
import com.sierra.vhr.databinding.FragmentDetailBinding
import com.sierra.vhr.network.ClienteRetrofit
import com.sierra.vhr.network.model.RespuestaJuego
import com.sierra.vhr.utils.SessionManager
import kotlinx.coroutines.launch

class DetallesFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var juegoActual: RespuestaJuego? = null
    private var idJuego: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // recogemos el id que nos manda la lista
        idJuego = arguments?.getInt("gameId") ?: -1

        if (idJuego == -1) {
            Toast.makeText(requireContext(), "Error al cargar el juego", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        cargarDetalle()

        binding.botonEditar.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("gameId", idJuego)
            findNavController().navigate(R.id.detalles2Aniadir, bundle)
        }

        binding.botonBorrar.setOnClickListener {
            // pedimos confirmacion antes de borrar
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar juego")
                .setMessage("¿Estas seguro de que quieres borrarlo?")
                .setPositiveButton("Fulmínalo") { _, _ ->
                    borrarJuego()
                }
                .setNegativeButton("Era bromi", null)
                .show()
        }
    }

    private fun cargarDetalle() {
        lifecycleScope.launch {
            try {
                val respuesta = ClienteRetrofit.api.recuperarColeccion(sessionManager.pedirToken())
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    juegoActual = respuesta.body()!!.find { it.id == idJuego }
                    juegoActual?.let { mostrarDatos(it) }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDatos(juego: RespuestaJuego) {
        binding.detalleTitulo.text = juego.titulo
        binding.detallePlataforma.text = juego.plataforma
        binding.detalleFormato.text = if (juego.tienda.isNullOrEmpty())
            juego.formato else "${juego.formato} — ${juego.tienda}"
        binding.detalleNota.text = "${juego.nota}/10"
        binding.detalleResenia.text = juego.resenia
    }

    private fun borrarJuego() {
        lifecycleScope.launch {
            try {
                val respuesta = ClienteRetrofit.api.borrarJuego(
                    sessionManager.pedirToken(), idJuego)
                if (respuesta.isSuccessful) {
                    Toast.makeText(requireContext(),
                        "Al cielo con el", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(),
                        "Me ha esquivado la bala", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}