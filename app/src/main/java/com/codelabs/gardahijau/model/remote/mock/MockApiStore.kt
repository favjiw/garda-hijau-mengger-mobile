package com.codelabs.gardahijau.model.remote.mock

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Data contoh untuk MODE DEMO. Disimpan di memori: data yang ditambah lewat aplikasi
 * (catatan sampah, siklus, fase) ikut muncul sampai aplikasi ditutup.
 *
 * Bentuk JSON mengikuti class di model/remote/response. Kalau API asli berubah,
 * sesuaikan juga di sini.
 */
object MockApiStore {

    private val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private const val USER_ID = 1
    private var username = "pengelola.rw5"
    private var rw = "RW5"

    private var nextId = 100
    private var nextSiklusId = 1 // tampil sebagai "Siklus 1", "Siklus 2", ...
    private val pencacahan = mutableListOf<JSONObject>()
    private val siklus = mutableListOf<JSONObject>()
    private val fase = mutableMapOf<Int, MutableList<JSONObject>>()
    private val prediksi = mutableMapOf<Int, MutableList<JSONObject>>()
    private val notifikasi = mutableListOf<JSONObject>()

    init {
        seed()
    }

    // ------------------------------------------------------------------ routing

    @Synchronized
    fun handle(method: String, path: String, body: String): Pair<Int, String> {
        val input = runCatching { JSONObject(body) }.getOrDefault(JSONObject())
        val p = path.trimEnd('/')
        val siklusFase = Regex("^/api/siklus/(\\d+)/fase$").find(p)
        val prediksiSiklus = Regex("^/api/prediksi/siklus/(\\d+)$").find(p)
        val notifRead = Regex("^/api/notifikasi/(\\d+)/read$").find(p)

        return try {
            when {
                method == "POST" && p == "/api/auth/login" -> login(input)
                method == "POST" && p == "/api/auth/register" -> register(input)
                method == "POST" && p == "/api/auth/logout" -> ok(JSONObject().put("message", "Berhasil keluar"))

                method == "GET" && p == "/api/pencacahan" -> ok(
                    JSONObject().put("status", "success").put("data", JSONArray(pencacahan.sortedByDescending { it.getString("tanggalWaktu") }))
                )
                method == "POST" && p == "/api/pencacahan" -> addPencacahan(input)

                method == "GET" && p == "/api/siklus" -> ok(
                    JSONObject().put("success", true).put("data", JSONArray(siklus.map { withPrediksi(it) }))
                )
                method == "POST" && p == "/api/siklus" -> addSiklus(input)
                method == "GET" && siklusFase != null -> {
                    val id = siklusFase.groupValues[1].toInt()
                    ok(JSONObject().put("success", true).put("data", JSONArray(fase[id].orEmpty())))
                }
                method == "POST" && siklusFase != null -> addFase(siklusFase.groupValues[1].toInt(), input)

                method == "GET" && prediksiSiklus != null -> {
                    val id = prediksiSiklus.groupValues[1].toInt()
                    ok(JSONObject().put("success", true).put("data", JSONArray(prediksi[id].orEmpty())))
                }
                method == "GET" && p == "/api/prediksi/total" -> prediksiTotal()

                method == "GET" && p == "/api/notifikasi" -> ok(
                    JSONObject().put("success", true).put("data", JSONArray(notifikasi))
                )
                method == "PATCH" && notifRead != null -> {
                    val id = notifRead.groupValues[1].toInt()
                    val item = notifikasi.find { it.getInt("id") == id }
                    item?.put("isRead", true)
                    ok(JSONObject().put("success", true).put("updated", if (item != null) 1 else 0))
                }

                // Chatbot (base URL berbeda, path sama-sama diawali /api)
                method == "POST" && p == "/api/query" -> chatbot(input)

                else -> error(404, "Endpoint ini belum ada di mode demo: $method $p")
            }
        } catch (e: Exception) {
            error(500, "Mode demo gagal memproses: ${e.message}")
        }
    }

    // ------------------------------------------------------------------ handlers

