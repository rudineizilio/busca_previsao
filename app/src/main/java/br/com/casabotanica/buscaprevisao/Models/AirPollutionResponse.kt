package br.com.casabotanica.buscaprevisao.Models

data class AirPollutionResponse(
    val list: List<AirQuality>
)

data class AirQuality(
    val main: MainPollution,
    val components: Components,
    val dt: Long
)

data class MainPollution(
    val aqi: Int
)

data class Components(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
)
