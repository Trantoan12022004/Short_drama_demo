# Drama API Documentation

Tài liệu này được tạo megalondon

## Base URL

Ứng dụng dùng base URL runtime từ remote config/storage (`base_url`). Nếu không có hoặc URL không hợp lệ, fallback hiện tại là:

```text
https://shortdrama-data.v1solution.com/
```

Trong tài liệu này, dùng placeholder:

```text
{BASE_URL}
```

Ví dụ URL đầy đủ:

```text
{BASE_URL}/short-dram/movies/page?page=0&size=20
```

## Headers Chung

Tất cả endpoint trong `DramaService` đều yêu cầu các header sau:

| Header | Type | Required | Mô tả         |
| --- | --- | --- |---------------|
| `X-Api-Key-Token` | `String` | Yes | API key token |
| `X-Package-Name` | `String` | Yes | Package  Name  |

Ví dụ:

```http
X-Api-Key-Token: <api-key-token>
X-Package-Name: com.example.api.megalondon
```

## Danh Sách Endpoint

| Method | Path | Function | Response |
| --- | --- | --- | --- |
| `GET` | `/short-dram/movies` | `getMovies` | `MovieListResponse[]` |
| `GET` | `/short-dram/movies/page` | `getMoviesPage` | `MoviePageResponse` |
| `GET` | `/short-dram/movies/top-viewed` | `getTopViewedMovies` | `MovieListResponse[]` |
| `GET` | `/short-dram/movies/top-rated` | `getTopRatedMovies` | `MovieListResponse[]` |
| `GET` | `/short-dram/movies/top-searched` | `getTopSearchedMovies` | `MovieListResponse[]` |
| `GET` | `/short-dram/movies/by-category` | `getMoviesByCategory` | `MoviePageResponse` |
| `GET` | `/short-dram/movies/by-category/all` | `getAllMoviesByCategory` | `MovieListResponse[]` |
| `GET` | `/short-dram/movies/search` | `searchMovies` | `MoviePageResponse` |
| `GET` | `/short-dram/movies/search/all` | `searchAllMovies` | `MovieListResponse[]` |
| `GET` | `/short-dram/categories/names` | `getCategoryNames` | `String[]` |
| `GET` | `/short-dram/categories/movies` | `getCategoryMovies` | `Map<String, MovieListResponse[]>` |
| `GET` | `/short-dram/categories/movies/page` | `getCategoryMoviesPage` | `Map<String, MoviePageResponse>` |
| `GET` | `/short-dram/movies/{id}` | `getMovie` | `MovieDetailResponse` |
| `GET` | `/short-dram/movies/{id}/episodes/preview` | `getMovieEpisodePreview` | `MovieDetailResponse` |
| `POST` | `/short-dram/episodes/{id}/unlock` | `unlockEpisode` | `EpisodeResponse` |
| `POST` | `/short-dram/episodes/unlock` | `unlockEpisodes` | `EpisodeResponse[]` |
| `GET` | `/short-dram/movies/legacy/{legacyId}` | `getMovieByLegacyId` | `MovieDetailResponse` |

## Endpoint Chi Tiết

### 1. Get Movies

```http
GET /short-dram/movies
```

Query params: không có.

Response: `MovieListResponse[]`.

### 2. Get Movies Page

```http
GET /short-dram/movies/page?page=0&size=20
```

| Query | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `page` | `Int` | No | `0` | Trang cần lấy. |
| `size` | `Int` | No | `20` | Số item mỗi trang. |

Response: `MoviePageResponse`.

### 3. Get Top Viewed Movies

```http
GET /short-dram/movies/top-viewed
```

Query params: không có.

Response: `MovieListResponse[]`.

### 4. Get Top Rated Movies

```http
GET /short-dram/movies/top-rated
```

Query params: không có.

Response: `MovieListResponse[]`.

### 5. Get Top Searched Movies

```http
GET /short-dram/movies/top-searched
```

Query params: không có.

Response: `MovieListResponse[]`.

### 6. Get Movies By Category

```http
GET /short-dram/movies/by-category?name=Romance&page=0&size=20
```

| Query | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `name` | `String` | Yes | - | Tên category. |
| `page` | `Int` | No | `0` | Trang cần lấy. |
| `size` | `Int` | No | `20` | Số item mỗi trang. |

Response: `MoviePageResponse`.

### 7. Get All Movies By Category

```http
GET /short-dram/movies/by-category/all?name=Romance
```

| Query | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `name` | `String` | Yes | - | Tên category. |

Response: `MovieListResponse[]`.

### 8. Search Movies

```http
GET /short-dram/movies/search?name=love&page=0&size=20
```

| Query | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `name` | `String` | Yes | - | Từ khóa tìm kiếm. |
| `page` | `Int` | No | `0` | Trang cần lấy. |
| `size` | `Int` | No | `20` | Số item mỗi trang. |

Response: `MoviePageResponse`.

### 9. Search All Movies

```http
GET /short-dram/movies/search/all?name=love
```

