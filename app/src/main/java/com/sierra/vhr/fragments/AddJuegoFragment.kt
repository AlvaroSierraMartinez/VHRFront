package com.sierra.vhr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sierra.vhr.databinding.FragmentAddGameBinding
import androidx.lifecycle.lifecycleScope
import com.sierra.vhr.network.ClienteRetrofit
import com.sierra.vhr.network.model.PeticionJuego
import com.sierra.vhr.utils.SessionManager
import kotlinx.coroutines.launch



class AddJuegoFragment : Fragment() {
    private var idJuego: Int = -1
    private var modoEdicion = false
    private var _binding: FragmentAddGameBinding? = null
    private lateinit var sessionManager: SessionManager
    private val binding get() = _binding!!

    private val plataformas = listOf(
        // PlayStation
        "PlayStation 1", "PlayStation 2", "PlayStation 3", "PlayStation 4", "PlayStation 5",
        "PSP", "PS Vita",
        // Xbox
        "Xbox", "Xbox 360", "Xbox One", "Xbox Series X/S",
        // Nintendo
        "NES", "SNES", "Nintendo 64", "GameCube", "Wii", "Wii U", "Switch",
        "Game Boy", "Game Boy Advance", "Nintendo DS", "Nintendo 3DS",
        // SEGA
        "Mega Drive", "Saturn", "Dreamcast", "Game Gear",
        // PC
        "PC"
    )

    private val tiendasPC = listOf(
        "Steam", "Epic Games", "GOG", "EA App", "Ubisoft Connect",
        "Battle.net", "Xbox Game Pass (PC)", "PlayStation PC", "Itch.io", "Otra"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        idJuego = arguments?.getInt("gameId") ?: -1
        if (idJuego != -1) {
            modoEdicion = true
            editarJuego()
        }

        // adaptador para las plataformas
        val adaptadorPlataforma = ArrayAdapter(requireContext(),android.R.layout.simple_dropdown_item_1line,plataformas
        )
        binding.plataforma.setAdapter(adaptadorPlataforma)

        // adaptador para las tiendas
        val adaptadorTienda = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            tiendasPC
        )
        binding.tienda.setAdapter(adaptadorTienda)

        // esconde adaptador de tienda si no es digital y de pc
        fun mostrarTiendaSiAcaso() {
            val isPC = binding.plataforma.text.toString() == "PC"
            val isDigital = binding.checkDigital.isChecked
            binding.despleTienda.visibility = if (isPC && isDigital) View.VISIBLE else View.GONE
            if (!isPC || !isDigital) binding.tienda.text.clear()
        }
        //escuchamos el cambio de plataforma
        binding.plataforma.setOnItemClickListener { _, _, _, _ ->
            mostrarTiendaSiAcaso()
        }
        //escuchamos el check de fisico/digital
        binding.rgFormat.setOnCheckedChangeListener { _, _ ->
            mostrarTiendaSiAcaso()
        }

        // Slider de la nota
        binding.nota.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, nota: Int, fromUser: Boolean) {
                binding.valorNota.text = "$nota/10"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        //boton de guardar
        binding.btnSave.setOnClickListener {
            val titulo = binding.editTextTitulo.text.toString().trim()
            val plataforma = binding.plataforma.text.toString().trim()
            val formato = if (binding.formato.isChecked) "Físico" else "Digital"
            val tienda = binding.tienda.text.toString().trim()
            val nota = binding.nota.progress
            val resenia = binding.resenia.text.toString().trim()

            if (titulo.isEmpty()) {
                binding.editTextTitulo.error = "El título es obligatorio"
                return@setOnClickListener
            }
            if (plataforma.isEmpty()) {
                Toast.makeText(requireContext(), "¿Y en qué lo tienes?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.despleTienda.visibility == View.VISIBLE && tienda.isEmpty()) {
                Toast.makeText(requireContext(), "¿De qué tienda es?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val juego = PeticionJuego(
                titulo = titulo,
                plataforma = plataforma,
                formato = formato,
                tienda = tienda,
                nota = nota,
                resenia = resenia
            )

            lifecycleScope.launch {
                try {
                    val respuesta = if (modoEdicion) {
                        ClienteRetrofit.api.editarJuego(sessionManager.pedirToken(), idJuego, juego)
                    } else {
                        ClienteRetrofit.api.añadirJuego(sessionManager.pedirToken(), juego)
                    }

                    if (respuesta.isSuccessful) {
                        val mensaje = if (modoEdicion) "Juego actualizado" else "Juego guardado en la colección"
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
    private fun editarJuego() {
        lifecycleScope.launch {
            try {
                val respuesta = ClienteRetrofit.api.recuperarColeccion(sessionManager.pedirToken())
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val juego = respuesta.body()!!.find { it.id == idJuego }
                    juego?.let {
                        // rellenamos el formulario con los datos existentes
                        binding.editTextTitulo.setText(it.titulo)
                        binding.plataforma.setText(it.plataforma, false)
                        binding.nota.progress = it.nota
                        binding.valorNota.text = "${it.nota}/10"
                        binding.resenia.setText(it.resenia)

                        // formato
                        if (it.formato == "Digital") {
                            binding.checkDigital.isChecked = true
                        } else {
                            binding.formato.isChecked = true
                        }

                        // tienda si la hay (porque no creo que nadie vaya a comprar un juego
                        // de la store de microchoff en una ps5 (por ahora)
                        if (!it.tienda.isNullOrEmpty()) {
                            binding.tienda.setText(it.tienda, false)
                            binding.despleTienda.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "Error al cargar el juego", Toast.LENGTH_SHORT).show()
            }
        }
    }
        //reseteamos el binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}