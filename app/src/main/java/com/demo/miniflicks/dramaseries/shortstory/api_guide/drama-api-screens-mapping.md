# Drama API & Screen Mapping Document

Tài liệu này cung cấp cái nhìn chi tiết về các API hiện có và gợi ý cách map (gắn) từng API vào các màn hình (screens) và tính năng (features) cụ thể trong một ứng dụng xem phim ngắn (Short Drama). 

---

## 1. Màn hình Trang chủ (Home Screen)
Màn hình này thường hiển thị tổng hợp nhiều nội dung hấp dẫn nhất để lôi cuốn người dùng ngay từ lúc mở app.

* **Hiển thị danh sách các Thể loại (Tab/Filter):**
  * **API:** `GET /short-dram/categories/names`
  * **Chức năng:** Lấy danh sách tên các thể loại (Romance, Comedy, v.v.) để làm menu hoặc các Tab ngang (TabLayout) trên cùng của màn hình.
  
* **Hiển thị danh sách Phim theo từng Thể loại (Carousel dọc/ngang):**
  * **API:** `GET /short-dram/categories/movies` hoặc `GET /short-dram/categories/movies/page`
  * **Chức năng:** Trả về một Map chứa phim theo từng danh mục. Rất phù hợp để render giao diện dạng các khối (mỗi khối là một Thể loại, bên trong là danh sách phim cuộn ngang).

* **Hiển thị mục Phim xem nhiều (Top Trending/Hot):**
  * **API:** `GET /short-dram/movies/top-viewed`
  * **Chức năng:** Render khối giao diện "Top phim thịnh hành" hoặc "Nhiều người xem nhất" với các badge xếp hạng (1, 2, 3).

* **Hiển thị mục Phim đánh giá cao (Top Rated):**
  * **API:** `GET /short-dram/movies/top-rated`
  * **Chức năng:** Hiển thị khu vực phim được đánh giá cao, có biểu tượng ngôi sao.

* **Xem thêm tất cả phim (Explore/All Movies):**
  * **API:** `GET /short-dram/movies/page?page={page}&size={size}`
  * **Chức năng:** Khi cuộn xuống cuối trang hoặc bấm vào nút "Xem tất cả", load thêm danh sách phim dạng phân trang (Infinite Scroll).

---

## 2. Màn hình Tìm kiếm (Search Screen)
Nơi người dùng có thể gõ từ khoá hoặc xem các gợi ý thịnh hành.

* **Từ khoá thịnh hành (Hot Searches):**
  * **API:** `GET /short-dram/movies/top-searched`
  * **Chức năng:** Khi người dùng vừa mở màn hình tìm kiếm (chưa gõ gì), gọi API này để hiển thị danh sách các phim hoặc từ khoá đang được tìm kiếm nhiều nhất để gợi ý.

* **Thực hiện Tìm kiếm phim:**
  * **API:** `GET /short-dram/movies/search?name={keyword}&page={page}&size={size}`
  * **Chức năng:** Khi người dùng nhập từ khoá và ấn Enter, gọi API này để lấy danh sách phim khớp với từ khoá. Hỗ trợ phân trang để load thêm khi cuộn (Load more).

---

## 3. Màn hình Danh sách theo Thể loại (Category Detail Screen)
Khi người dùng bấm vào mục "Xem tất cả" của một thể loại nào đó trên trang chủ.

* **Lấy toàn bộ phim của một Thể loại (có phân trang):**
  * **API:** `GET /short-dram/movies/by-category?name={CategoryName}&page={page}&size={size}`
  * **Chức năng:** Hiển thị dưới dạng Grid view (lưới 2-3 cột) chứa tất cả phim của thể loại đó, tự động load thêm trang mới khi cuộn xuống.

---

## 4. Màn hình Chi tiết Phim (Movie Detail Screen)
Sau khi bấm vào một bộ phim bất kỳ, màn hình này sẽ hiện ra thông tin chi tiết và danh sách tập phim.

* **Lấy thông tin chi tiết bộ phim:**
  * **API:** `GET /short-dram/movies/{id}`
  * **Chức năng:** Trả về thông tin chi tiết: Tên phim, ảnh bìa, số lượt xem, rating, tổng số tập phim và quan trọng nhất là **Danh sách toàn bộ các tập (episodes)** của phim đó. Giao diện sẽ hiển thị phần giới thiệu ở trên và bảng danh sách tập (Episode List) ở dưới.
  
* **Truy cập phim qua link chia sẻ (Deep link / Legacy ID):**
  * **API:** `GET /short-dram/movies/legacy/{legacyId}`
  * **Chức năng:** Tương tự API trên, nhưng dùng cho trường hợp người dùng mở app qua một link chia sẻ có chứa ID định dạng cũ (Legacy ID).

---

## 5. Màn hình Xem Phim (Player / Watch Screen)
Nơi trực tiếp phát video (thường dùng dạng cuộn TikTok hoặc ExoPlayer ngang).

* **Lấy thông tin xem trước (Preview):**
  * **API:** `GET /short-dram/movies/{id}/episodes/preview`
  * **Chức năng:** Lấy nhanh những tập được phép xem thử (miễn phí, không khóa). Dành cho luồng hiển thị trailer hoặc preview tập phim.

* **Mở khoá 1 tập phim (Unlock Single Episode):**
  * **API:** `POST /short-dram/episodes/{id}/unlock`
  * **Chức năng:** Người dùng xem hết tập miễn phí và bị chặn lại ở tập VIP. Khi họ bấm nút "Dùng xu để mở khoá" hoặc "Xem quảng cáo để mở khoá", ứng dụng sẽ gọi API này để mở khóa. Server sẽ trả về thông tin tập phim (chứa URL video thật) để player có thể phát.

* **Mở khoá nhiều tập (Unlock Multiple Episodes - Mua trọn bộ):**
  * **API:** `POST /short-dram/episodes/unlock`
  * **Chức năng:** Tính năng "Mở khoá các tập tiếp theo" (ví dụ mở 10 tập cùng lúc với giá rẻ hơn). Ứng dụng gửi lên danh sách các Episode ID và server trả về danh sách các tập đã được cấp quyền truy cập.

---

## Tóm tắt các Luồng chức năng (Workflows)

1. **Luồng Khám phá (Discovery):** 
   `Home Screen (Categories, Top Viewed)` -> Bấm vào Phim -> `Detail Screen (Movie Info, Episode List)`.
2. **Luồng Xem và Mở khóa (Watch & Monetization):**
   `Detail Screen` -> Bấm vào Tập 1 (miễn phí) -> `Player Screen` cuộn đến Tập 3 (bị khóa) -> Hiển thị Popup Mở khoá -> Gọi API `Unlock` -> Thành công -> Phát tiếp video.
3. **Luồng Tìm kiếm (Search):**
   `Search Screen` (hiện `Top Searched`) -> Gõ chữ tìm kiếm -> Gọi API `Search` -> Hiện danh sách phim -> Bấm vào Phim -> `Detail Screen`.