    private fun login(input: JSONObject): Pair<Int, String> {
        val name = input.optString("username").trim()
        val password = input.optString("password")
        if (name.isEmpty() || password.isEmpty()) return error(400, "Nama pengguna dan kata sandi wajib diisi")
        username = name
        val user = JSONObject().put("id", USER_ID).put("username", username).put("rw", rw)
        return ok(JSONObject().put("message", "Login berhasil").put("user", user).put("token", "token-demo"))
    }

    private fun register(input: JSONObject): Pair<Int, String> {
        username = input.optString("username", username)
        rw = input.optString("rw", rw).ifEmpty { rw }
        val user = JSONObject()
            .put("id", USER_ID).put("username", username).put("rw", rw)
            .put("password", "********").put("createdAt", iso.format(Date()))
        return ok(JSONObject().put("message", "Registrasi berhasil").put("user", user))
    }

    private fun addPencacahan(input: JSONObject): Pair<Int, String> {
        val item = JSONObject()
            .put("id", nextId++)
            .put("userId", USER_ID)
            .put("tanggalWaktu", input.optString("tanggalWaktu").ifEmpty { iso.format(Date()) })
            .put("totalSampah", input.optDouble("totalSampah", 0.0))
            .put("catatan", input.optString("catatan"))
            .put("createdAt", iso.format(Date()))
        pencacahan.add(item)
        return ok(JSONObject().put("message", "Catatan sampah tersimpan").put("data", item))
    }

    private fun addSiklus(input: JSONObject): Pair<Int, String> {
        val id = nextSiklusId++
        val tanggal = input.optString("tanggalMulai").ifEmpty { iso.format(Date()) }
        val item = JSONObject()
            .put("id", id)
            .put("userId", USER_ID)
            .put("tanggalMulai", tanggal)
            .put("jumlahTelur", input.optInt("jumlahTelur", 0))
            .put("mediaTelur", input.optString("mediaTelur"))
            .put("catatan", input.optString("catatan"))
            .put("createdAt", iso.format(Date()))
        siklus.add(item)
        // Server membuat fase PENETASAN otomatis (aplikasi tidak punya form untuk fase ini).
        fase[id] = mutableListOf(
            faseJson(id, "PENETASAN", tanggal, "Telur dipindahkan ke wadah penetasan", jumlahTelur = item.getInt("jumlahTelur"))
        )
        return ok(JSONObject().put("success", true).put("data", item))
    }

    private fun addFase(siklusId: Int, input: JSONObject): Pair<Int, String> {
        val parent = siklus.find { it.getInt("id") == siklusId } ?: return error(404, "Siklus tidak ditemukan")
        val jenis = input.optString("jenis").uppercase()
        val tanggal = input.optString("tanggal").ifEmpty { iso.format(Date()) }
        val keterangan = input.optString("catatan")
        val list = fase.getOrPut(siklusId) { mutableListOf() }
        list.removeAll { it.getString("jenis") == jenis }

        val item = when (jenis) {
            "PEMBESARAN" -> faseJson(siklusId, jenis, tanggal, keterangan, jumlahMakanan = input.optInt("jumlahMakanan", 0))
            "PANEN" -> {
                val pakan = list.find { it.getString("jenis") == "PEMBESARAN" }?.optInt("jumlahMakanan", 0) ?: 0
                val hasilKg = perkiraanKg(parent.optInt("jumlahTelur", 0), pakan)
                val pred = prediksiJson(siklusId, hasilKg)
                prediksi[siklusId] = mutableListOf(pred)
                faseJson(siklusId, jenis, tanggal, keterangan, prediksiPanen = pred)
            }
            else -> faseJson(siklusId, jenis, tanggal, keterangan, jumlahTelur = input.optInt("jumlahTelur", 0))
        }
        list.add(item)
        return ok(JSONObject().put("success", true).put("data", item))
    }

    private fun prediksiTotal(): Pair<Int, String> {
        val all = prediksi.values.flatten()
        val totalGram = all.sumOf { it.getInt("hasilGram") }
        return ok(
            JSONObject()
                .put("success", true)
                .put("totalGram", totalGram)
                .put("totalKg", totalGram / 1000.0)
                .put("jumlahPrediksi", all.size)
        )
    }

