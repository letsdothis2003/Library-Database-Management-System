package com.example.demo; // Make sure this matches your actual folder path!

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Backend {  

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/data/{tableName}")
    public List<Map<String, Object>> getTableData(@PathVariable String tableName) {
        String cleanName = tableName.replaceAll("[^a-zA-Z0-9_]", "");
        String sql = "SELECT * FROM " + cleanName;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchBooks(@RequestParam String query) {
        String sql = "SELECT * FROM Books WHERE Title LIKE ? OR Author LIKE ? OR ISBN LIKE ?";
        String pattern = "%" + query + "%";
        return jdbcTemplate.queryForList(sql, pattern, pattern, pattern);
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Library Backend is Online";
    }

} 
