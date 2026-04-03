package com.example.English.teaching.center.service.user;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.dto.TeacherDashboardDTO;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.StudentCourseRepository;
import com.example.English.teaching.center.repository.UserRepository;

@Service
public class TeacherService {
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final UserRepository userRepository;

    public TeacherService(CourseRepository courseRepository,
                          StudentCourseRepository studentCourseRepository,
                          UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.userRepository = userRepository;
    }

    public TeacherDashboardDTO getDashboardData(String email){
        User teacher = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Long teacherId = teacher.getId();   

        TeacherDashboardDTO dto = new TeacherDashboardDTO();

        // 1.Logic frocess KPI & Null check
        Long totalViews = courseRepository.sumViewByTEacherId(teacherId);
        dto.setTotalViews(totalViews != null ? totalViews : 0L);

        Long totalCourses = courseRepository.countByTeacherId(teacherId);
        dto.setTotalCourses(totalCourses != null ? totalCourses : 0L);

        Long totalStudents = studentCourseRepository.countTotalStudentByTeacher(teacherId);
        dto.setTotalStudents(totalStudents);

        BigDecimal totalRevenue = studentCourseRepository.calculateTotalRevenueByTeacherId(teacherId);
        dto.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 2. Get the list
        dto.setTopCourses(courseRepository.findTopSellingCourses(teacherId));
        dto.setRecentEnrollments(studentCourseRepository.findRecentErollments(teacherId));

        // 3. Logic for processing chart data (Converting raw data into a List for FE)
        List<Object[]> categoryStats = studentCourseRepository.countSalesByCategory(teacherId);
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        if(categoryStats != null){
            for(Object[] row : categoryStats){
                String label = row[0] != null ? row[0].toString() : "Unknown";
                Long count = row[1] != null ? (Long) row[1] : 0L;
                labels.add(label);
                data.add(count);
            }
        }

        dto.setChartLabels(labels);
        dto.setChartData(data);

        return dto;
    }

    public Map<String, Object> getRevenueChartData(String email, String range) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giảng viên"));

        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        List<Object[]> rawData = new ArrayList<>();

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:00");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

        switch (range) {
            case "1D":
                startDate = now.minusHours(23).withMinute(0).withSecond(0).withNano(0);
                for (int i = 0; i < 24; i++) {
                    String label = startDate.plusHours(i).format(hourFormatter);
                    revenueMap.put(label, BigDecimal.ZERO);
                }
                rawData = studentCourseRepository.getRevenueByHour(teacher.getId(), startDate);
                break;

            case "1W":
                startDate = now.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
                for (int i = 0; i <= 6; i++) {
                    String label = startDate.plusDays(i).format(dayFormatter);
                    revenueMap.put(label, BigDecimal.ZERO);
                }
                rawData = studentCourseRepository.getRevenueByDay(teacher.getId(), startDate);
                break;

            case "1M":
                startDate = now.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
                for (int i = 0; i <= 29; i++) {
                    String label = startDate.plusDays(i).format(dayFormatter);
                    revenueMap.put(label, BigDecimal.ZERO);
                }
                rawData = studentCourseRepository.getRevenueByDay(teacher.getId(), startDate);
                break;

            case "3M":
                startDate = now.minusDays(89).withHour(0).withMinute(0).withSecond(0).withNano(0);
                for (int i = 0; i <= 89; i++) {
                    String label = startDate.plusDays(i).format(dayFormatter);
                    revenueMap.put(label, BigDecimal.ZERO);
                }
                rawData = studentCourseRepository.getRevenueByDay(teacher.getId(), startDate);
                break;

            case "1Y":
                LocalDate firstMonth = now.toLocalDate().minusMonths(11).withDayOfMonth(1);
                startDate = firstMonth.atStartOfDay();
                for (int i = 0; i < 12; i++) {
                    String label = firstMonth.plusMonths(i).format(monthFormatter);
                    revenueMap.put(label, BigDecimal.ZERO);
                }
                rawData = studentCourseRepository.getRevenueByMonth(teacher.getId(), startDate);
                break;

            default:
                throw new IllegalArgumentException("Phạm vi không hợp lệ");
        }

        for (Object[] row : rawData) {
            String formattedLabel = "";
            BigDecimal amount = (BigDecimal) row[1];

            try {
                if (range.equals("1D")) {
                    int hour = ((Number) row[0]).intValue();
                    formattedLabel = String.format("%02d:00", hour);
                    
                } else if (range.equals("1Y")) {
                    if (row[0] instanceof Number) {
                        int month = ((Number) row[0]).intValue();
                        String monthPrefix = String.format("%02d/", month);

                        for (String key : revenueMap.keySet()) {
                            if (key.startsWith(monthPrefix)) {
                                formattedLabel = key;
                                break;
                            }
                        }
                    } else {
                        String dbMonthStr = row[0].toString();
                        if (dbMonthStr.contains("-")) {
                            String[] parts = dbMonthStr.split("-");
                            formattedLabel = parts[1] + "/" + parts[0]; 
                        }
                    }
                    
                } else {
                    String dbDateString = row[0].toString(); 
                    LocalDate dbDate = LocalDate.parse(dbDateString); 
                    formattedLabel = dbDate.format(dayFormatter);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Lỗi Parse thời gian từ SQL: " + row[0]);
                continue;
            }

            if (revenueMap.containsKey(formattedLabel)) {
                revenueMap.put(formattedLabel, amount);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(revenueMap.keySet()));
        result.put("data", new ArrayList<>(revenueMap.values()));

        return result;
    }
}