    private fun chatbot(input: JSONObject): Pair<Int, String> {
        val question = input.optString("query", input.optString("question", input.optString("message"))).trim()
        val q = question.lowercase()
        val answer = when {
            "menetas" in q || "telur" in q ->
                "Telur maggot BSF biasanya menetas dalam 3 sampai 4 hari. Jaga media tetap lembap dan jauhkan dari sinar matahari langsung."
            "pakan" in q || "makan" in q ->
                "Pakan terbaik adalah sampah organik lunak seperti sisa sayur, buah, dan ampas tahu. Cacah dulu supaya larva lebih mudah makan."
            "dilarang" in q || "tidak boleh" in q || "jangan" in q ->
                "Hindari plastik, kaca, logam, minyak berlebih, dan bahan kimia. Daging dan tulang sebaiknya sedikit saja karena cepat berbau."
            "panen" in q ->
                "Larva siap dipanen sekitar hari ke-18 sampai 24, saat warnanya mulai gelap. Pisahkan dari media dengan ayakan."
            else ->
                "Ini jawaban contoh dari mode demo. Saat server menyala, GardaBot akan menjawab pertanyaan seputar maggot dan pemilahan sampah."
        }
        return ok(
            JSONObject()
                .put("answer", answer)
                .put("question", question)
                .put("sources", JSONArray())
                .put("tokens_used", 0)
                .put("model", "mode-demo")
                .put("num_sources", 0)
        )
    }

    // ------------------------------------------------------------------ data contoh

    private fun seed() {
        // Catatan sampah 10 hari terakhir (satu hari kosong supaya grafik tidak terlalu rata).
        val kg = listOf(230.0, 140.0, 150.0, 120.0, 190.0, 260.0, 150.0, 0.0, 175.0, 160.0)
        val notes = listOf(
            "Banyak sisa sayur dari pasar", "", "Sebagian basah, dijemur dulu", "",
            "Ada kiriman dari RW 4", "Sisa hajatan warga", "", "", "", "Cacahan pagi"
        )
        kg.forEachIndexed { daysAgo, value ->
            if (value <= 0) return@forEachIndexed
            pencacahan.add(
                JSONObject()
                    .put("id", nextId++)
                    .put("userId", USER_ID)
                    .put("tanggalWaktu", isoAt(daysAgo, hour = 8, minute = 15 + daysAgo * 3))
                    .put("totalSampah", value)
                    .put("catatan", notes[daysAgo])
                    .put("createdAt", isoAt(daysAgo, hour = 9, minute = 0))
            )
        }

        // Siklus maggot: satu sudah ada perkiraan panen besar, satu sedang membesar, satu baru menetas.
        seedSiklus(mulaiHariLalu = 22, telur = 40, media = "Dedak", pakanKg = 50, besarHariLalu = 18)
        seedSiklus(mulaiHariLalu = 12, telur = 40, media = "Dedak", pakanKg = 40, besarHariLalu = 8)
        seedSiklus(mulaiHariLalu = 5, telur = 35, media = "Kotoran Ternak", pakanKg = null, besarHariLalu = null)

        notifikasi.addAll(
            listOf(
                notifJson(1, "Siklus siap dipanen", "Larva siklus pertama sudah berumur 22 hari. Catat hasil panen setelah ditimbang.", "panen", hoursAgo = 3, read = false),
                notifJson(2, "Belum ada catatan sampah hari ini", "Catat sampah yang masuk hari ini supaya grafik tetap lengkap.", "pencacahan", hoursAgo = 5, read = false),
                notifJson(3, "Perkiraan panen sudah keluar", "Perkiraan hasil panen siklus kedua sekitar 10 kg.", "prediksi", hoursAgo = 26, read = true),
                notifJson(4, "Sampah Sabtu paling banyak", "260 kg masuk. Siapkan pencacahan lebih awal di akhir pekan.", "pencacahan", hoursAgo = 30, read = true),
            )
        )
    }

