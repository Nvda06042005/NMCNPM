import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManager {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // =================== Helpers ===================
    private JSONArray loadTasksFromDb() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            return (JSONArray) obj;
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
        }
        return new JSONArray();
    }

    private void saveTasksToDb(JSONArray tasksData) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasksData.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
        }
    }

    private boolean isValidPriority(String priorityLevel) {
        return priorityLevel.equals("Thấp") || priorityLevel.equals("Trung bình") || priorityLevel.equals("Cao");
    }

    private boolean isDuplicateTask(JSONArray tasks, String title, LocalDate dueDate) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("title").toString().equalsIgnoreCase(title)
                    && task.get("due_date").toString().equals(dueDate.format(DATE_FORMATTER))) {
                return true;
            }
        }
        return false;
    }

    private JSONObject createTask(String title, String description, LocalDate dueDate, String priorityLevel) {
        String taskId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        JSONObject task = new JSONObject();
        task.put("id", taskId);
        task.put("title", title);
        task.put("description", description);
        task.put("due_date", dueDate.format(DATE_FORMATTER));
        task.put("priority", priorityLevel);
        task.put("status", "Chưa hoàn thành");
        task.put("created_at", now.format(DateTimeFormatter.ISO_DATE_TIME));
        task.put("last_updated_at", now.format(DateTimeFormatter.ISO_DATE_TIME));
        return task;
    }

    // =================== Public Interface ===================
    public JSONObject addNewTask(String title, String description, String dueDateStr, String priorityLevel) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return null;
        }

        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Lỗi: Ngày đến hạn không được để trống.");
            return null;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Định dạng ngày không hợp lệ. Định dạng đúng: YYYY-MM-DD.");
            return null;
        }

        if (!isValidPriority(priorityLevel)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ. Chọn: Thấp, Trung bình, Cao.");
            return null;
        }

        JSONArray tasks = loadTasksFromDb();

        if (isDuplicateTask(tasks, title, dueDate)) {
            System.out.println("Lỗi: Nhiệm vụ đã tồn tại với tiêu đề và ngày đến hạn giống nhau.");
            return null;
        }

        JSONObject newTask = createTask(title, description, dueDate, priorityLevel);
        tasks.add(newTask);
        saveTasksToDb(tasks);

        System.out.println("✅ Đã thêm nhiệm vụ mới thành công với ID: " + newTask.get("id"));
        return newTask;
    }

    // =================== Main (Test) ===================
    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();

        System.out.println("\n➤ Thêm nhiệm vụ hợp lệ:");
        manager.addNewTask("Mua sách", "Sách Công nghệ phần mềm", "2025-07-20", "Cao");

        System.out.println("\n➤ Thêm nhiệm vụ trùng lặp:");
        manager.addNewTask("Mua sách", "Sách Công nghệ phần mềm", "2025-07-20", "Cao");

        System.out.println("\n➤ Thêm nhiệm vụ với tiêu đề rỗng:");
        manager.addNewTask("", "Không có tiêu đề", "2025-07-21", "Trung bình");

        System.out.println("\n➤ Thêm nhiệm vụ với ngày sai định dạng:");
        manager.addNewTask("Gặp khách hàng", "Trao đổi dự án", "2025/07/22", "Cao");

        System.out.println("\n➤ Thêm nhiệm vụ với mức độ ưu tiên không hợp lệ:");
        manager.addNewTask("Chạy bộ", "5km mỗi sáng", "2025-07-23", "Rất cao");
    }
}
