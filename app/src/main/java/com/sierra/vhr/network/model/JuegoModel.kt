package com.sierra.vhr.network.model

data class PeticionJuego(
    val titulo: String,
    val plataforma: String,
    val formato: String,
    val tienda: String,
    val nota: Int,
    val resenia: String
)

data class RespuestaJuego(
    val id: Int,
    val titulo: String,
    val plataforma: String,
    val formato: String,
    val tienda: String,
    val nota: Int,
    val resenia: String
)