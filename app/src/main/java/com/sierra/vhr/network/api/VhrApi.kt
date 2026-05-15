package com.sierra.vhr.network.api

import com.sierra.vhr.network.model.PeticionJuego
import com.sierra.vhr.network.model.RespuestaJuego
import com.sierra.vhr.network.model.PeticionLogeo
import com.sierra.vhr.network.model.RespuestaLogeo
import com.sierra.vhr.network.model.PeticionRegistro
import com.sierra.vhr.network.model.RespuestaRegistro
import retrofit2.Response
import retrofit2.http.*

interface VhrApi {

    // logeo
    @POST("api/auth/login")
    suspend fun logear(@Body request: PeticionLogeo): Response<RespuestaLogeo>
    //registro
    @POST("api/auth/register")
    suspend fun registrar(@Body request: PeticionRegistro): Response<RespuestaRegistro>


    // juegos
    @GET("api/games")
    suspend fun recuperarColeccion(
        @Header("Authorization") token: String
    ): Response<List<RespuestaJuego>>


    //añadir
    @POST("api/games")
    suspend fun añadirJuego(
        @Header("Authorization") token: String,
        @Body juego: PeticionJuego
    ): Response<RespuestaJuego>


    //editar
    @PUT("api/games/{id}")
    suspend fun editarJuego(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body juego: PeticionJuego
    ): Response<RespuestaJuego>


    //borrar
    @DELETE("api/games/{id}")
    suspend fun borrarJuego(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Void>

}