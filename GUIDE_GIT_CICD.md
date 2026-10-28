# HƯỚNG DẪN QUY TRÌNH ĐẨY CODE VÀ BUILD APK TỰ ĐỘNG (CI/CD)

Tài liệu này hướng dẫn quy trình làm việc chuẩn khi phát triển dự án:
* **Git công ty (Gitea / GitLab)**: Quản lý source code chính thức và phân nhánh (`toantv`).
* **GitHub cá nhân**: Tận dụng **GitHub Actions Cloud** để tự động build file APK không tốn tài nguyên máy thật.

---

## 1. Mô hình quản lý 2 Remote (Công ty & GitHub cá nhân)

Trong 1 dự án trên máy, bạn sẽ cấu hình 2 remote song song:
1. `origin`: Trỏ tới Git công ty (ví dụ: `https://haiyangit.vtdvn.net/S-APP-ASD/ASD036-Short-Drama3`).
2. `github`: Trỏ tới GitHub cá nhân (ví dụ: `https://github.com/Trantoan12022004/ASD036_Short_Drama`).

---

## 2. Thiết lập ban đầu (Chỉ làm 1 lần khi bắt đầu dự án mới)

### Bước 1: Tạo Repository mới trên GitHub cá nhân
1. Vào [github.com/new](https://github.com/new).
2. Đặt tên Repository (ví dụ: `ASD036_Short_Drama`).
3. Chọn chế độ **Private** (khuyến nghị để bảo mật code) và **không** tích tạo README.

### Bước 2: Thêm remote `github` vào dự án ở máy
Mở Terminal tại thư mục gốc dự án và chạy các lệnh:

```powershell
# 1. Kiểm tra danh sách remote hiện tại
git remote -v

# 2. Thêm remote trỏ về GitHub cá nhân của bạn
git remote add github https://github.com/<USERNAME_CUA_BAN>/<TEN_REPO_GITHUB>.git

# 3. Kiểm tra lại để chắc chắn có cả origin và github
git remote -v
```

### Bước 3: Đảm bảo dự án đã có file `.github/workflows/build-apk.yml`
File này nằm tại thư mục `.github/workflows/build-apk.yml`. Nếu dự án mới chưa có, copy file này vào để GitHub Actions biết cách build APK.

---

## 3. Quy trình làm việc hàng ngày (Daily Workflow)

Mỗi khi bạn code xong 1 tính năng hoặc muốn xuất file APK để test:

### Bước 1: Kiểm tra trạng thái code
```powershell
git status
```

### Bước 2: Lưu và Commit các thay đổi
```powershell
# Thêm toàn bộ file đã chỉnh sửa vào staging
git add .

# Tạo commit với nội dung mô tả công việc
git commit -m "feat: cap nhat giao dien va tinh nang moi"
```

### Bước 3: Đẩy code lên Git công ty (Nhánh `toantv`)
```powershell
git push origin toantv
```

### Bước 4: Đẩy code lên GitHub cá nhân để kích hoạt Cloud Build APK
```powershell
git push github toantv
```

---

## 4. Cách tải file APK sau khi GitHub Actions build xong

1. Truy cập vào Repository GitHub cá nhân của bạn trên trình duyệt (bằng điện thoại hoặc máy tính).
2. Nhấn vào tab **`Actions`** ở thanh menu trên cùng.
3. Nhấn vào Workflow run gần nhất đang hiển thị tên commit bạn vừa push.
4. Chờ khoảng **2 - 4 phút** cho đến khi có icon tích xanh **`completed`** ✅.
5. Cuộn xuống mục **Artifacts** ở cuối trang.
6. Click vào **`App-Debug-APK`** để tải file `.zip` chứa file APK về máy và cài đặt.

---

## 5. Mẹo nâng cao: Cấu hình 1 lệnh đẩy code lên cả 2 nơi cùng lúc

Nếu bạn muốn chỉ cần gõ 1 lệnh `git push all` là code tự động được đẩy lên cả Git công ty và GitHub:

```powershell
# Tạo remote tên là "all" chứa cả 2 địa chỉ:
git remote add all https://haiyangit.vtdvn.net/S-APP-ASD/ASD036-Short-Drama3
git remote set-url --add --push all https://haiyangit.vtdvn.net/S-APP-ASD/ASD036-Short-Drama3
git remote set-url --add --push all https://github.com/Trantoan12022004/ASD036_Short_Drama

# Từ giờ, mỗi lần muốn đẩy lên cả 2 nơi chỉ cần chạy:
git push all toantv
```

---

## 6. Xử lý các lỗi thường gặp

### Lỗi 1: `error: src refspec master does not match any`
* **Nguyên nhân**: Nhánh hiện tại trên máy bạn là `toantv`, nhưng bạn gõ lệnh push nhánh `master`.
* **Cách khắc phục**: Luôn kiểm tra tên nhánh bằng lệnh `git branch` và gõ đúng tên nhánh: `git push github toantv`.

### Lỗi 2: Yêu cầu mật khẩu khi push lên GitHub (Authentication failed)
* **Nguyên nhân**: GitHub không còn hỗ trợ dùng mật khẩu tài khoản để push code qua HTTPS mà yêu cầu dùng **Personal Access Token (PAT)**.
* **Cách khắc phục**:
  1. Trên GitHub: Vào **Settings** > **Developer Settings** > **Personal access tokens** > **Tokens (classic)**.
  2. Bấm **Generate new token (classic)**, tích chọn quyền **`repo`**, sau đó copy mã token (bắt đầu bằng `ghp_...`).
  3. Khi Terminal hỏi Password thì dán mã token này vào.

### Lỗi 3: Bị từ chối push do GitHub có commit mới (`Updates were rejected`)
* **Cách khắc phục**:
  ```powershell
  # Kéo code mới về trước
  git pull github toantv --rebase
  # Hoặc nếu repo GitHub cá nhân chỉ dùng để build APK (không sợ ghi đè):
  git push github toantv --force
  ```
