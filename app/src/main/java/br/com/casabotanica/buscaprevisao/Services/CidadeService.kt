package br.com.casabotanica.buscaprevisao.Services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient // Importação correta para OkHttpClient
import okhttp3.Request     // Importação correta para Request
import org.json.JSONArray  // Importação para JSONArray (para parsing do JSON)
import java.io.IOException   // É uma boa prática capturar IOException especificamente para OkHttp

/*Quando usar OkHttp?

Quando você precisa de mais controle sobre requisições HTTP.

Quando quer um cliente leve, eficiente e com bons recursos de cache e autenticação.

*/

object CidadeService {
    // 1. Instância do OkHttpClient:
    // É uma boa prática criar uma única instância de OkHttpClient e reutilizá-la
    // para todas as chamadas HTTP, pois ele gerencia seu próprio pool de conexões
    // e cache (se configurado).
    private val client = OkHttpClient()

    suspend fun buscarCidadesPorEstado(uf: String): List<String> {
        val url = "https://servicodados.ibge.gov.br/api/v1/localidades/estados/$uf/municipios"
        // 2. Executando na Thread de IO:
        // 'withContext(Dispatchers.IO)' é essencial aqui.
        // OkHttp.execute() é uma chamada bloqueante (síncrona).
        // Executá-la em Dispatchers.IO move a operação para uma thread de background,
        // evitando que a thread principal (UI) seja bloqueada, o que causaria ANRs.
        return withContext(Dispatchers.IO) {
            // 3. Criando a Requisição:
            // Simples e direto para uma requisição GET.
            val request = Request.Builder()
                .url(url)// Define a URL do endpoint
                .build() // Constrói o objeto Request

            // 4. Executando a Chamada:
            // client.newCall(request).execute() envia a requisição e aguarda a resposta.
            // O '.use' garante que o 'response.body' seja fechado automaticamente,
            // mesmo que ocorram exceções. Isso é muito importante para liberar recursos.
            client.newCall(request).execute().use { response ->
                // 5. Verificando o Sucesso da Resposta:
                // response.isSuccessful cobre códigos de status HTTP na faixa 200-299.
                if (!response.isSuccessful) {
                    // É útil incluir o código de status e, possivelmente, a mensagem do corpo do erro
                    // para facilitar a depuração.
                    val errorBody = response.body?.string() // Tenta ler o corpo do erro, se houver
                    throw IOException("Erro na requisição: ${response.code} ${response.message}. Body: $errorBody")
                }

                // 6. Obtendo e Verificando o Corpo da Resposta:
                // response.body?.string() lê  o corpo da resposta como uma String.
                // Cuidado: Para respostas muito grandes, isso pode consumir muita memória.
                // Para casos assim, processar o corpo como um stream (response.body?.source()) seria melhor.
                // A verificação '?: throw Exception(...)' é boa para garantir que o corpo não seja nulo.
                val responseBodyString = response.body?.string()
                    ?: throw IOException("Corpo de resposta vazio ou não pôde ser lido.")

                // 7. Parsing do JSON:
                // Você está usando org.json.JSONArray, que é uma biblioteca embutida no Android.
                // Para projetos mais complexos ou com JSON mais estruturado, bibliotecas como
                // Kotlinx Serialization, Gson ou Moshi são recomendadas por serem mais robustas e type-safe.
                val cidadesJson = JSONArray(responseBodyString)

                val listaCidades = mutableListOf<String>()
                for (i in 0 until cidadesJson.length()) {
                    val cidadeObject = cidadesJson.getJSONObject(i)
                    // Assumindo que cada objeto no array JSON tem uma chave "nome"
                    listaCidades.add(cidadeObject.getString("nome"))
                }
                listaCidades // Retorna a lista de nomes das cidades
            }
        }
    }
}