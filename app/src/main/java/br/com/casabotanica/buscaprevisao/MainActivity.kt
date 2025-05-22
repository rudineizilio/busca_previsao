package br.com.casabotanica.buscaprevisao

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.casabotanica.buscaprevisao.Network.OkHttpInstance
import br.com.casabotanica.buscaprevisao.Network.RetrofitInstance
import br.com.casabotanica.buscaprevisao.Network.VolleyInstance
import br.com.casabotanica.buscaprevisao.Services.CidadeService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var tvDadoPrevisao: TextView
    private lateinit var btBuscar: Button
    private lateinit var spCidade: Spinner
    private lateinit var spEstado: Spinner
    private lateinit var spTipoClienteHttp: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var tvDadoTotal: TextView
    private lateinit var listaDeEstadosCompletos: List<Estado>

    val apiKey = "6fd50f778bb945cd747bfb7b5ceff9b0" //Api Key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDadoPrevisao = findViewById(R.id.tvDadoPrevisao)
        btBuscar = findViewById(R.id.btBuscar)
        spEstado = findViewById(R.id.spEstado)
        spCidade = findViewById(R.id.spCidade)
        spTipoClienteHttp = findViewById(R.id.spTipoClienteHttp)
        progressBar = findViewById(R.id.progressBar)
        tvDadoTotal = findViewById(R.id.tvDadoTotal)


        // Configura spinner de bibliotecas HTTP
        val tiposHttp = listOf("Retrofit", "Volley", "OKHttp")
        val adapterTipoHttp = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposHttp)
        adapterTipoHttp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipoClienteHttp.adapter = adapterTipoHttp

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
            val cidadeSelecionada = spCidade.selectedItem?.toString()
            val tipoClienteHttp = spTipoClienteHttp.selectedItem?.toString()

            if (cidadeSelecionada.isNullOrBlank()) {
                Toast.makeText(this, "Selecione uma cidade.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (tipoClienteHttp) {
                "Retrofit" -> getWithRetrofit(cidadeSelecionada)
                "Volley" -> getWithVolley(cidadeSelecionada)
                "OKHttp" -> getWithOkHttp(cidadeSelecionada)
                else -> Toast.makeText(this, "Selecione um tipo de cliente HTTP", Toast.LENGTH_SHORT).show()
            }
            if (tipoClienteHttp != null) {
                calcularTempo(tipoClienteHttp, cidadeSelecionada)
            }
        }


    }
    private fun calcularTempo(tipoClienteHttp: String, cidadeSelecionada: String?) {
        val tempos = mutableListOf<Long>()
        when (tipoClienteHttp) {

            "Retrofit" -> repeat(10) {
                if (cidadeSelecionada != null) {
                    val tempoInicio = System.currentTimeMillis()
                    getWithRetrofit(cidadeSelecionada)
                    val tempoFim = System.currentTimeMillis()
                    tempos.add(tempoFim - tempoInicio)
                }
            }
            "Volley" -> repeat(10) {
                if (cidadeSelecionada != null) {
                    val tempoInicio = System.currentTimeMillis()
                    getWithVolley(cidadeSelecionada)
                    val tempoFim = System.currentTimeMillis()
                    tempos.add(tempoFim - tempoInicio)
                }
            }
            "OKHttp" -> repeat(10) {
                if (cidadeSelecionada != null) {
                    val tempoInicio = System.currentTimeMillis()
                    getWithOkHttp(cidadeSelecionada)
                    val tempoFim = System.currentTimeMillis()
                    tempos.add(tempoFim - tempoInicio)
                }
            }
            else -> Toast.makeText(this, "Selecione um tipo de cliente HTTP", Toast.LENGTH_SHORT).show()
        }
        val media = tempos.average()
        val desvio = sqrt(tempos.map { (it - media).pow(2) }.average())
        tvDadoTotal.text = "Média de tempo da requisição: ${media}\n Desvio: ${desvio}"
    } //Fim do calculaTempo



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

    private fun getWithRetrofit(cidade: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getWeatherByCity(cidade, apiKey, "metric", "pt_br")

                val temp = response.main.temp
                val desc = response.weather.firstOrNull()?.description ?: "Sem descrição"
                val cidadeNome = response.name

                val lat = response.coord.lat
                val lon = response.coord.lon

                val poluicaoResponse = RetrofitInstance.api.getAirPollution(lat, lon, apiKey)
                val aqi = poluicaoResponse.list.firstOrNull()?.main?.aqi ?: 0

                val textoPoluicao = when (aqi) {
                    1 -> "Qualidade do ar: Boa"
                    2 -> "Qualidade do ar: Moderada"
                    3 -> "Qualidade do ar: Ruim"
                    4 -> "Qualidade do ar: Muito Ruim"
                    5 -> "Qualidade do ar: Pior"
                    else -> "Qualidade do ar: Desconhecida"
                }

                val texto = """
                Cidade: $cidadeNome
                Temperatura: ${temp}°C
                Condição: ${desc.replaceFirstChar { it.uppercase() }}
                $textoPoluicao
            """.trimIndent()
                progressBar.visibility = View.GONE
                tvDadoPrevisao.text = texto

            } catch (e: Exception) {
                tvDadoPrevisao.text = "Erro ao buscar previsão: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getWithVolley(cidade: String) {

        val urlClima = "https://api.openweathermap.org/data/2.5/weather?q=$cidade&appid=$apiKey&units=metric&lang=pt_br"
        progressBar.visibility = View.VISIBLE

        VolleyInstance.makeRequest(this, urlClima,
            onSuccess = { response ->
                progressBar.visibility = View.GONE
                try {
                    val weatherObj = JSONObject(response)
                    val temp = weatherObj.getJSONObject("main").getDouble("temp")
                    val desc = weatherObj.getJSONArray("weather").getJSONObject(0).getString("description")
                    val cidadeNome = weatherObj.getString("name")
                    val coord = weatherObj.getJSONObject("coord")
                    val lat = coord.getDouble("lat")
                    val lon = coord.getDouble("lon")

                    val urlPoluicao = "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$apiKey"

                    VolleyInstance.makeRequest(this, urlPoluicao,
                        onSuccess = { poluicaoResponse ->
                            try {
                                val poluicaoObj = JSONObject(poluicaoResponse)
                                val aqi = poluicaoObj.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("aqi")

                                val textoPoluicao = when (aqi) {
                                    1 -> "Qualidade do ar: Boa"
                                    2 -> "Qualidade do ar: Moderada"
                                    3 -> "Qualidade do ar: Ruim"
                                    4 -> "Qualidade do ar: Muito Ruim"
                                    5 -> "Qualidade do ar: Pior"
                                    else -> "Qualidade do ar: Desconhecida"
                                }

                                val texto = """
                                Cidade: $cidadeNome
                                Temperatura: ${temp}°C
                                Condição: ${desc.replaceFirstChar { it.uppercase() }}
                                $textoPoluicao
                            """.trimIndent()

                                tvDadoPrevisao.text = texto

                            } catch (e: Exception) {
                                tvDadoPrevisao.text = getString(R.string.erro_processar_ar) + " ${e.message}"
                            }
                        },
                        onError = { error ->
                            tvDadoPrevisao.text = getString(R.string.erro_req_poluicao) + " ${error.message}"
                        }
                    )

                } catch (e: Exception) {
                    tvDadoPrevisao.text = getString(R.string.erro_processar_clima) + " ${e.message}"
                }
            },
            onError = { error ->
                tvDadoPrevisao.text = getString(R.string.erro_req_clima) + " ${error.message}"
            }
        )
    }

    private fun getWithOkHttp(cidade: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            lifecycleScope.launch {
                try {

                    val weatherJson = withContext(Dispatchers.IO) {
                        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cidade&appid=$apiKey&units=metric&lang=pt_br"
                        OkHttpInstance.makeRequest(url)
                    }

                    val weatherObj = JSONObject(weatherJson)
                    val temp = weatherObj.getJSONObject("main").getDouble("temp")
                    val desc = weatherObj.getJSONArray("weather")
                        .getJSONObject(0).getString("description")
                    val cidadeNome = weatherObj.getString("name")

                    val coord = weatherObj.getJSONObject("coord")
                    val lat = coord.getDouble("lat")
                    val lon = coord.getDouble("lon")
                    val pollutionJson = withContext(Dispatchers.IO) {
                        val url = "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$apiKey"
                        OkHttpInstance.makeRequest(url)
                    }
                    val pollutionObj = JSONObject(pollutionJson)
                    val aqi = pollutionObj.getJSONArray("list").getJSONObject(0)
                        .getJSONObject("main").getInt("aqi")
                    val textoPoluicao = when (aqi) {
                        1 -> "Qualidade do ar: Boa"
                        2 -> "Qualidade do ar: Moderada"
                        3 -> "Qualidade do ar: Ruim"
                        4 -> "Qualidade do ar: Muito Ruim"
                        5 -> "Qualidade do ar: Pior"
                        else -> "Qualidade do ar: Desconhecida"
                    }
                    val texto = """
                    Cidade: $cidadeNome
                    Temperatura: ${temp}°C
                    Condição: ${desc.replaceFirstChar { it.uppercase() }}
                    $textoPoluicao
                """.trimIndent()
                    progressBar.visibility = View.GONE
                    tvDadoPrevisao.text = texto

                } catch (e: Exception) {
                    tvDadoPrevisao.text = "Erro ao buscar previsão: ${e.message}"
                    e.printStackTrace()
                }
            }
        }
    }//Fim da fun getWithOkHttp
} //Fim da classe



