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

        // 1. Validasi Konten
        if (!content) {
            return res.status(400).json({ error: "Teks jurnal tidak boleh kosong" });
        }

        // 2. PROTEKSI PREMIUM (Menggunakan data bypass dari admin) // ini tambahan dari cornel 
        // const user = users.find(u => u.id === userId);
        // if (!user || !user.isPremium) {
        //     return res.status(403).json({ 
        //         error: "Fitur Terkunci", 
        //         message: "Analisis Jurnal AI adalah fitur Premium. Silakan hubungi admin atau lakukan pembayaran." 
        //     });
        // }

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


// =====================================================================
// ENDPOINTS: CRUD EVENT UNTUK CAREGIVER APP
// =====================================================================

// 1. GET ALL EVENTS (Read)
// Digunakan oleh aplikasi Caregiver untuk menampilkan semua daftar event
app.get('/api/events', (req, res) => {
    try {
        // Mengurutkan dari event terbaru yang dibuat
        const sortedEvents = [...events].sort((a, b) => b.createdAt - a.createdAt);
        res.status(200).json(sortedEvents);
    } catch (error) {
        console.error("Gagal mengambil data event:", error);
        res.status(500).json({ error: "Gagal mengambil data event." });
    }
});

// 2. GET EVENT BY ID (Read Single)
// Berguna saat Caregiver membuka halaman detail event
app.get('/api/events/:id', (req, res) => {
    try {
        const event = events.find(e => e.id === req.params.id);
        if (!event) {
            return res.status(404).json({ error: "Event tidak ditemukan." });
        }
        res.status(200).json(event);
    } catch (error) {
        res.status(500).json({ error: "Gagal mengambil detail event." });
    }
});

// 3. POST NEW EVENT (Create)
// Digunakan admin / sistem untuk menambahkan event baru kesehatan/komunitas
app.post('/api/events', (req, res) => {
    try {
        const { title, description, date, time, location, category, slots } = req.body;

        // Validasi input minimal
        if (!title || !date || !location || !category) {
            return res.status(400).json({ error: "Field title, date, location, dan category wajib diisi." });
        }

        const newEvent = {
            id: crypto.randomUUID(),
            title,
            description: description || "",
            date,
            time: time || "00:00",
            location,
            category,
            slots: slots ? parseInt(slots) : 0,
            createdAt: Date.now()
        };

        events.push(newEvent);
        res.status(201).json({ message: "Event berhasil ditambahkan.", data: newEvent });
    } catch (error) {
        console.error("Gagal menambahkan event:", error);
        res.status(500).json({ error: "Gagal menambahkan event baru." });
    }
});

// 4. PUT UPDATE EVENT (Update)
// Digunakan untuk mengubah detail event jika ada perubahan jadwal/lokasi
app.put('/api/events/:id', (req, res) => {
    try {
        const { id } = req.params;
        const { title, description, date, time, location, category, slots } = req.body;

        const eventIndex = events.findIndex(e => e.id === id);
        if (eventIndex === -1) {
            return res.status(404).json({ error: "Event tidak ditemukan." });
        }

        // Update data yang dikirimkan, jika tidak dikirim gunakan data lama
        events[eventIndex] = {
            ...events[eventIndex],
            title: title || events[eventIndex].title,
            description: description !== undefined ? description : events[eventIndex].description,
            date: date || events[eventIndex].date,
            time: time || events[eventIndex].time,
            location: location || events[eventIndex].location,
            category: category || events[eventIndex].category,
            slots: slots !== undefined ? parseInt(slots) : events[eventIndex].slots
        };

        res.status(200).json({ message: "Event berhasil diperbarui.", data: events[eventIndex] });
    } catch (error) {
        console.error("Gagal memperbarui event:", error);
        res.status(500).json({ error: "Gagal memperbarui event." });
    }
});

// 5. DELETE EVENT (Delete)
// Digunakan jika event dibatalkan
app.delete('/api/events/:id', (req, res) => {
    try {
        const { id } = req.params;
        const eventIndex = events.findIndex(e => e.id === id);

        if (eventIndex === -1) {
            return res.status(404).json({ error: "Event tidak ditemukan." });
        }

        // Hapus dari array
        events.splice(eventIndex, 1);
        res.status(200).json({ message: "Event berhasil dihapus." });
    } catch (error) {
        console.error("Gagal menghapus event:", error);
        res.status(500).json({ error: "Gagal menghapus event." });
    }
});


// =====================================================================
// ENDPOINTS: CRUD CAREGIVER & FAMILY
// =====================================================================