    private fun seedSiklus(mulaiHariLalu: Int, telur: Int, media: String, pakanKg: Int?, besarHariLalu: Int?) {
        val id = nextSiklusId++
        val mulai = isoAt(mulaiHariLalu, hour = 7, minute = 30)
        siklus.add(
            JSONObject()
                .put("id", id)
                .put("userId", USER_ID)
                .put("tanggalMulai", mulai)
                .put("jumlahTelur", telur)
                .put("mediaTelur", media)
                .put("catatan", "")
                .put("createdAt", mulai)
        )
        val list = mutableListOf(faseJson(id, "PENETASAN", mulai, "Telur dipindahkan ke wadah penetasan", jumlahTelur = telur))
        if (pakanKg != null && besarHariLalu != null) {
            val tanggalBesar = isoAt(besarHariLalu, hour = 8, minute = 0)
            list.add(faseJson(id, "PEMBESARAN", tanggalBesar, "Pakan sisa sayur dari pasar", jumlahMakanan = pakanKg))
            val pred = prediksiJson(id, perkiraanKg(telur, pakanKg))
            prediksi[id] = mutableListOf(pred)
            list.add(faseJson(id, "PANEN", tanggalBesar, "Fase panen otomatis", prediksiPanen = pred))
        }
        fase[id] = list
    }

    // ------------------------------------------------------------------ pembantu

    /** Rumus contoh, BUKAN model ML: cukup untuk menghasilkan angka yang masuk akal. */
    private fun perkiraanKg(telurGram: Int, pakanKg: Int): Double =
        ((pakanKg * 0.2 + telurGram * 0.05) * 10).roundToInt() / 10.0

    private fun withPrediksi(item: JSONObject): JSONObject {
        val copy = JSONObject(item.toString())
        copy.put("prediksiPanen", prediksi[item.getInt("id")]?.firstOrNull() ?: JSONObject.NULL)
        return copy
    }

    private fun faseJson(
        siklusId: Int,
        jenis: String,
        tanggal: String,
        keterangan: String,
        jumlahTelur: Int? = null,
        jumlahMakanan: Int = 0,
        prediksiPanen: JSONObject? = null,
    ): JSONObject = JSONObject()
        .put("id", nextId++)
        .put("siklusId", siklusId)
        .put("jenis", jenis)
        .put("tanggal", tanggal)
        .put("keterangan", keterangan)
        .put("jumlahMakanan", jumlahMakanan)
        .put("jumlahTelur", jumlahTelur ?: JSONObject.NULL)
        .put("prediksiPanen", prediksiPanen ?: JSONObject.NULL)
        .put("createdAt", tanggal)

    private fun prediksiJson(siklusId: Int, hasilKg: Double): JSONObject = JSONObject()
        .put("id", nextId++)
        .put("faseId", siklusId)
        .put("hasilKg", hasilKg)
        .put("hasilGram", (hasilKg * 1000).roundToInt())
        .put("estimatedValue", (hasilKg * 6000).roundToInt())
        .put("feedCost", 0)
        .put("conversionRate", 0.2)
        .put("conversionLabel", "Baik")
        .put("roiEstimate", JSONObject.NULL)
        .put("createdAt", iso.format(Date()))

    private fun notifJson(id: Int, title: String, message: String, type: String, hoursAgo: Int, read: Boolean) =
        JSONObject()
            .put("id", id)
            .put("userId", USER_ID)
            .put("title", title)
            .put("message", message)
            .put("type", type)
            .put("createdAt", iso.format(Date(System.currentTimeMillis() - hoursAgo * 3_600_000L)))
            .put("isRead", read)

    /** Tanggal [daysAgo] hari lalu pada jam lokal tertentu, ditulis dalam format UTC seperti API. */
    private fun isoAt(daysAgo: Int, hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return iso.format(cal.time)
    }

    private fun ok(json: JSONObject): Pair<Int, String> = 200 to json.toString()

    private fun error(code: Int, message: String): Pair<Int, String> =
        code to JSONObject().put("success", false).put("message", message).toString()
}
