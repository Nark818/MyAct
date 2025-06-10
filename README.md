# MyAct - Aplikasi Manajemen Tugas

## Ikhtisar

MyAct adalah aplikasi manajemen tugas Android modern yang dirancang untuk membantu pengguna melacak dan mengatur tugas harian mereka. Dengan antarmuka yang intuitif, kategori yang dapat disesuaikan, dan kemampuan penyaringan yang kuat, MyAct membuat pengorganisasian tugas menjadi sederhana dan efisien.

## Fitur

### Manajemen Tugas
- Membuat, mengedit, dan menghapus tugas
- Mengatur prioritas tugas (Rendah, Menengah, Tinggi)
- Menetapkan kategori dengan warna kustom
- Mengatur tenggat waktu untuk tugas yang sensitif terhadap waktu
- Menandai tugas sebagai selesai

### Pengorganisasian
- Filter tugas berdasarkan status (Aktif/Selesai)
- Filter tugas berdasarkan prioritas
- Filter tugas berdasarkan kategori
- Filter tugas berdasarkan tenggat waktu (Hari ini, Minggu ini, Terlambat)
- Fungsi pencarian untuk pencarian tugas yang cepat

### Statistik dan Wawasan
- Gambaran visual tentang distribusi tugas
- Persentase penyelesaian tugas
- Jumlah tugas berdasarkan tingkat prioritas
- Jumlah tugas berdasarkan kategori
- Perbandingan tugas aktif dan selesai

### Pengalaman Pengguna
- Komponen UI Material Design 3
- Tema yang dapat disesuaikan (Mode Terang/Gelap)
- Geser-untuk-menyegarkan untuk pembaruan daftar tugas
- Navigasi bawah modern
- Animasi perayaan setelah menyelesaikan tugas

### Manajemen Data
- Database SQLite untuk penyimpanan lokal
- Fungsi cadangan dan pemulihan
- Kategori default untuk memulai dengan cepat

## Arsitektur

MyAct mengikuti praktik pengembangan Android modern:

### Struktur Proyek
- **activities**: Berisi semua kelas aktivitas
- **adapters**: Berisi adapter RecyclerView
- **api.models**: Kelas model data
- **database**: Helper database dan kelas kontrak
- **fragments**: Fragment UI
- **utils**: Kelas utilitas

### Skema Database

#### Tabel Tugas
| Kolom          | Tipe    | Deskripsi                                |
|----------------|---------|------------------------------------------|
| id             | INTEGER | Kunci utama                              |
| title          | TEXT    | Judul tugas                              |
| description    | TEXT    | Deskripsi tugas                          |
| due_date       | TEXT    | Tenggat waktu dalam format YYYY-MM-DD    |
| priority       | INTEGER | Tingkat prioritas (1=Rendah, 2=Menengah, 3=Tinggi) |
| category_id    | INTEGER | Kunci asing ke tabel kategori            |
| is_completed   | INTEGER | Status penyelesaian tugas (0/1)          |

#### Tabel Kategori
| Kolom   | Tipe    | Deskripsi                    |
|---------|---------|------------------------------|
| id      | INTEGER | Kunci utama                  |
| name    | TEXT    | Nama kategori                |
| color   | TEXT    | Kode warna hex untuk kategori|

## Komponen Utama

### Aktivitas
- **MainActivity**: Antarmuka aplikasi utama dengan navigasi bawah
- **SplashActivity**: Layar pemuatan awal dengan kutipan motivasi
- **TaskDetailActivity**: Membuat/mengedit detail tugas
- **RewardActivity**: Layar perayaan yang ditampilkan saat menyelesaikan tugas

### Fragment
- **TaskListFragment**: Menampilkan daftar tugas dengan opsi pemfilteran
- **StatisticsFragment**: Menampilkan statistik tugas dan wawasan
- **SettingsFragment**: Preferensi pengguna dan pengaturan aplikasi

### Utilitas
- **ThemeManager**: Menangani pengalihan tema aplikasi
- **NetworkUtils**: Memeriksa konektivitas jaringan
- **BackupRestoreHelper**: Operasi cadangan dan pemulihan database
- **TaskFilterSettings**: Mengelola preferensi pemfilteran tugas

## Memulai

### Prasyarat
- Android Studio Arctic Fox (2020.3.1) atau lebih baru
- SDK Minimum: Android 5.0 (API level 21)
- SDK Target: Android 12 (API level 31)

### Instruksi Pengaturan
1. Klon repositori atau unduh kode sumber
2. Buka proyek di Android Studio
3. Sinkronkan file Gradle
4. Jalankan aplikasi pada emulator atau perangkat fisik

### Konfigurasi
- Aplikasi menggunakan tema Material Components
- Kategori default didefinisikan dalam TaskContract.DEFAULT_CATEGORIES
- Preferensi aplikasi disimpan menggunakan SharedPreferences

## Praktik Terbaik yang Diterapkan

- Pola Singleton untuk akses database
- Pola Executor untuk operasi latar belakang
- Manajemen transaksi database yang tepat
- Pemisahan tugas dengan kelas kontrak
- Desain UI yang responsif
- Adapter RecyclerView yang efisien
- Kepatuhan terhadap pedoman Material Design
- Pemrosesan latar belakang pada thread terpisah
- Pengorganisasian sumber daya yang tepat

## Pengembangan Masa Depan
- Sinkronisasi cloud
- Berbagi tugas
- Notifikasi kustom
- Integrasi kalender
- Widget untuk layar beranda
- Subtugas dan daftar periksa
- Tugas berulang
- Pelacakan waktu

## Pemecahan Masalah

### Masalah Umum
- **Kesalahan Database**: Coba hapus data aplikasi atau instal ulang
- **Kategori Hilang**: Periksa inisialisasi database di TaskDbHelper
- **Glitch UI**: Pastikan tema Material Components diterapkan dengan benar
- **Kehilangan Data**: Gunakan fungsi cadangan secara teratur

### Tips Debugging
- Aktifkan opsi pengembang untuk pencatatan yang detail
- Periksa Logcat untuk kesalahan operasi database
- Verifikasi atribut tema di styles.xml
- Uji pada berbagai ukuran perangkat untuk desain responsif
