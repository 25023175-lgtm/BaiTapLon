# 🏆 Hệ thống Đấu giá UET — AuctionSystem_UET

> Bài tập lớn môn **Lập trình nâng cao** — Học kỳ II, 2025–2026  
> Trường Đại học Công nghệ — ĐHQGHN (UET)

---

## 1. Mô tả bài toán

Hệ thống Đấu giá Trực tuyến cho phép người dùng đăng ký tài khoản theo vai trò **Bidder** (người mua) hoặc **Seller** (người bán), sau đó tham gia vào các phiên đấu giá theo thời gian thực.

**Phạm vi hệ thống:**

- Người bán đăng sản phẩm với giá khởi điểm và thời gian kết thúc, nạp tiền vào tài khoản
- Người mua đặt giá cạnh tranh, hệ thống tự động kiểm tra tính hợp lệ
- Khi phiên kết thúc, hệ thống tự động xác định người thắng cuộc
- Giao diện cập nhật realtime khi có thay đổi từ bất kỳ client nào
- Admin quản trị toàn bộ hệ thống qua tài khoản đặc biệt

---

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Giao diện (GUI) | JavaFX 21 + FXML + SceneBuilder |
| Theme UI | AtlantaFX |
| Cơ sở dữ liệu | MySQL (qua XAMPP) |
| Kết nối DB | JDBC |
| Lập trình mạng | Java Socket (TCP) |
| Build tool | Apache Maven |
| Kiểm thử | JUnit 5 |
| CI/CD | GitHub Actions |
| Coding convention | Checkstyle (Google Style) |

---

## 3. Yêu cầu cài đặt

- **Java 21** (JDK 21 trở lên)
- **Maven 3.8+**
- **XAMPP** (MySQL + Apache) hoặc MySQL Server độc lập
- **IntelliJ IDEA** (khuyên dùng) hoặc Eclipse

---

## 4. Cấu trúc thư mục

```
BaiTapLon/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/auction/
│   │   │   │   ├── common/              # Custom Exceptions
│   │   │   │   │   ├── AuctionClosedException.java
│   │   │   │   │   ├── InvalidBidException.java
│   │   │   │   │   └── AuthenticationException.java
│   │   │   │   ├── factory/             # Factory Pattern
│   │   │   │   │   ├── UserFactory.java
│   │   │   │   │   └── ItemFactory.java
│   │   │   │   ├── model/               # OOP Model Classes
│   │   │   │   │   ├── User.java              (abstract)
│   │   │   │   │   ├── Bidder.java
│   │   │   │   │   ├── Seller.java
│   │   │   │   │   ├── Admin.java
│   │   │   │   │   ├── Item.java              (abstract)
│   │   │   │   │   ├── Product.java
│   │   │   │   │   ├── Electronics.java
│   │   │   │   │   ├── Art.java
│   │   │   │   │   ├── Vehicle.java
│   │   │   │   │   └── Bid.java
│   │   │   │   └── observer/            # Observer Pattern
│   │   │   │       ├── AuctionObserver.java
│   │   │   │       ├── AuctionSubject.java
│   │   │   │       └── AuctionManager.java
│   │   │   ├── com/uet/auction/         # Controllers & Server
│   │   │   │   ├── AuctionServer.java
│   │   │   │   ├── ClientHandler.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── BidController.java
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── DataManager.java
│   │   │   │   ├── DatabaseConnection.java
│   │   │   │   ├── SessionManager.java
│   │   │   │   └── SceneManager.java
│   │   │   └── module-info.java
│   │   └── resources/com/uet/auction/
│   │       ├── dashboard-view.fxml
│   │       ├── login-view.fxml
│   │       ├── register-view.fxml
│   │       ├── bid-view.fxml
│   │       └── styles.css
│   └── test/                            # Unit Tests (JUnit 5)
├── target/
│   ├── server.jar                       # File JAR máy chủ
│   └── client.jar                       # File JAR giao diện người dùng
├── .github/workflows/ci.yml             # CI/CD GitHub Actions
├── pom.xml
└── README.md
```

---

## 5. Vị trí các file .jar

Sau khi build bằng lệnh `mvn clean package -DskipTests`, hai file JAR sẽ được tạo trong thư mục `target/`:

| File | Đường dẫn | Mô tả |
|---|---|---|
| `server.jar` | `target/server.jar` | Chạy máy chủ đấu giá (AuctionServer) |
| `client.jar` | `target/client.jar` | Chạy giao diện người dùng (JavaFX) |

---

## 6. Thiết lập Database

Mở **phpMyAdmin** (hoặc MySQL Workbench) và chạy lần lượt các câu SQL sau:

```sql
-- 1. Tạo database
CREATE DATABASE IF NOT EXISTS auction_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE auction_db;

-- 2. Bảng người dùng
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'Bidder',
    balance     DOUBLE       NOT NULL DEFAULT 0.0
);

-- 3. Bảng sản phẩm
CREATE TABLE IF NOT EXISTS products (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    start_price     DOUBLE  NOT NULL,
    current_price   DOUBLE  NOT NULL,
    buy_now_price   DOUBLE  DEFAULT 0,
    seller_username VARCHAR(50),
    start_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time        DATETIME NOT NULL,
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    bid_count       INT DEFAULT 0
);

-- 4. Bảng lịch sử đặt giá
CREATE TABLE IF NOT EXISTS bids (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    product_id  INT    NOT NULL,
    bid_price   DOUBLE NOT NULL,
    bid_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 5. Tạo tài khoản Admin mặc định
INSERT INTO users (username, password, email, full_name, role, balance)
VALUES ('admin', '123456', 'admin@uet.vn', 'Admin UET', 'Admin', 0);
```