// 1. CREATE Caregiver / Family
app.post('/api/users', (req, res) => {
    try {
        const { name, role, email, managedLansiaId, monitoredLansiaId } = req.body;

        if (!name || !role || !email) {
            return res.status(400).json({ error: "Nama, role, dan email wajib diisi." });
        }

        if (role !== "Caregiver" && role !== "Family") {
            return res.status(400).json({ error: "Endpoint ini khusus untuk role Caregiver atau Family." });
        }

        const newUser = {
            id: crypto.randomUUID(),
            name,
            role,
            email,
            isPremium: false, // Default awal pendaftaran adalah False
            managedLansiaId: managedLansiaId || null,
            monitoredLansiaId: monitoredLansiaId || null,
            createdAt: Date.now()
        };

        users.push(newUser);
        res.status(201).json({ message: `User dengan role ${role} berhasil terdaftar.`, data: newUser });
    } catch (error) {
        res.status(500).json({ error: "Gagal membuat data user baru." });
    }
});

// 2. READ (Get Profile Berdasarkan ID)
app.get('/api/users/:id', (req, res) => {
    try {
        const user = users.find(u => u.id === req.params.id);
        if (!user) {
            return res.status(404).json({ error: "User tidak ditemukan." });
        }
        res.status(200).json(user);
    } catch (error) {
        res.status(500).json({ error: "Gagal mengambil data user." });
    }
});

// 3. UPDATE Data Caregiver / Family
app.put('/api/users/:id', (req, res) => {
    try {
        const { id } = req.params;
        const { name, email, managedLansiaId, monitoredLansiaId } = req.body;

        const userIndex = users.findIndex(u => u.id === id);
        if (userIndex === -1) {
            return res.status(404).json({ error: "User tidak ditemukan." });
        }

        users[userIndex] = {
            ...users[userIndex],
            name: name || users[userIndex].name,
            email: email || users[userIndex].email,
            managedLansiaId: managedLansiaId !== undefined ? managedLansiaId : users[userIndex].managedLansiaId,
            monitoredLansiaId: monitoredLansiaId !== undefined ? monitoredLansiaId : users[userIndex].monitoredLansiaId
        };

        res.status(200).json({ message: "Profil berhasil diperbarui.", data: users[userIndex] });
    } catch (error) {
        res.status(500).json({ error: "Gagal memperbarui data user." });
    }
});

// 4. DELETE Data Caregiver / Family
app.delete('/api/users/:id', (req, res) => {
    try {
        const { id } = req.params;
        const userIndex = users.findIndex(u => u.id === id);

        if (userIndex === -1) {
            return res.status(404).json({ error: "User tidak ditemukan." });
        }

        users.splice(userIndex, 1);
        res.status(200).json({ message: "User berhasil dihapus dari sistem." });
    } catch (error) {
        res.status(500).json({ error: "Gagal menghapus user." });
    }
});


// =====================================================================
// ENDPOINTS: DASHBOARD MONITORING ADMIN
// =====================================================================

// 5. MONITORING SELURUH USER (Melihat semua Lansia, Caregiver, & Family)
app.get('/api/admin/monitoring/users', (req, res) => {
    try {
        // Mendapatkan statistik singkat beserta list data mentah untuk tabel admin
        const totalUsers = users.length;
        const totalPremium = users.filter(u => u.isPremium).length;
        const breakdownRole = {
            Lansia: users.filter(u => u.role === "Lansia").length,
            Caregiver: users.filter(u => u.role === "Caregiver").length,
            Family: users.filter(u => u.role === "Family").length,
            Admin: users.filter(u => u.role === "Admin").length,
        };

        res.status(200).json({
            summary: {
                totalUsers,
                totalPremium,
                breakdownRole
            },
            allUsers: users
        });
    } catch (error) {
        res.status(500).json({ error: "Gagal memuat data monitoring admin." });
    }
});


// =====================================================================
// ENDPOINTS: BYPASS PREMIUM STATUS BY ADMIN
// =====================================================================

// 6. BYPASS STATUS PREMIUM MANUAL
// Route ini menerima ID User dan status isPremium baru (true/false) dari dashboard admin
app.patch('/api/admin/bypass-premium/:userId', (req, res) => {
    try {
        const { userId } = req.params;
        const { isPremium } = req.body; // Isikan boolean true atau false

        if (isPremium === undefined) {
            return res.status(400).json({ error: "Field 'isPremium' (true/false) wajib dikirim di body." });
        }

        const userIndex = users.findIndex(u => u.id === userId);
        if (userIndex === -1) {
            return res.status(404).json({ error: "User tidak ditemukan." });
        }

        // Proses bypass status premium oleh admin
        users[userIndex].isPremium = isPremium;

        res.status(200).json({
            message: `Status Premium untuk user ${users[userIndex].name} berhasil diubah menjadi ${isPremium}.`,
            data: users[userIndex]
        });
    } catch (error) {
        res.status(500).json({ error: "Gagal mengubah status premium user." });
    }
});