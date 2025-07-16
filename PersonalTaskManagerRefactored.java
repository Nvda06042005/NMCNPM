import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

class PersonalTaskManagerRefactored {
    
    private static final String DB_FILE_PATH = "tasks_database.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] VALID_PRIORITIES = {"Thap", "Trung binh", "Cao"};
    private static final String DELIMITER = "|";
    
    // ===== INNER CLASSES =====
    
    /**
     * Lop dai dien cho ket qua validation
     */
    private static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        private final LocalDate dueDate;
        
        private ValidationResult(boolean isValid, String errorMessage, LocalDate dueDate) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.dueDate = dueDate;
        }
        
        public static ValidationResult success(LocalDate dueDate) {
            return new ValidationResult(true, null, dueDate);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null);
        }
        
        public boolean isValid() { return isValid; }
        public String getErrorMessage() { return errorMessage; }
        public LocalDate getDueDate() { return dueDate; }
    }
    
    /**
     * Lop dai dien cho mot nhiem vu
     */
    private static class Task {
        private final String id;
        private final String title;
        private final String description;
        private final LocalDate dueDate;
        private final String priority;
        private final String status;
        private final LocalDateTime createdAt;
        private final LocalDateTime lastUpdatedAt;
        
        public Task(String title, String description, LocalDate dueDate, String priority) {
            this.id = generateSimpleId();
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.priority = priority;
            this.status = "Da hoan thanh";
            this.createdAt = LocalDateTime.now();
            this.lastUpdatedAt = LocalDateTime.now();
        }
        
        // Constructor de tao tu du lieu da co
        public Task(String id, String title, String description, LocalDate dueDate, 
                   String priority, String status, LocalDateTime createdAt, LocalDateTime lastUpdatedAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.priority = priority;
            this.status = status;
            this.createdAt = createdAt;
            this.lastUpdatedAt = lastUpdatedAt;
        }
        
        private String generateSimpleId() {
            return String.valueOf(System.currentTimeMillis());
        }
        
        public String toFileString() {
            return String.join(DELIMITER,
                id,
                title,
                description != null ? description : "",
                dueDate.format(DATE_FORMATTER),
                priority,
                status,
                createdAt.format(DateTimeFormatter.ISO_DATE_TIME),
                lastUpdatedAt.format(DateTimeFormatter.ISO_DATE_TIME)
            );
        }
        
        public static Task fromFileString(String line) {
            String[] parts = line.split("\\" + DELIMITER);
            if (parts.length >= 8) {
                return new Task(
                    parts[0], // id
                    parts[1], // title
                    parts[2].isEmpty() ? null : parts[2], // description
                    LocalDate.parse(parts[3], DATE_FORMATTER), // dueDate
                    parts[4], // priority
                    parts[5], // status
                    LocalDateTime.parse(parts[6]), // createdAt
                    LocalDateTime.parse(parts[7])  // lastUpdatedAt
                );
            }
            return null;
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDate getDueDate() { return dueDate; }
        public String getPriority() { return priority; }
        public String getStatus() { return status; }
    }
    
    // ===== VALIDATION METHODS =====
    
    /**
     * Validate thong tin nhiem vu
     */
    private ValidationResult validateTaskInput(String title, String dueDateStr, String priorityLevel) {
        if (isEmptyOrNull(title)) {
            return ValidationResult.error("Tieu de khong duoc de trong.");
        }
        
        if (isEmptyOrNull(dueDateStr)) {
            return ValidationResult.error("Ngay den han khong duoc de trong.");
        }
        
        LocalDate dueDate = parseDate(dueDateStr);
        if (dueDate == null) {
            return ValidationResult.error("Ngay den han khong hop le. Vui long su dung dinh dang YYYY-MM-DD.");
        }
        
        if (!isValidPriority(priorityLevel)) {
            return ValidationResult.error("Muc do uu tien khong hop le. Vui long chon tu: Thap, Trung binh, Cao.");
        }
        
        return ValidationResult.success(dueDate);
    }
    
    private boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    private boolean isValidPriority(String priority) {
        return Arrays.stream(VALID_PRIORITIES)
                    .anyMatch(validP -> validP.equals(priority));
    }
    
    // ===== DATABASE METHODS =====
    
    /**
     * Tai danh sach nhiem vu tu database
     */
    private List<Task> loadTasksFromDatabase() {
        List<Task> tasks = new ArrayList<>();
        File file = new File(DB_FILE_PATH);
        
        if (!file.exists()) {
            return tasks;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Task task = Task.fromFileString(line);
                    if (task != null) {
                        tasks.add(task);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Loi khi doc file database: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Luu danh sach nhiem vu vao database
     */
    private void saveTasksToDatabase(List<Task> tasks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DB_FILE_PATH))) {
            for (Task task : tasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Loi khi ghi vao file database: " + e.getMessage());
        }
    }
    
    /**
     * Kiem tra nhiem vu trung lap
     */
    private boolean isDuplicateTask(List<Task> tasks, String title, String dueDate) {
        return tasks.stream()
                   .anyMatch(task -> 
                       task.getTitle().equalsIgnoreCase(title) &&
                       task.getDueDate().format(DATE_FORMATTER).equals(dueDate)
                   );
    }
    
    // ===== BUSINESS LOGIC METHODS =====
    
    /**
     * Them nhiem vu moi (da duoc refactor)
     */
    public Task addNewTask(String title, String description, String dueDateStr, String priorityLevel) {
        // Buoc 1: Validate input
        ValidationResult validation = validateTaskInput(title, dueDateStr, priorityLevel);
        if (!validation.isValid()) {
            System.out.println("Loi: " + validation.getErrorMessage());
            return null;
        }
        
        // Buoc 2: Load du lieu
        List<Task> tasks = loadTasksFromDatabase();
        
        // Buoc 3: Kiem tra trung lap
        String formattedDate = validation.getDueDate().format(DATE_FORMATTER);
        if (isDuplicateTask(tasks, title, formattedDate)) {
            System.out.println(String.format("Loi: Nhiem vu '%s' da ton tai voi cung ngay den han.", title));
            return null;
        }
        
        // Buoc 4: Tao nhiem vu moi
        Task newTask = createNewTask(title, description, validation.getDueDate(), priorityLevel);
        
        // Buoc 5: Luu vao database
        tasks.add(newTask);
        saveTasksToDatabase(tasks);
        
        // Buoc 6: Thong bao thanh cong
        System.out.println(String.format("Da them nhiem vu moi thanh cong voi ID: %s", newTask.getId()));
        return newTask;
    }
    
    /**
     * Tao doi tuong nhiem vu moi
     */
    private Task createNewTask(String title, String description, LocalDate dueDate, String priority) {
        return new Task(title, description, dueDate, priority);
    }
    
    /**
     * Hien thi tat ca nhiem vu
     */
    public void displayAllTasks() {
        List<Task> tasks = loadTasksFromDatabase();
        if (tasks.isEmpty()) {
            System.out.println("Khong co nhiem vu nao.");
            return;
        }
        
        System.out.println("\n=== DANH SACH NHIEM VU ===");
        for (Task task : tasks) {
            System.out.printf("ID: %s | Tieu de: %s | Mo ta: %s | Ngay han: %s | Uu tien: %s | Trang thai: %s%n",
                task.getId(),
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "Khong co",
                task.getDueDate().format(DATE_FORMATTER),
                task.getPriority(),
                task.getStatus()
            );
        }
        System.out.println("========================");
    }
    
    // ===== MAIN METHOD =====
    
    public static void main(String[] args) {
        PersonalTaskManagerRefactored manager = new PersonalTaskManagerRefactored();
        
        System.out.println("\n=== DEMO REFACTORED CODE - NHOM 9 PRODUCT BACKLOG ===");
        System.out.println("=== TAT CA NHIEM VU DA HOAN THANH TRONG THANG 7/2025 ===");
        
        // SPRINT 1: 1/7 - 7/7/2025
        System.out.println("\n=== SPRINT 1 (1/7 - 7/7/2025) ===");
        
        // Test case 1: Lap ke hoach & lich bieu 3 tuan (1-2/7)
        System.out.println("\n1. Lap ke hoach & lich bieu 3 tuan (hoan thanh 2/7):");
        manager.addNewTask("Lap ke hoach & lich bieu 3 tuan", "Xac dinh cac giai doan va phan cong cong viec cho 3 tuan", "2025-07-02", "Cao");
        
        // Test case 2: Khoi tao Project & Board Azure DevOps (3/7)
        System.out.println("\n2. Khoi tao Project & Board Azure DevOps (hoan thanh 3/7):");
        manager.addNewTask("Khoi tao Project & Board Azure DevOps", "Thiet lap project va board quan ly tren Azure DevOps", "2025-07-03", "Trung binh");
        
        // Test case 3: Xay dung Product Backlog (4-5/7)
        System.out.println("\n3. Xay dung Product Backlog (hoan thanh 5/7):");
        manager.addNewTask("Xay dung Product Backlog", "Viet User Stories va Acceptance Criteria cho ung dung", "2025-07-05", "Cao");
        
        // Test case 4: Thiet ke Context Diagram (6-7/7)
        System.out.println("\n4. Thiet ke Context Diagram (hoan thanh 7/7):");
        manager.addNewTask("Thiet ke Context Diagram", "Ve Context Diagram cho he thong quan ly nhiem vu", "2025-07-07", "Trung binh");
        
        // SPRINT 2: 8/7 - 15/7/2025
        System.out.println("\n=== SPRINT 2 (8/7 - 15/7/2025) ===");
        
        // Test case 5: Chinh sua & hoan thien Context Diagram (8-9/7)
        System.out.println("\n5. Chinh sua & hoan thien Context Diagram (hoan thanh 9/7):");
        manager.addNewTask("Chinh sua & hoan thien Context Diagram", "Ra soat va chinh sua Context Diagram theo gop y", "2025-07-09", "Trung binh");
        
        // Test case 6: Bo sung & ra soat User Stories (10-11/7)
        System.out.println("\n6. Bo sung & ra soat User Stories (hoan thanh 11/7):");
        manager.addNewTask("Bo sung & ra soat User Stories", "Hoan thien User Stories va Acceptance Criteria chi tiet", "2025-07-11", "Cao");
        
        // Test case 7: Thiet lap Sprint 2 board (12/7)
        System.out.println("\n7. Thiet lap Sprint 2 board (hoan thanh 12/7):");
        manager.addNewTask("Thiet lap Sprint 2 board", "Phan cong cong viec tren Azure DevOps cho Sprint 2", "2025-07-12", "Thap");
        
        // Test case 8: Refactor Code theo chuan (13-14/7)
        System.out.println("\n8. Refactor Code theo chuan (hoan thanh 14/7):");
        manager.addNewTask("Refactor Code theo chuan", "Ap dung Clean Code principles va quan ly tren GitHub", "2025-07-14", "Cao");
        
        // Test case 9: Viet Test Case chuc nang (15/7)
        System.out.println("\n9. Viet Test Case chuc nang (hoan thanh 15/7):");
        manager.addNewTask("Viet Test Case chuc nang", "Viet Functional Test Cases cho ung dung", "2025-07-15", "Trung binh");
        
        // Test case 10: Them nhiem vu trung lap de kiem tra
        System.out.println("\n10. Test nhiem vu trung lap:");
        manager.addNewTask("Lap ke hoach & lich bieu 3 tuan", "Nhiem vu trung lap", "2025-07-02", "Cao");
        
        // Test case 11: Test validation loi
        System.out.println("\n11. Test validation loi - tieu de trong:");
        manager.addNewTask("", "Nhiem vu khong co tieu de", "2025-07-10", "Thap");
        
        System.out.println("\n12. Test validation loi - ngay khong hop le:");
        manager.addNewTask("Test ngay loi", "Nhiem vu voi ngay sai", "2025-13-40", "Trung binh");
        
        System.out.println("\n13. Test validation loi - uu tien khong hop le:");
        manager.addNewTask("Test uu tien loi", "Nhiem vu voi uu tien sai", "2025-07-08", "Rat cao");
        
        // Hien thi tat ca nhiem vu
        System.out.println("\n=== HIEN THI TAT CA NHIEM VU DA HOAN THANH ===");
        manager.displayAllTasks();
        
        System.out.println("\n=== TONG KET SPRINT ===");
        System.out.println("Sprint 1 (1/7 - 7/7): 4 nhiem vu da hoan thanh");
        System.out.println("Sprint 2 (8/7 - 15/7): 5 nhiem vu da hoan thanh");
        System.out.println("Tong cong: 9 nhiem vu da hoan thanh trong Product Backlog");
        
        System.out.println("\n=== KET THUC DEMO NHOM 9 PRODUCT BACKLOG ===");
    }
}
