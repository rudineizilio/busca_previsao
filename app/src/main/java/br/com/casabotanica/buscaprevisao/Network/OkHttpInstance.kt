package br.com.casabotanica.buscaprevisao.Network

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object OkHttpInstance {
    val client = OkHttpClient()

    fun makeRequest(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Erro na resposta: ${response.code}")
            return response.body?.string() ?: throw IOException("Resposta vazia")
        }
    }

}