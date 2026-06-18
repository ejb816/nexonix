package domains.world

/** WGS84 geodetic ⇄ Earth-Centered-Earth-Fixed (Geocentric/Axial) conversions — the
  * closed-form projection the Geocentric/Axial input/output adapters compute through.
  * Plain math utility, no JSON twin (it is the adapter's mechanism, not a message
  * type). Heliocentric/Ecliptic + the time-dependent translation are deferred until
  * the first crossing to Ethereal. */
object Geodesy {
  private val a   = 6378137.0               // WGS84 semi-major axis (m)
  private val f   = 1.0 / 298.257223563     // flattening
  private val e2  = f * (2.0 - f)           // first eccentricity squared
  private val b   = a * (1.0 - f)           // semi-minor axis
  private val ep2 = e2 / (1.0 - e2)         // second eccentricity squared

  /** geodetic (latitude°, longitude°, height m) -> ECEF (x, y, z) m. */
  def geodeticToEcef(latitudeDeg: Double, longitudeDeg: Double, heightMetres: Double): (Double, Double, Double) = {
    val phi = latitudeDeg.toRadians
    val lam = longitudeDeg.toRadians
    val sinPhi = math.sin(phi)
    val cosPhi = math.cos(phi)
    val n = a / math.sqrt(1.0 - e2 * sinPhi * sinPhi)
    val x = (n + heightMetres) * cosPhi * math.cos(lam)
    val y = (n + heightMetres) * cosPhi * math.sin(lam)
    val z = (n * (1.0 - e2) + heightMetres) * sinPhi
    (x, y, z)
  }

  /** ECEF (x, y, z) m -> geodetic (latitude°, longitude°, height m), Bowring's closed form. */
  def ecefToGeodetic(x: Double, y: Double, z: Double): (Double, Double, Double) = {
    val lam = math.atan2(y, x)
    val p = math.sqrt(x * x + y * y)
    val theta = math.atan2(z * a, p * b)
    val sinT = math.sin(theta)
    val cosT = math.cos(theta)
    val phi = math.atan2(z + ep2 * b * sinT * sinT * sinT, p - e2 * a * cosT * cosT * cosT)
    val sinPhi = math.sin(phi)
    val n = a / math.sqrt(1.0 - e2 * sinPhi * sinPhi)
    val h = p / math.cos(phi) - n
    (phi.toDegrees, lam.toDegrees, h)
  }
}
