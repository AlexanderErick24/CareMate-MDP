package com.mdp.caremate.ui.premium

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mdp.caremate.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PaywallFragment : Fragment() {

    private lateinit var webViewMidtrans: WebView
    private lateinit var btnBuyPremium: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_paywall, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webViewMidtrans = view.findViewById(R.id.webViewMidtrans)
        btnBuyPremium = view.findViewById(R.id.btnBuyPremium)

        // Setup WebView for Midtrans Snap
        webViewMidtrans.settings.javaScriptEnabled = true
        webViewMidtrans.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                // Jika url mengarah kembali ke app atau callback sukses, kita bisa tutup webview
                // Untuk simulasi, anggap semua url eksternal selain snap adalah callback sukses
                if (url.contains("example.com") || url.contains("callback")) {
                    Toast.makeText(requireContext(), "Pembayaran Berhasil! Mengaktifkan Premium...", Toast.LENGTH_LONG).show()
                    webViewMidtrans.visibility = View.GONE
                    
                    // Update status isPremium = true per-user via PremiumUtils
                    com.mdp.caremate.utils.PremiumUtils.setPremium(requireContext(), true)
                    
                    // Simpan juga di Firebase agar admin bisa melihat revenue
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(uid)
                            .update("isPremium", true)
                    }
                    
                    activity?.onBackPressedDispatcher?.onBackPressed()
                    return true
                }
                return false
            }
        }

        btnBuyPremium.setOnClickListener {
            // Simulasi panggil backend create-transaction
            createTransaction()
        }

        // Tombol bypass untuk simulasi langsung tanpa Midtrans
        val btnBypass = view.findViewById<Button>(R.id.btnBypassPremium)
        btnBypass.setOnClickListener {
            // Simpan di lokal (SharedPreferences per-user)
            com.mdp.caremate.utils.PremiumUtils.setPremium(requireContext(), true)

            // Simpan juga di Firebase agar admin bisa melihat revenue
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .update("isPremium", true)
            }

            Toast.makeText(requireContext(), "✅ Premium Aktif! (Simulasi Bypass)", Toast.LENGTH_LONG).show()
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun createTransaction() {
        btnBuyPremium.isEnabled = false
        btnBuyPremium.text = "Memproses..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:3000/api/payment/create-transaction")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonParam = JSONObject()
                jsonParam.put("userId", "12345") // Ambil dari session yang aktif
                jsonParam.put("grossAmount", 50000)

                val os = OutputStreamWriter(conn.outputStream)
                os.write(jsonParam.toString())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(responseStr)
                    val redirectUrl = jsonResponse.getString("redirect_url")

                    withContext(Dispatchers.Main) {
                        webViewMidtrans.visibility = View.VISIBLE
                        webViewMidtrans.loadUrl(redirectUrl)
                        btnBuyPremium.visibility = View.GONE
                        view?.findViewById<Button>(R.id.btnBypassPremium)?.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Gagal memproses pembayaran.", Toast.LENGTH_SHORT).show()
                        btnBuyPremium.text = "Berlangganan Rp 50.000 / Bulan"
                        btnBuyPremium.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error jaringan: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnBuyPremium.text = "Berlangganan Rp 50.000 / Bulan"
                    btnBuyPremium.isEnabled = true
                }
            }
        }
    }
}