| Query | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `name` | `String` | Yes | - | Từ khóa tìm kiếm. |

Response: `MovieListResponse[]`.

### 10. Get Category Names

```http
GET /short-dram/categories/names
```

Query params: không có.

Response: `String[]`.

Ví dụ:

```json
[
  "Romance",
  "Drama",
  "Comedy"
]
```

### 11. Get Category Movies

```http
GET /short-dram/categories/movies
```

Query params: không có.

Response: object map theo tên category.

```json
{
  "Romance": [
    {
      "id": 1,
      "legacyId": "movie-001",
      "subtitle": true,
      "rating": 4.8,
      "viewCount": 120000,
      "searchCount": 3200,
      "totalEpisode": 60,
      "firstEpisode": {
        "id": 101,
        "legacyId": "episode-001",
        "episodeNumber": 1,
        "url": "https://cdn.example.com/video.mp4",
        "premium": false,
        "rating": 4.8,
        "viewCount": 10000,
        "thumbnails": [],
        "subtitles": []
      },
      "names": [],
      "covers": [],
      "categories": []
    }
  ]
}
```

### 12. Get Category Movies Page

```http
GET /short-dram/categories/movies/page?page=0&size=20
```

| Query | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `page` | `Int` | No | `0` | Trang cần lấy cho từng category. |
| `size` | `Int` | No | `20` | Số item mỗi trang cho từng category. |

Response: object map theo tên category, mỗi value là `MoviePageResponse`.

### 13. Get Movie Detail

```http
GET /short-dram/movies/{id}
```

| Path | Type | Required | Mô tả |
| --- | --- | --- | --- |
| `id` | `Long` | Yes | Movie ID. |

Response: `MovieDetailResponse`.

### 14. Get Movie Episode Preview

```http
GET /short-dram/movies/{id}/episodes/preview
```

| Path | Type | Required | Mô tả |
| --- | --- | --- | --- |
| `id` | `Long` | Yes | Movie ID. |

Response: `MovieDetailResponse`.

### 15. Unlock Episode

```http
POST /short-dram/episodes/{id}/unlock
```

| Path | Type | Required | Mô tả |
| --- | --- | --- | --- |
| `id` | `Long` | Yes | Episode ID. |

Request body: không có.

Response: `EpisodeResponse`.

### 16. Unlock Episodes

```http
POST /short-dram/episodes/unlock
Content-Type: application/json
```

Request body: JSON array episode IDs.

```json
[
  101,
  102,
  103
]
```

Response: `EpisodeResponse[]`.

### 17. Get Movie By Legacy ID

```http
GET /short-dram/movies/legacy/{legacyId}
```

| Path | Type | Required | Mô tả |
| --- | --- | --- | --- |
| `legacyId` | `String` | Yes | Legacy movie ID. |

Response: `MovieDetailResponse`.

## Response Schemas

### `LocalizedTextResponse`

```json
{
  "languageCode": "en",
  "languageName": "English",
  "value": "Example text"
}
```

| Field | Type | Nullable | Mô tả |
| --- | --- | --- | --- |
| `languageCode` | `String` | Yes | Mã ngôn ngữ. |
| `languageName` | `String` | Yes | Tên ngôn ngữ. |
| `value` | `String` | Yes | Nội dung localized. |

### `CategoryResponse`

```json
{
  "id": 1,
  "slug": "romance",
  "translations": [
    {
      "languageCode": "en",
      "languageName": "English",
      "value": "Romance"
    }
  ]
}
```

| Field | Type | Nullable | Mô tả |
| --- | --- | --- | --- |
| `id` | `Long` | Yes | Category ID. |
| `slug` | `String` | Yes | Category slug. |
| `translations` | `LocalizedTextResponse[]` | Yes | Danh sách bản dịch tên category. |

### `EpisodeResponse`

```json
{
  "id": 101,
  "legacyId": "episode-001",
  "episodeNumber": 1,
  "url": "https://cdn.example.com/video.mp4",
  "premium": false,
  "rating": 4.8,
  "viewCount": 10000,
  "thumbnails": [
    {
      "languageCode": "en",
      "languageName": "English",
      "value": "https://cdn.example.com/thumb.jpg"
    }
  ],
  "subtitles": [
    {
      "languageCode": "en",
      "languageName": "English",
      "value": "https://cdn.example.com/subtitle.vtt"
    }
  ]
}
```

| Field | Type | Nullable | Mô tả |
| --- | --- | --- | --- |
| `id` | `Long` | Yes | Episode ID. |
| `legacyId` | `String` | Yes | Legacy episode ID. |
| `episodeNumber` | `Int` | Yes | Số tập. |
| `url` | `String` | Yes | URL video. |
| `premium` | `Boolean` | Yes | Episode premium hay không. |
| `rating` | `Decimal` | Yes | Rating. |
| `viewCount` | `Long` | Yes | Số lượt xem. |
| `thumbnails` | `LocalizedTextResponse[]` | Yes | Danh sách thumbnail theo ngôn ngữ/locale. |
| `subtitles` | `LocalizedTextResponse[]` | Yes | Danh sách subtitle theo ngôn ngữ/locale. |

