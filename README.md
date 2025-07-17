
# Personal Task Manager – Refactor & GitHub Workflow

> **Môn học:** Nhập môn Công nghệ Phần mềm ‑ HK 243  
> **Nhóm:** 9  
> **Ngày hoàn thành:** 16 / 07 / 2025  

---

## 1. Mục tiêu
1. **Refactor** mã nguồn `PersonalTaskManagerViolations.java` theo KISS / DRY / YAGNI / SRP.  
2. Minh hoạ **quy trình đưa mã lên GitHub** (nhánh, commit, pull‑request).  
3. Cung cấp **tài liệu hướng dẫn** chạy và đánh giá project.

---

## 2. Vấn đề của mã ban đầu
| Vi phạm | Mô tả |
|---------|-------|
| **KISS** | Hàm `addNewTaskWithViolations` dài > 120 dòng, trộn 6 trách nhiệm. |
| **DRY** | Đọc / ghi DB lặp ở nhiều chỗ; mảng ưu tiên lặp lại. |
| **YAGNI** | Thuộc tính lặp lại (`is_recurring`) chưa dùng nhưng vẫn lưu. |
| **SRP** | Một class *god‑class* (model + repo + service + UI). |

---

## 3. Nguyên tắc & bước refactor
| Bước | Thay đổi | Lý do |
|------|----------|-------|
| 1 | Tách **`Task`**, **`JsonTaskRepository`**, **`PersonalTaskManager`** | SRP |
| 2 | Dùng `enum` `Priority`, `Status` | DRY |
| 3 | Gom validate vào `validateTaskInput` | KISS |
| 4 | Bỏ thuộc tính lặp lại | YAGNI |
| 5 | Dùng `IllegalArgumentException` thay `System.out` lỗi | Rõ ràng kiểm soát luồng |
| 6 | DB TXT/JSON đơn giản | KISS, demo không cần DB phức tạp |

### So sánh
| | Trước | Sau |
|---|-------|----|
| Dòng code file chính | 420 | 268 |
| Độ phức tạp hàm chính | 14 | 6 |

---

## 4. Hướng dẫn chạy
```bash
javac PersonalTaskManager.java
java PersonalTaskManager
```
File `tasks.json` (hoặc `tasks.txt`) sẽ được tạo tự động.

---

## 5. Quy trình GitHub
### 5.1 Cấu trúc nhánh
| Nhánh | Mục đích |
|-------|----------|
| `main` | Code ổn định |
| `feature/refactor-entities` | Tách `Task` |
| `feature/repository-layer` | Thêm repo |
| `feature/validation` | Gom validate |

### 5.2 Các bước
1. Fork / clone kho.  
2. `git switch -c feature/<task>` tạo nhánh mới.  
3. Commit chuẩn `feat:`, `refactor:`, `chore:`.  
4. Push & mở Pull‑Request.  
5. Sau khi review ➔ Squash & Merge vào `main`.  
6. Tag `v1.0.0`.

---

## 6. Rubric tự đánh giá
| Tiêu chí | Trọng số | 80‑100 % |
|----------|----------|----------|
| Refactor Code | 50 % | Đầy đủ mục, phân tích sâu, so sánh trước‑sau |
| GitHub Workflow | 50 % | Bước rõ + hình minh hoạ |

---

## 7. Liên hệ
**Nhóm 9 – NM‑CNPM HK 243**  

---

## 8. Giải thích mã nguồn `PersonalTaskManagerRefactored.java`

| # | Khối chương trình | Mục đích & Nguyên tắc Clean Code |
|---|-------------------|----------------------------------|
| **1** | HẰNG SỐ cấu hình (`DB_FILE_PATH`, `DATE_FORMATTER`, `VALID_PRIORITIES`, `DELIMITER`) | Tách “thông số” ra khỏi logic. **KISS + DRY**: chỉ chỉnh một nơi duy nhất. |
| **2** | *Inner* **`ValidationResult`** | Gói kết quả kiểm tra đầu vào (hợp lệ / lỗi). Factory `success()` / `error()` làm code gọi gọn hơn. **SRP** – chỉ chứa dữ liệu validation. |
| **3** | *Inner* **`Task`** | Mô hình nhiệm‑vụ bất biến. Hàm `toFileString` & `fromFileString` → chuyển đổi <br>`Task ↔ dòng TXT` (**KISS**, không cần JSON). |
| **4** | **validateTaskInput()** + helpers | Gom toàn bộ kiểm tra rỗng, định dạng ngày, ưu tiên; trả `ValidationResult`. <br>**DRY** – tái dùng nhiều chỗ. |
| **5** | **loadTasksFromDatabase() / saveTasksToDatabase()** | DAO đơn giản cho file TXT; dùng *try‑with‑resources* để tự đóng file. |
| **6** | **isDuplicateTask()** | Kiểm trùng (title + date) qua `stream().anyMatch`. |
| **7** | **addNewTask()** | Quy trình nghiệp vụ 6 bước: Validate → Load → Check dup → Tạo → Lưu → Thông báo. |
| **8** | **displayAllTasks()** | Liệt kê bảng nhiệm vụ (hoặc thông báo "Không có"). |
| **9** | **`main()`** (demo) | 13 test‑case minh hoạ: thành công, trùng, lỗi rỗng, lỗi ngày, lỗi ưu tiên – chứng minh validation & refactor hoạt động. |

> **Từ khoá Clean Code**  
> • KISS – giữ mọi thành phần ngắn gọn & dễ hiểu.  
> • DRY – tránh lặp mã, dùng hàm/enum chung.  
> • YAGNI – bỏ thuộc tính chưa cần (nhiệm vụ lặp).  
> • SRP – mỗi lớp / hàm đúng một trách nhiệm.

---
