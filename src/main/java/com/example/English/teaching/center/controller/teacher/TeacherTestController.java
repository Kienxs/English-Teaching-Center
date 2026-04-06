package com.example.English.teaching.center.controller.teacher;

import com.example.English.teaching.center.dto.course.QuestionSaveDTO;
import com.example.English.teaching.center.dto.course.TestSaveDTO;
import com.example.English.teaching.center.entity.Test;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.service.course.TestService;
import com.example.English.teaching.center.service.user.UserService;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher")
public class TeacherTestController {

    private final TestService testService;
    private final UserService userService;

    public TeacherTestController(TestService testService,
                                UserService userService) {
        this.testService = testService;
        this.userService = userService;
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
                             Principal principal,
                             RedirectAttributes ra){
        User teacher = userService.findByEmail(principal.getName());
        testService.deleteTest(id, teacher.getId());
        return "redirect:/teacher/course/edit/" + courseSlug;
    }

    @PostMapping("/question/save")
    public String saveQuestion(@ModelAttribute QuestionSaveDTO dto, Principal principal) {
        User teacher = userService.findByEmail(principal.getName());
        testService.saveQuestion(dto, teacher.getId()); 
        
        Test test = testService.findTestById(dto.getTestId());
        return "redirect:/teacher/test/edit/" + test.getSlug();
    }

    @PostMapping("/question/delete/{id}")
    public String deleteQuestion(@PathVariable Long id, Principal principal){
        User teacher = userService.findByEmail(principal.getName());
        Long testId = testService.deleteQuestionAndGetTestId(id, teacher.getId()); 
        Test test = testService.findTestById(testId);
        return "redirect:/teacher/test/edit/" + test.getSlug();
    }
}