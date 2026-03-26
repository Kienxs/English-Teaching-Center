package com.example.English.teaching.center.controller;

import com.example.English.teaching.center.dto.CourseDTO;
import com.example.English.teaching.center.dto.CourseSaveDTO;
import com.example.English.teaching.center.dto.LessonSaveDTO;
import com.example.English.teaching.center.dto.MaterialDTO;
import com.example.English.teaching.center.dto.QuestionSaveDTO;
import com.example.English.teaching.center.dto.TeacherDashboardDTO;
import com.example.English.teaching.center.dto.TestSaveDTO;
import com.example.English.teaching.center.entity.BlogPost;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.Test;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.service.*;
import com.example.English.teaching.center.service.course.BlogPostService;
import com.example.English.teaching.center.service.course.CourseService;
import com.example.English.teaching.center.service.course.LessonService;
import com.example.English.teaching.center.service.course.TestService;
import com.example.English.teaching.center.service.user.TeacherService;
import com.example.English.teaching.center.service.user.UserService;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/teacher")
public class TeacherController {
    private final CourseService courseService;
    private final UserService userService;
    private final LessonService lessonService;
    private final TestService testService;
    private final BlogPostService blogPostService;
    private final TeacherService teacherService;

    public TeacherController(CourseService courseService,
                             UserService userService,
                             LessonService lessonService,
                             TestService testService,
                             BlogPostService blogPostService,
                             TeacherService teacherService) {
        this.courseService = courseService;
        this.userService = userService;
        this.lessonService = lessonService;
        this.testService = testService;
        this.blogPostService = blogPostService;
        this.teacherService = teacherService;
    }

// Process flow for teacher dashboard ----------------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal){
        if(principal == null) return "redirect:/login";

        TeacherDashboardDTO dashboardData = teacherService.getDashboardData(principal.getName());

        model.addAttribute("totalViews", dashboardData.getTotalViews());
        model.addAttribute("totalCourses", dashboardData.getTotalCourses());
        model.addAttribute("totalStudents", dashboardData.getTotalStudents());
        model.addAttribute("totalRevenue", dashboardData.getTotalRevenue());

        model.addAttribute("topCourses", dashboardData.getTopCourses());
        model.addAttribute("recentEnrollments", dashboardData.getRecentEnrollments());

        model.addAttribute("catLabels", dashboardData.getChartLabels());
        model.addAttribute("catData", dashboardData.getChartData());

        return "teacher/dashboard";
    }

