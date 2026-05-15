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
import com.sierra.vhr.databinding.FragmentLoginBinding
import com.sierra.vhr.network.ClienteRetrofit
import com.sierra.vhr.network.model.PeticionLogeo
import com.sierra.vhr.network.model.PeticionRegistro
import com.sierra.vhr.utils.SessionManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // si ya hay sesion activa saltamos el login directamente
        if (sessionManager.haySesion()) {
            findNavController().navigate(R.id.login2Lista)
            return
        }

        binding.btnLogin.setOnClickListener {
            val usuario = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (usuario.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Rellena los dos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // llamada a la API en una corrutina
            lifecycleScope.launch {
                try {
                    val respuesta = ClienteRetrofit.api.logear(PeticionLogeo(usuario, pass))
                    if (respuesta.isSuccessful && respuesta.body() != null) {
                        sessionManager.guardarToken(respuesta.body()!!.token)
                        findNavController().navigate(R.id.login2Lista)
                    } else {
                        Toast.makeText(requireContext(),
                            "Problemas de identidad?", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            val usuario = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (usuario.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Rellena los campos, que no son tantos...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = ClienteRetrofit.api.registrar(PeticionRegistro(usuario, pass))
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(),
                            "Yay! bienvenido a la familia", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(),
                            "Ya existes en la base de datos y tu sin saberlo!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}