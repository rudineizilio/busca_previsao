package br.com.casabotanica.buscaprevisao.Models

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val coord: Coord,
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Weather(
    val description: String
)

data class Coord(
    val lon: Double,
    val lat: Double
)