### `MovieListResponse`

```json
{
  "id": 1,
  "legacyId": "movie-001",
  "subtitle": true,
  "rating": 4.8,
  "viewCount": 120000,
  "searchCount": 3200,
  "totalEpisode": 60,
  "firstEpisode": {
    "id": 101,
    "legacyId": "episode-001",
    "episodeNumber": 1,
    "url": "https://cdn.example.com/video.mp4",
    "premium": false,
    "rating": 4.8,
    "viewCount": 10000,
    "thumbnails": [],
    "subtitles": []
  },
  "names": [
    {
      "languageCode": "en",
      "languageName": "English",
      "value": "Movie name"
    }
  ],
  "covers": [
    {
      "languageCode": "en",
      "languageName": "English",
      "value": "https://cdn.example.com/cover.jpg"
    }
  ],
  "categories": []
}
```

| Field | Type | Nullable | Mô tả |
| --- | --- | --- | --- |
| `id` | `Long` | No | Movie ID. |
| `legacyId` | `String` | No | Legacy movie ID. |
| `subtitle` | `Boolean` | No | Movie có subtitle hay không. |
| `rating` | `Decimal` | No | Rating. |
| `viewCount` | `Long` | No | Số lượt xem. |
| `searchCount` | `Long` | No | Số lượt tìm kiếm. |
| `totalEpisode` | `Int` | No | Tổng số tập. |
| `firstEpisode` | `EpisodeResponse` | No | Tập đầu tiên. |
| `names` | `LocalizedTextResponse[]` | No | Danh sách tên theo ngôn ngữ/locale. |
| `covers` | `LocalizedTextResponse[]` | No | Danh sách cover theo ngôn ngữ/locale. |
| `categories` | `CategoryResponse[]` | No | Danh sách category. |

### `MovieDetailResponse`

```json
{
  "id": 1,
  "legacyId": "movie-001",
  "subtitle": true,
  "rating": 4.8,
  "viewCount": 120000,
  "searchCount": 3200,
  "totalEpisode": 60,
  "names": [],
  "covers": [],
  "categories": [],
  "episodes": []
}
```

| Field | Type | Nullable | Mô tả |
| --- | --- | --- | --- |
| `id` | `Long` | Yes | Movie ID. |
| `legacyId` | `String` | Yes | Legacy movie ID. |
| `subtitle` | `Boolean` | Yes | Movie có subtitle hay không. |
| `rating` | `Decimal` | Yes | Rating. |
| `viewCount` | `Long` | Yes | Số lượt xem. |
| `searchCount` | `Long` | Yes | Số lượt tìm kiếm. |
| `totalEpisode` | `Int` | Yes | Tổng số tập. |
| `names` | `LocalizedTextResponse[]` | Yes | Danh sách tên theo ngôn ngữ/locale. |
| `covers` | `LocalizedTextResponse[]` | Yes | Danh sách cover theo ngôn ngữ/locale. |
| `categories` | `CategoryResponse[]` | Yes | Danh sách category. |
| `episodes` | `EpisodeResponse[]` | Yes | Danh sách episode. |

### `MoviePageResponse`

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

| Field | Type | Nullable | Mô tả |
| --- | --- | --- | --- |
| `items` | `MovieListResponse[]` | No | Danh sách movie trong trang. |
| `page` | `Int` | No | Trang hiện tại. |
| `size` | `Int` | No | Số item mỗi trang. |
| `totalElements` | `Long` | No | Tổng số item. |
| `totalPages` | `Int` | No | Tổng số trang. |
| `first` | `Boolean` | No | Có phải trang đầu không. |
| `last` | `Boolean` | No | Có phải trang cuối không. |

## cURL Examples

### Get movies page

```bash
curl -X GET "{BASE_URL}/short-dram/movies/page?page=0&size=20" \
  -H "X-Api-Key-Token: <api-key-token>" \
  -H "X-Package-Name: com.cck.dramareels.watchdrama"
```

### Search movies

```bash
curl -X GET "{BASE_URL}/short-dram/movies/search?name=love&page=0&size=20" \
  -H "X-Api-Key-Token: <api-key-token>" \
  -H "X-Package-Name: com.cck.dramareels.watchdrama"
```

### Unlock episodes

```bash
curl -X POST "{BASE_URL}/short-dram/episodes/unlock" \
  -H "Content-Type: application/json" \
  -H "X-Api-Key-Token: <api-key-token>" \
  -H "X-Package-Name: com.cck.dramareels.watchdrama" \
  -d '[101,102,103]'
```

## Ghi Chú

- Tài liệu này chỉ mô tả contract đang được khai báo trong client Android. Nếu backend có wrapper response hoặc error format riêng, cần bổ sung từ tài liệu backend thực tế.
- Các field `Decimal` trong Kotlin là `BigDecimal`, khi serialize JSON thường là number.
- Các endpoint page có default client-side `page=0` và `size=20` theo `DramaService.kt`.
