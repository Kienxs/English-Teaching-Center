package com.example.English.teaching.center.service.user;

import java.time.Year;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.English.teaching.center.dto.MonthlyRevenueDTO;
import com.example.English.teaching.center.entity.BlogPost;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.repository.BlogPostRepository;
import com.example.English.teaching.center.repository.CourseRepository;

@Service
public class AdminService {
    private final CourseRepository courseRepository;
    private final BlogPostRepository blogPostRepository;

    public AdminService(CourseRepository courseRepository,
                        BlogPostRepository blogPostRepository) {
        this.courseRepository = courseRepository;
        this.blogPostRepository = blogPostRepository;
    }

// --------------------------------- BLOG MANAGER ---------------------------------------
    public Page<BlogPost> getPendingBlogs(int page, int size){
        Pageable pageable =  PageRequest.of(page, size, Sort.by("createdAt").descending());
        return blogPostRepository.findByStatus(BlogPost.Status.PENDING, pageable);
    }

    @Transactional
    public void approvePost(Long postId){
        BlogPost post = blogPostRepository.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));
        post.setStatus(BlogPost.Status.APPROVED);
        blogPostRepository.save(post);
    }

    @Transactional
    public void rejectPost(Long postId, String note){
        BlogPost post = blogPostRepository.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));
        post.setStatus(BlogPost.Status.REJECTED);
        post.setAdminNote(note);
        blogPostRepository.save(post);
    }

// --------------------------------- COURSE MANAGER ---------------------------------------

    public Page<Course> getPendingCourse(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseRepository.findByStatus(Course.Status.PENDING, pageable);
    }

    @Transactional
    public void approveCourse(Long courseId){
        Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        course.setStatus(Course.Status.APPROVED);
        courseRepository.save(course);
    }

    @Transactional
    public void rejectCourse(Long courseId, String note){
        Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        course.setStatus(Course.Status.REJECTED);
        course.setAdminNote(note);
        courseRepository.save(course);
    }

// --------------------------------- REPORTS & ANALYTICS ---------------------------------------
    public List<MonthlyRevenueDTO> getChartData(Integer year){
        // Nếu không truyền năm, mặc định lấy năm nay
        int targetYear = (year != null) ? year : Year.now().getValue();

        // 1. Lấy dữ liệu thô từ data
        List<MonthlyRevenueDTO> rawData = courseRepository.getRevenueByYear(targetYear);

        return rawData;
    }
}
