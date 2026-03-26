package com.example.English.teaching.center.controller;

import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.StudentCourseRepository;
import com.example.English.teaching.center.service.user.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher/api")
public class TeacherRestController {

    private final UserService userService;
    private final StudentCourseRepository studentCourseRepository;

    public TeacherRestController(UserService userService,
                 StudentCourseRepository studentCourseRepository) {
        this.userService = userService;
        this.studentCourseRepository = studentCourseRepository;
    }

    @GetMapping("/revenue-chart")
    public Map<String, Object> getRevenueChartData(@RequestParam String range, Principal principal) {
        User teacher = userService.findByEmail(principal.getName());
        LocalDateTime startDate = LocalDateTime.now();
        List<Object[]> rawData;
        String formatPattern; // Định dạng hiển thị

        // Xử lý logic chọn Query và Format dựa trên Range
        switch (range) {
            case "1D": // 24 Giờ
                startDate = startDate.minusDays(1);
                rawData = studentCourseRepository.getRevenueByHour(teacher.getId(), startDate);
                formatPattern = "HOURS"; // Đánh dấu để xử lý riêng
                break;
                
            case "1Y": // 1 Năm
                startDate = startDate.minusYears(1);
                rawData = studentCourseRepository.getRevenueByMonth(teacher.getId(), startDate);
                formatPattern = "MM/yyyy";
                break;
                
            case "1W": // 1 Tuần
                startDate = startDate.minusWeeks(1);
                rawData = studentCourseRepository.getRevenueByDay(teacher.getId(), startDate);
                formatPattern = "dd/MM";
                break;
                
            case "3M": // 3 Tháng
                startDate = startDate.minusMonths(3);
                rawData = studentCourseRepository.getRevenueByDay(teacher.getId(), startDate);
                formatPattern = "dd/MM";
                break;
                
            case "1M": default: // 1 Tháng
                startDate = startDate.minusMonths(1);
                rawData = studentCourseRepository.getRevenueByDay(teacher.getId(), startDate);
                formatPattern = "dd/MM";
                break;
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        // Xử lý dữ liệu trả về
        for (Object[] row : rawData) {
            String label;
            BigDecimal amount = (BigDecimal) row[1];

            if ("HOURS".equals(formatPattern)) {
                // Nếu là giờ, SQL trả về số nguyên (ví dụ: 14, 15) -> Format thành "14:00"
                int hour = ((Number) row[0]).intValue(); 
                label = String.format("%02d:00", hour);
            } else {
                // Nếu là Ngày hoặc Tháng, SQL trả về String hoặc Date
                String rawDateStr = row[0].toString(); 
                // Xử lý parse date tùy theo format mong muốn (Code đơn giản hóa: lấy chuỗi SQL trả về)
                // Để đẹp hơn, bạn có thể Parse sang LocalDate rồi format lại theo formatPattern
                label = rawDateStr; 
            }

            labels.add(label);
            data.add(amount);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        
        return result;
    }
}