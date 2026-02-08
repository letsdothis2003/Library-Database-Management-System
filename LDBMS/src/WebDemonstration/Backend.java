import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

/**
 * We wanted to create a web version just to demonstrate our application 
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows your GitHub Pages site to talk to our backend
public class LibraryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //To view each table
    @GetMapping("/data/{tableName}")
    public List<Map<String, Object>> getTableData(@PathVariable String tableName) {
        // Sanitize table name to prevent any messiness or overlapping information in SQL
        String sql = "SELECT * FROM " + tableName.replaceAll("[^a-zA-Z0-9_]", "");
        return jdbcTemplate.queryForList(sql);
    }

    // Search Logic
    @GetMapping("/search")
    public List<Map<String, Object>> searchBooks(@RequestParam String query) {
        String sql = "SELECT * FROM Books WHERE Title LIKE ? OR Author LIKE ? OR ISBN LIKE ?";
        String searchPattern = "%" + query + "%";
        return jdbcTemplate.queryForList(sql, searchPattern, searchPattern, searchPattern);
    }

    // Logic for checking overdue books 
    @GetMapping("/overdue")
    public List<Map<String, Object>> getOverdue() {
        String sql = "SELECT * FROM CheckOut WHERE ReturnDeadline < CURDATE()";
        return jdbcTemplate.queryForList(sql);
    }
}
