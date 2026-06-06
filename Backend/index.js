require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { GoogleGenerativeAI } = require('@google/generative-ai');
const crypto = require('crypto'); // Bawaan Node.js untuk membuat ID Unik

const app = express();
app.use(cors());
app.use(express.json()); // Wajib ada agar server bisa membaca JSON dari Retrofit Android

// Inisialisasi Gemini AI
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

// =====================================================================
// ENDPOINT: POST /premium/analyze-mood
// Menerima teks dari Android, memproses ke Gemini, dan membalas JSON
// =====================================================================
app.post('/premium/analyze-mood', async (req, res) => {
    try {
        const { content } = req.body;

        if (!content) {
            return res.status(400).json({ error: "Teks jurnal tidak boleh kosong" });
        }

        // Kita gunakan model Flash karena merespons sangat cepat dan ringan
        const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

        // PROMPT ENGINEERING
        // Di sini kita menaruh "roh" dari aplikasi Teman Keluarga.
        const prompt = `
        Kamu adalah asisten psikologi yang hangat, sabar, dan empatik di dalam aplikasi "Teman Keluarga". 
        Aplikasi ini didesain khusus untuk membantu lansia.
        
        Tugasmu adalah menganalisis teks curhatan/jurnal berikut dan memberikan:
        1. Skor mood (1-100, di mana 1 sangat stres/sedih, 100 sangat bahagia/damai).
        2. Analisis singkat dan kalimat penyemangat (maksimal 3 kalimat) dengan sapaan yang sopan (seperti Bapak/Ibu/Opa/Oma).

        Teks Jurnal: "${content}"

        Balas HANYA dengan format JSON persis seperti ini, tanpa penjelasan tambahan dan tanpa markdown:
        {
            "moodScore": angka,
            "aiAnalysis": "teks kalimat penyemangat"
        }
        `;

        // Mengirim prompt ke Gemini dan menunggu balasan
        const result = await model.generateContent(prompt);
        const aiResponseText = result.response.text();
        
        // TEKNIK EKSTRAKSI DATA
        // Pada bagian ini, kita menerapkan teknik pembersihan untuk memaksa Gemini merespons dalam struktur JSON murni. 
        // Ini adalah logika yang sama dengan saat mengotomatisasi ekstraksi dokumen mentah menjadi data terstruktur yang siap dibaca oleh sistem.
        const cleanJsonString = aiResponseText.replace(/```json/g, '').replace(/```/g, '').trim();
        const aiData = JSON.parse(cleanJsonString);

        // Merakit balasan persis seperti format "JournalJson" yang ditunggu oleh Retrofit Android
        const finalResponse = {
            id: crypto.randomUUID(), // Buatkan ID unik secara otomatis
            content: content,
            moodScore: aiData.moodScore,
            aiAnalysis: aiData.aiAnalysis,
            timestamp: Date.now() // Berikan cap waktu server
        };

        // Kirimkan ke Android!
        res.status(200).json(finalResponse);

    } catch (error) {
        console.error("Terjadi kesalahan sistem AI:", error);
        res.status(500).json({ error: "Gagal menganalisis jurnal, periksa koneksi atau API Key." });
    }
});

// Jalankan Server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server Backend Caremate sudah berjalan di http://localhost:${PORT}`);
});