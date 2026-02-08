package WebDemonstration;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

/**
 *We wanted to create a proper web demonstration to showcase this project in a near and concise way. We used 2 apis, railway and springboot
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") / 

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Select and view tables 
    @GetMapping("/data/{tableName}")
    public List<Map<String, Object>> getTableData(@PathVariable String tableName) {
        // This is to allow alphanumeric and underscores for table names
        String cleanTableName = tableName.replaceAll("[^a-zA-Z0-9_]", "");
        String sql = "SELECT * FROM " + cleanTableName;
        return jdbcTemplate.queryForList(sql);
    }

    //Search logic 
    @GetMapping("/search")
    public List<Map<String, Object>> searchBooks(@RequestParam String query) {
        String sql = "SELECT * FROM Books WHERE Title LIKE ? OR Author LIKE ? OR ISBN LIKE ?";
        String searchPattern = "%" + query + "%";
        return jdbcTemplate.queryForList(sql, searchPattern, searchPattern, searchPattern);
    }

    // logic for Overdue Books 
    @GetMapping("/overdue")
    public List<Map<String, Object>> getOverdue() {
        String sql = "SELECT * FROM CheckOut WHERE ReturnDeadline < CURDATE()";
        return jdbcTemplate.queryForList(sql);
    }

    // Simple health check to verify the backend is running
    @GetMapping("/status")
    public String getStatus() {
        return "Library Backend is Online and DevOps-ready!";
    }
}
