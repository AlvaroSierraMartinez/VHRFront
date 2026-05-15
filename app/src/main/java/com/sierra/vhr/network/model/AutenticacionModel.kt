package com.sierra.vhr.network.model

data class PeticionLogeo(
    val username: String,
    val password: String
)

data class RespuestaLogeo(
    val token: String
)

data class PeticionRegistro(
    val username: String,
    val mensaje: String
)

data class RespuestaRegistro(
    val username: String,
    val password: String
)