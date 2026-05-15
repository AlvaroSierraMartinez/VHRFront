package com.sierra.vhr.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val config: SharedPreferences =
        context.getSharedPreferences("preferencias", Context.MODE_PRIVATE)

    // guarda el token cuando el usuario hace login
    fun guardarToken(token: String) {
        config.edit().putString("token", token).apply()
    }

    // devuelve el token con el Bearer delante, listo para usar en las peticiones
    fun pedirToken(): String {
        return "Bearer ${config.getString("token", "")}"
    }

    // comprueba si hay sesion activa
    fun haySesion(): Boolean {
        return config.getString("token", null) != null
    }

    // borra el token al cerrar sesion
    fun cerrarSesion() {
        config.edit().remove("token").apply()
    }
}