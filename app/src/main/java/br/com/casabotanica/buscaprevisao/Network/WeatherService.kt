package br.com.casabotanica.buscaprevisao.Network

import br.com.casabotanica.buscaprevisao.Models.AirPollutionResponse
import br.com.casabotanica.buscaprevisao.Models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pt_br"
    ): WeatherResponse

    @GET("air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirPollutionResponse
}
