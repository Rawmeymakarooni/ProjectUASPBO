# 🍽️ CekTipis - Warung Makan POS System

Aplikasi Point of Sale (POS) berbasis Java Swing untuk manajemen restoran/warung dengan fitur kasir, manajemen stok, dan laporan penjualan.

![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)
![SQLite](https://img.shields.io/badge/SQLite-3.36-blue?style=flat-square&logo=sqlite)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

---

## 📋 Daftar Isi

- [Fitur](#-fitur)
- [Teknologi](#-teknologi)
- [Struktur Project](#-struktur-project)
- [Instalasi](#-instalasi)
- [Cara Menjalankan](#-cara-menjalankan)
- [Penggunaan](#-penggunaan)
- [Database](#-database)
- [Arsitektur & OOP](#-arsitektur--oop)
- [Screenshots](#-screenshots)

---

## ✨ Fitur

### 💰 Modul Kasir
- Menampilkan daftar menu dengan harga dan stok
- Menambah item ke keranjang belanja
- Edit quantity item di keranjang
- Hapus item dari keranjang
- Kalkulasi otomatis subtotal, pajak (10%), dan grand total
- Proses pembayaran dengan berbagai metode (Cash, Debit, E-Wallet)
- Generate struk pembayaran

### 📦 Modul Stok
- Melihat semua item dengan informasi stok
- Warning indicator untuk stok rendah (< 10)
- Fitur restock untuk menambah stok item
- Update stok otomatis setelah transaksi

### 📊 Modul Laporan Penjualan
- Total jumlah transaksi
- Total pendapatan
- Rata-rata nilai transaksi
- Daftar best seller items

---

## 🛠️ Teknologi

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Java 25 |
| GUI Framework | Java Swing |
| Database | SQLite 3.36 |
| JDBC Driver | sqlite-jdbc-3.36.0.3.jar |
| IDE | IntelliJ IDEA (recommended) |

---

## 📁 Struktur Project

```
CekTipis/
├── 📂 lib/
│   └── sqlite-jdbc-3.36.0.3.jar    # SQLite JDBC driver
│
├── 📂 src/
│   ├── POSRestaurant.java          # Main class + GUI (Entry point)
│   ├── DatabaseManager.java        # Singleton database handler
│   ├── MenuItem.java               # Abstract base class untuk menu
│   ├── Food.java                   # Subclass untuk makanan
│   ├── Beverage.java               # Subclass untuk minuman
│   ├── Dessert.java                # Subclass untuk dessert
│   ├── Order.java                  # Class untuk order + inner class OrderItem
│   ├── InvalidPaymentException.java
│   ├── InvalidQuantityException.java
│   └── OutOfStockException.java
│
├── 📂 out/                         # Compiled .class files
├── cektipis.db                     # SQLite database file (auto-generated)
├── CekTipis.iml                    # IntelliJ module config
└── README.md                       # Dokumentasi ini
```

---

## 💻 Instalasi

### Prerequisites
- **Java JDK 17** atau lebih baru
- **IntelliJ IDEA** (recommended) atau IDE Java lainnya

### Langkah Instalasi

1. **Clone atau download project**
   ```bash
   git clone https://github.com/username/CekTipis.git
   cd CekTipis
   ```

2. **Buka project di IntelliJ IDEA**
   - File → Open → Pilih folder CekTipis

3. **Pastikan library SQLite sudah terdaftar**
   - Library ada di folder `lib/sqlite-jdbc-3.36.0.3.jar`
   - Sudah dikonfigurasi di `CekTipis.iml`

4. **Build project**
   - Build → Build Project (Ctrl + F9)

---

## 🚀 Cara Menjalankan

### Dari IntelliJ IDEA (Recommended)
1. Buka file `src/POSRestaurant.java`
2. Klik tombol **▶️ Run** di sebelah method `main`
3. Atau tekan **Shift + F10**

### Dari Command Line
```powershell
# Navigate ke folder project
cd c:\path\to\CekTipis

# Compile semua file Java
javac -cp ".;lib/*" -d out src/*.java

# Jalankan aplikasi
java -cp "out;lib/*" POSRestaurant
```

### Pertama Kali Dijalankan
Saat pertama kali dijalankan, aplikasi akan:
1. ✅ Membuat koneksi ke SQLite
2. ✅ Membuat tabel database (menu_items, orders, order_items)
3. ✅ Mengisi data menu awal (12 items)
4. ✅ Membuat file `cektipis.db`

---

## 📖 Penggunaan

### Tab Kasir (💰 Cashier)

1. **Menambah Item ke Keranjang**
   - Pilih item dari daftar menu di sebelah kiri
   - Klik tombol "Add to Cart"

2. **Mengubah Quantity**
   - Klik pada kolom "Qty" di tabel keranjang
   - Ubah angka sesuai keinginan

3. **Menghapus Item**
   - Pilih item di keranjang
   - Klik "Remove" untuk hapus 1 item
   - Klik "Clear Cart" untuk kosongkan semua

4. **Proses Pembayaran**
   - Pilih metode pembayaran (Cash/Debit/E-Wallet)
   - Masukkan nominal pembayaran
   - Klik "💳 Process Payment"
   - Struk akan ditampilkan
   - Kembalian akan dihitung otomatis

### Tab Stok (📦 Stock)

1. **Melihat Stok**
   - Semua item ditampilkan dengan informasi stok
   - Item dengan stok < 10 ditandai ⚠️

2. **Restock Item**
   - Pilih item yang ingin di-restock
   - Klik "Restock Selected Item"
   - Masukkan jumlah yang ditambahkan

### Tab Laporan (📊 Sales)

1. **Statistik**
   - Total Orders: Jumlah transaksi
   - Total Sales: Total pendapatan
   - Avg Order: Rata-rata nilai transaksi

2. **Best Sellers**
   - Daftar 5 item terlaris beserta jumlah terjual

---

## 🗄️ Database

### Skema Database

Aplikasi menggunakan SQLite dengan 3 tabel:

#### Tabel `menu_items`
| Kolom | Tipe | Keterangan |
|-------|------|------------|
| id | INTEGER | Primary key, auto increment |
| name | TEXT | Nama menu |
| price | REAL | Harga |
| stock | INTEGER | Jumlah stok |
| category | TEXT | Food/Beverage/Dessert |
| spicy_level | INTEGER | Level pedas (0-5) untuk Food |
| is_hot | INTEGER | 0=dingin, 1=panas untuk Beverage |
| has_ice_cream | INTEGER | 0=tidak, 1=ada es krim untuk Dessert |

#### Tabel `orders`
| Kolom | Tipe | Keterangan |
|-------|------|------------|
| id | INTEGER | Primary key, auto increment |
| timestamp | TEXT | Waktu transaksi |
| status | TEXT | Pending/Completed |
| payment_method | TEXT | Cash/Debit Card/E-Wallet |
| payment_amount | REAL | Nominal pembayaran |
| total | REAL | Subtotal |
| tax | REAL | Pajak (10%) |
| grand_total | REAL | Total akhir |

#### Tabel `order_items`
| Kolom | Tipe | Keterangan |
|-------|------|------------|
| id | INTEGER | Primary key, auto increment |
| order_id | INTEGER | Foreign key ke orders |
| menu_item_id | INTEGER | Foreign key ke menu_items |
| menu_item_name | TEXT | Nama item (snapshot) |
| quantity | INTEGER | Jumlah |
| price | REAL | Harga satuan |
| subtotal | REAL | Harga × Quantity |

### Mengakses Database

Untuk melihat dan mengedit database secara langsung:
1. Download [DB Browser for SQLite](https://sqlitebrowser.org/)
2. Buka file `cektipis.db` di folder project
3. Browse tabel dan data

---

## 🏗️ Arsitektur & OOP

### Konsep OOP yang Digunakan

#### 1. Inheritance (Pewarisan)
```
MenuItem (abstract)
    ├── Food
    ├── Beverage
    └── Dessert
```

#### 2. Abstraction
- Class `MenuItem` adalah abstract class
- Method `getDescription()` adalah abstract method yang di-override di subclass

#### 3. Encapsulation
- Semua field menggunakan access modifier `private`
- Akses melalui getter dan setter
- Validasi di setter (contoh: stock tidak boleh negatif)

#### 4. Polymorphism
- Method `getDescription()` memiliki implementasi berbeda di setiap subclass:
  - Food: Menampilkan level pedas 🌶️
  - Beverage: Menampilkan panas ☕ atau dingin 🧊
  - Dessert: Menampilkan icon 🍨 atau 🍰

#### 5. Inner Class
- Class `OrderItem` didefinisikan di dalam class `Order`

#### 6. Exception Handling
- Custom exception: `InvalidPaymentException`, `InvalidQuantityException`, `OutOfStockException`

### Design Pattern

#### Singleton Pattern
- `DatabaseManager` menggunakan Singleton pattern
- Memastikan hanya ada satu instance koneksi database

```java
public static synchronized DatabaseManager getInstance() {
    if (instance == null) {
        instance = new DatabaseManager();
    }
    return instance;
}
```

---

## 📝 Menu Default

| No | Nama | Harga | Kategori | Keterangan |
|----|------|-------|----------|------------|
| 1 | Nasi Goreng | Rp 25.000 | Food | 🌶️🌶️ |
| 2 | Rendang | Rp 35.000 | Food | 🌶️🌶️🌶️ |
| 3 | Ayam Geprek | Rp 20.000 | Food | 🌶️🌶️🌶️🌶️🌶️ |
| 4 | Soto Ayam | Rp 18.000 | Food | 🌶️ |
| 5 | Mie Goreng | Rp 22.000 | Food | 🌶️🌶️ |
| 6 | Es Teh Manis | Rp 5.000 | Beverage | 🧊 Cold |
| 7 | Kopi Hitam | Rp 8.000 | Beverage | ☕ Hot |
| 8 | Jus Alpukat | Rp 15.000 | Beverage | 🧊 Cold |
| 9 | Teh Hangat | Rp 5.000 | Beverage | ☕ Hot |
| 10 | Es Krim | Rp 12.000 | Dessert | 🍨 |
| 11 | Pudding | Rp 10.000 | Dessert | 🍰 |
| 12 | Pisang Goreng | Rp 8.000 | Dessert | 🍰 |

---

## 🤝 Kontributor:
* Isma'il Faruqy 092
* Alysha Ananda Shafa 087
* Astrella Syadira Ramadhante 094
* Rizqullah Ramadhan Anandamulti 075
* Yohanes Andhika Bintang Pramadya 068


---

## 📄 Lisensi

Project ini dilisensikan di bawah MIT License.

---

<p align="center">
  Made with ❤️ using Java Swing & SQLite
</p>
