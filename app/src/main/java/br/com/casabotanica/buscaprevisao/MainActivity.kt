package br.com.casabotanica.buscaprevisao

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.casabotanica.buscaprevisao.Services.CidadeService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var tvDadoPrevisao: TextView
    private lateinit var btBuscar: Button
    private lateinit var spCidade: Spinner
    private lateinit var spEstado: Spinner

    private lateinit var listaDeEstadosCompletos: List<Estado>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDadoPrevisao = findViewById<TextView>(R.id.tvDadoPrevisao)
        btBuscar = findViewById<Button>(R.id.btBuscar)
        spEstado = findViewById<Spinner>(R.id.spEstado)
        spCidade = findViewById<Spinner>(R.id.spCidade)

        listaDeEstadosCompletos = carregarEstados(this)
        val siglasDosEstados = listaDeEstadosCompletos.map { it.sigla }

        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_spinner_item, siglasDosEstados)
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEstado.adapter = adapterEstado

        spEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val siglaEstadoSelecionado = parent?.getItemAtPosition(position).toString()
                atualizarSpinnerCidades(siglaEstadoSelecionado)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spCidade.adapter = null
            }
        }


        btBuscar.setOnClickListener {

        }
    } // Fim do onCreate

    private fun atualizarSpinnerCidades(siglaEstado: String) {
        lifecycleScope.launch {
            try {

                val cidades = CidadeService.buscarCidadesPorEstado(siglaEstado)
                if (cidades.isNotEmpty()) {
                    val adapterCidades = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, cidades)
                    adapterCidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spCidade.adapter = adapterCidades
                } else {
                    spCidade.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, listOf("Nenhuma cidade encontrada"))
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao buscar cidades: ${e.message}", Toast.LENGTH_LONG).show()
                spCidade.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, listOf("Erro ao carregar"))
            }
        }
    }


    fun carregarEstados(context: Context): List<Estado> {
        val json = context.assets.open("estados.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val tipoLista = object : TypeToken<List<Estado>>() {}.type
        return gson.fromJson(json, tipoLista)
    }
}
// cadastro gratuito : https://home.openweathermap.org/users/sign_up
    //api keys: https://home.openweathermap.org/api_keys