// Process flow for course administrators --------------------------------------------------------------------
    @GetMapping("/course-management")
    public String courseManagement(Model model, Principal principal,
                                   @RequestParam(defaultValue = "0") int page, 
                                   @RequestParam(defaultValue = "5") int size) { 
        if(principal == null) return "redirect:/login";

        User teacher = userService.findByEmail(principal.getName());
        Page<CourseDTO> coursePage = courseService.getCoursesByTeacher(teacher.getId(), page, size);
        
        model.addAttribute("coursePage", coursePage); 
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("teacherName", teacher.getFullName());
        
        return "teacher/course-management";
    }

    @GetMapping("/course/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", courseService.createNewDraftCourse()); 
        model.addAttribute("pageTitle", "Tạo khóa học mới");
        return "teacher/course-edit"; 
    }

    @GetMapping("/course/edit/{slug}")
    public String showEditForm(@PathVariable("slug") String slug, Model model) {
        Course existingCourse = courseService.findBySlug(slug).orElse(null);
        
        if (existingCourse == null) return "redirect:/teacher/course-management";

        model.addAttribute("course", existingCourse); 
        model.addAttribute("pageTitle", "Chỉnh sửa: " + existingCourse.getName());
        return "teacher/course-edit"; 
    }

    @GetMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id, RedirectAttributes ra) {
        try{
            courseService.deleteDraftCourse(id);
            ra.addFlashAttribute("successMessage", "Đã xóa khóa học thành công!");
        }catch (Exception e){
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/teacher/course-management";
    }

    @PostMapping("/course/save")
    public String saveCourse(@ModelAttribute("course") CourseSaveDTO dto, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        Course savedCourse = courseService.saveOrUpdateCourse(dto, currentUser.getId());
        return "redirect:/teacher/course/edit/" + savedCourse.getSlug();
    }

    @PostMapping("/lesson/save")
    public String saveLesson(@ModelAttribute("LessonSaveDTO") LessonSaveDTO dto){
        lessonService.saveOrUpdateLesson(dto);

        String slug = courseService.findCourseById(dto.getCourseId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"))
                        .getSlug();

        return "redirect:/teacher/course/edit/" + slug;
    }

    @PostMapping("/lesson/delete/{lessonId}/{courseSlug}")
    public String deleteLesson(@PathVariable ("lessonId") Long lessonId,
                               @PathVariable ("courseSlug") String courseSlug){
        lessonService.deleteLesson(lessonId);
        return "redirect:/teacher/course/edit/" + courseSlug;
    }

    @PostMapping("/material/save")
    public String saveMaterial(@ModelAttribute MaterialDTO dto){
        Long courseId = lessonService.saveOrUpdateMaterial(dto);

        String slug = courseService.findCourseById(courseId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"))
                    .getSlug();

        return "redirect:/teacher/course/edit/" + slug;
    }

    @PostMapping("/material/delete/{id}/{courseSlug}")
    public String deleteMaterial(@PathVariable Long id, 
                                @PathVariable String courseSlug){
        lessonService.deleteMaterial(id);

        return "redirect:/teacher/course/edit/" + courseSlug;
    }

    @PostMapping("/test/save")
    public String saveTest(@ModelAttribute TestSaveDTO dto){ 
        String slug = testService.saveTest(dto);
        return "redirect:/teacher/course/edit/" + slug;
    }

    @GetMapping("/test/edit/{testSlug}")
    public String showTestQuestions(@PathVariable String testSlug, Model model){
        Test test = testService.findTestByIdentifier(testSlug);
        model.addAttribute("test", test);
        model.addAttribute("questions", test.getQuestions());
        return "teacher/test-questions";
    }

    @PostMapping("/test/delete/{id}/{courseSlug}")
    public String deleteTest(@PathVariable("id") Long id, 
                             @PathVariable("courseSlug") String courseSlug, 
                             RedirectAttributes ra){
        testService.deleteTest(id);
        return "redirect:/teacher/course/edit/" + courseSlug;
    }

    @PostMapping("/question/save")
    public String saveQuestion(@ModelAttribute QuestionSaveDTO dto) {
        testService.saveQuestion(dto);
        
        Test test = testService.findTestById(dto.getTestId());
        return "redirect:/teacher/test/edit/" + test.getSlug();
    }

    @GetMapping("/question/delete/{id}")
    public String deleteQuestion(@PathVariable Long id){
        Long testId = testService.deleteQuestionAndGetTestId(id);
        
        Test test = testService.findTestById(testId);
        return "redirect:/teacher/test/edit/" + test.getSlug();
    }

// Process flow for teachers managing blog posts ---------------------------------------------------------------------------------------------------------------
    @GetMapping("/blog-management")
    public String blogManagement(Model model, Principal principal,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size
    ){
        if(principal == null) return "redirect:/login";

        User teacher = userService.findByEmail(principal.getName());
        
        // Gọi Service lấy Page
        Page<BlogPost> blogPage = blogPostService.getBlogPostsByAuthor_Id(teacher.getId(), page, size);
        
        // Truyền dữ liệu sang View
        model.addAttribute("blogPage", blogPage); // Đối tượng Page chứa list bài viết + thông tin phân trang
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("teacherName", teacher.getFullName());
        
        return "teacher/blog-management";
    }

    @GetMapping("/blogPost/create")
    public String showCreateBlogForm(Model model){
        model.addAttribute("blogPost", blogPostService.createNewDraftBlogPost());
        model.addAttribute("pageTitle", "Tạo bài post mới");
        return "teacher/blogpost-edit";
    }

    @GetMapping("/blogPost/edit/{slug}")
    public String showEditBlogForm(@PathVariable("slug") String slug, Model model) {
        BlogPost existingBlogPost = blogPostService.findBlogPostBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết trên Blog"));

        model.addAttribute("blogPost", existingBlogPost);
        model.addAttribute("pageTitle", "Chỉnh sửa: " + existingBlogPost.getTitle());
        return "teacher/blogpost-edit";
    }

    @PostMapping("/blogPost/save")
    public String saveBlogPost(@ModelAttribute("blogPost") BlogPost blogPost,
                               Principal principal, RedirectAttributes ra) {
        User currentUser = userService.findByEmail(principal.getName());
        try{
            BlogPost saved = blogPostService.saveOrUpdateBlogPost(blogPost, currentUser.getId());
            ra.addFlashAttribute("successMessage", "Bài viết đã được lưu thành công!");
            return "redirect:/teacher/blogPost/edit/" + saved.getSlug();
        } catch(Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi lưu: " + e.getMessage());
            return "redirect:/teacher/blog-management";
        }
    }

    @GetMapping("/blogPost/delete/{id}")
    public String deleteBlogPost(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            blogPostService.deleteDraftBlogPost(id);
            ra.addFlashAttribute("successMessage", "Đã xóa bài post thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/blog-management";
    }
}