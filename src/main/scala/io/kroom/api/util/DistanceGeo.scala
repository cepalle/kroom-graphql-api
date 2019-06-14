package io.kroom.api.util

object DistanceGeo {
  def distanceGeo(lat1: Double, long1: Double, lat2: Double, long2: Double): Double = {

    val lat1r = math.toRadians(lat1)
    val lat2r = math.toRadians(lat2)
    val long1r = math.toRadians(long1)
    val long2r = math.toRadians(long2)

    //distance (A, B) = R * arccos (sin (lata) sin * (LATB) + cos (lata) cos * (LATB) cos * (Lona-lonB))
    math.acos(
      math.sin(lat1r) * math.sin(lat2r) + math.cos(lat1r) * math.cos(lat2r) * math.cos(long1r - long2r)
    ) * 6371.0
  }
}
