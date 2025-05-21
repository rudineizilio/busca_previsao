package br.com.casabotanica.buscaprevisao.Services

import br.com.casabotanica.buscaprevisao.Network.OkHttpInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray

/*Quando usar OkHttp?

Quando você precisa de mais controle sobre requisições HTTP.

Quando quer um cliente leve, eficiente e com bons recursos de cache e autenticação.

*/

object CidadeService {

    suspend fun buscarCidadesPorEstado(uf: String): List<String> {
        val url = "https://servicodados.ibge.gov.br/api/v1/localidades/estados/$uf/municipios"

        val cidades = withContext(Dispatchers.IO) { // OkHttp.execute() é uma chamada bloqueante (síncrona).Executá-la em Dispatchers.IO move a operação para uma thread de background,
            OkHttpInstance.makeRequest(url)
        }
        val cidadesJson = JSONArray(cidades)

        val listaCidades = mutableListOf<String>()
        for (i in 0 until cidadesJson.length()) {
            val cidadeObject = cidadesJson.getJSONObject(i)

            listaCidades.add(cidadeObject.getString("nome"))
        }
        return listaCidades // Retorna a lista de nomes das cidades

    } //Fim da fun
} //Fim do object