---

## 7. Cấu hình kết nối Database

Mở file `src/main/java/com/uet/auction/DatabaseConnection.java` và chỉnh thông tin nếu cần:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/auction_db";
private static final String USER     = "root";   // username MySQL của bạn
private static final String PASSWORD = "";        // password MySQL (XAMPP mặc định để trống)
```

---

## 8. Hướng dẫn chạy Server/Client

### Bước 1 — Build dự án

```bash
mvn clean package -DskipTests
```

Sau khi build xong, kiểm tra thư mục `target/` có 2 file: `server.jar` và `client.jar`.

---

### Bước 2 — Chạy Server (bắt buộc chạy TRƯỚC)

Mở một terminal, di chuyển vào thư mục project và chạy:

```bash
cd ~/IdeaProjects/AuctionSystem_UET
java -jar target/server.jar
```

Chờ đến khi thấy thông báo:

```
[SERVER] Máy chủ đang mở cửa tại Cổng 8888.
[SERVER] Đang lắng nghe và chờ Client kết nối tới...
```

> ⚠️ Giữ nguyên terminal này, không đóng trong suốt quá trình chạy.

---

### Bước 3 — Chạy Client (mở nhiều cửa sổ để test realtime)

Mở terminal **mới**, di chuyển vào thư mục project và chạy:

```bash
cd ~/IdeaProjects/AuctionSystem_UET
java -jar target/client.jar
```

Để giả lập nhiều người dùng cùng lúc, mở thêm terminal khác và chạy lại lệnh trên:

```bash
# Terminal 2 — người dùng thứ 2
java -jar target/client.jar

# Terminal 3 — người dùng thứ 3
java -jar target/client.jar
```

> ✅ Mỗi lần chạy `client.jar` sẽ mở một cửa sổ giao diện độc lập.

---

### Tài khoản mặc định

| Vai trò | Username | Password |
|---|---|---|
| Admin | `admin` | `123456` |
| Bidder / Seller | Tự đăng ký qua giao diện | — |

---

## 9. Danh sách chức năng đã hoàn thành

### Chức năng bắt buộc

- [x] Đăng ký / Đăng nhập theo vai trò (Bidder / Seller / Admin)
- [x] Phân quyền: Bidder chỉ đấu giá, Seller đăng sản phẩm
- [x] Thêm / Xoá sản phẩm — Seller chỉ xoá được sản phẩm của mình
- [x] Ngăn Seller tự đấu giá sản phẩm của chính mình
- [x] Đặt giá với kiểm tra hợp lệ (phải cao hơn giá hiện tại)
- [x] Tự động đóng phiên khi hết thời gian, xác định người thắng cuộc
- [x] Cập nhật realtime cho tất cả client đang mở
- [x] Xử lý lỗi & ngoại lệ: `AuctionClosedException`, `InvalidBidException`, `AuthenticationException`
- [x] Xử lý đấu giá đồng thời với `synchronized`
- [x] Kiến trúc Client–Server qua Java Socket (cổng 8888)
- [x] Giao diện JavaFX với FXML, áp dụng mô hình MVC
- [x] Thiết kế OOP: Abstract class, Inheritance, Polymorphism, Interface
- [x] Design Patterns: Singleton, Factory Method, Observer
- [x] Maven build + Checkstyle (Google Style)
- [x] Unit Test JUnit 5 (15 lớp kiểm thử)
- [x] CI/CD GitHub Actions (build + test + checkstyle tự động)

### Chức năng nâng cao (điểm bonus)

- [x] **Chống Sniping** — Tự động gia hạn 5 phút khi có lượt đặt giá trong 60 giây cuối
- [x] **Biểu đồ lịch sử giá** — Biểu đồ vùng (AreaChart) hiển thị lịch sử giá theo thời gian
- [x] **Nạp tiền** — Seller nạp tiền vào tài khoản qua giao diện có style
- [ ] Đấu giá tự động (Auto-Bidding) — chưa triển khai

---

## 10. Design Patterns áp dụng

| Pattern | Class | Mô tả |
|---|---|---|
| **Singleton** | `SessionManager`, `AuctionManager` | Đảm bảo chỉ có 1 instance duy nhất trong toàn hệ thống |
| **Factory Method** | `UserFactory`, `ItemFactory` | Tạo đúng lớp con theo vai trò hoặc danh mục sản phẩm |
| **Observer** | `AuctionObserver`, `AuctionManager`, `DashboardController` | Tự động thông báo khi phiên đấu giá thay đổi |

---

## 11. Thành viên nhóm

| Họ tên | MSSV | Vai trò |
|---|---|---|
| Đặng Duy Hưng | 25023274 | Frontend và giao diện người dùng; Dữ liệu, Kiểm thử, Hạ tầng |
| Lục Gia Bảo | 25023175 | Backend và Xử lý lỗi |

---

> 📎 *Link báo cáo PDF và video demo sẽ được bổ sung sau.*
