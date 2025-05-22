package br.com.casabotanica.buscaprevisao.Network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

object VolleyInstance {
    private var requestQueue: RequestQueue? = null

    fun init(context: Context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.applicationContext)
        }
    }

    fun makeRequest(context: Context, url: String, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response -> onSuccess(response) },
            { error -> onError(error) }
        )

        // Garante que a requestQueue esteja inicializada
        if (requestQueue == null) init(context)

        requestQueue?.add(stringRequest)
    }
}