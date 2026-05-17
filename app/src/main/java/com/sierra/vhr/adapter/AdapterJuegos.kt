package com.sierra.vhr.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sierra.vhr.databinding.ItemJuegoBinding
import com.sierra.vhr.network.model.RespuestaJuego

class AdapterJuegos(
    private var juegos: List<RespuestaJuego> = emptyList(),
    private val alPulsarJuego: (RespuestaJuego) -> Unit
) : RecyclerView.Adapter<AdapterJuegos.VistaJuegos>() {

    inner class VistaJuegos(private val binding: ItemJuegoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(juego: RespuestaJuego) {
            binding.titulo.text = juego.titulo
            binding.plataforma.text = juego.plataforma
            binding.formato.text = juego.formato
            binding.valorNota.text = "${juego.nota}/10"
            binding.root.setOnClickListener { alPulsarJuego(juego) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistaJuegos {
        val binding = ItemJuegoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VistaJuegos(binding)
    }

    override fun onBindViewHolder(holder: VistaJuegos, posicion: Int) {
        holder.bind(juegos[posicion])
    }

    override fun getItemCount() = juegos.size

    fun actualizarLista(nuevaLista: List<RespuestaJuego>) {
        juegos = nuevaLista
        notifyDataSetChanged()
    }
}