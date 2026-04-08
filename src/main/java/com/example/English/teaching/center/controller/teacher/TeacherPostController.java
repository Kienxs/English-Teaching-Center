package com.example.English.teaching.center.controller.teacher;

import com.example.English.teaching.center.dto.content.EditPostRequest;
import com.example.English.teaching.center.dto.content.SectionRequest;
import com.example.English.teaching.center.entity.Post; 
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.service.content.PostService;
import com.example.English.teaching.center.service.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
public class TeacherPostController {

    private final UserService userService;
    private final PostService postService;

    public TeacherPostController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/post-management")
    public String postManagement(Model model, Principal principal,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size){

        User teacher = userService.findByEmail(principal.getName());
        Page<Post> postPage = postService.getPostsByAuthorId(teacher.getId(), page, size);
        
        model.addAttribute("postPage", postPage); 
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("teacherName", teacher.getFullName());
        
        return "teacher/post-management"; 
    }

    @GetMapping("/post/create")
    public String showCreatePostForm(Model model){
        model.addAttribute("post", new EditPostRequest());
        model.addAttribute("pageTitle", "Tạo bài viết mới");
        return "teacher/post-edit";
    }

    @GetMapping("/post/edit/{slug}")
    public String showEditPostForm(@PathVariable("slug") String slug, Model model) {
        Post existingPost = postService.findPostBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        EditPostRequest dto = new EditPostRequest();
        dto.setId(existingPost.getId());
        dto.setTitle(existingPost.getTitle());
        dto.setSummary(existingPost.getSummary());
        dto.setThumbnailUrl(existingPost.getThumbnailUrl());
        dto.setSlug(existingPost.getSlug());
        dto.setStatus(existingPost.getStatus()); 

        if (existingPost.getSections() != null) {
            existingPost.getSections().forEach(entitySection -> {
                SectionRequest secDto = new SectionRequest();
                secDto.setId(entitySection.getId());
                secDto.setSectionTitle(entitySection.getSectionTitle());
                secDto.setSectionContent(entitySection.getSectionContent());
                secDto.setImageUrl(entitySection.getImageUrl());
                dto.getSections().add(secDto);
            });
        }

        model.addAttribute("post", dto);
        model.addAttribute("pageTitle", "Chỉnh sửa: " + existingPost.getTitle());
        return "teacher/post-edit";
    }

    @PostMapping("/post/save")
    public String savePost(@ModelAttribute("post") EditPostRequest postDto,
                            BindingResult bindingResult,
                           Principal principal, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
            return postDto.getId() != null 
                   ? "redirect:/teacher/post/edit/" + postDto.getSlug() 
                   : "redirect:/teacher/post/create";
        }
        
        User currentUser = userService.findByEmail(principal.getName());
        try{
            Post saved = postService.saveOrUpdateFromDTO(postDto, currentUser.getId());
            ra.addFlashAttribute("successMessage", "Bài viết đã được lưu thành công!");
            return "redirect:/teacher/post/edit/" + saved.getSlug();
        } catch(Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi lưu: " + e.getMessage());
            return "redirect:/teacher/post-management";
        }
    }

    @PostMapping("/api/post/auto-save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autoSaveDraft(
                                      @ModelAttribute EditPostRequest postDto,
                                      Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        try{
            Post savedDraft = postService.saveOrUpdateFromDTO(postDto, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedDraft.getId());
            response.put("slug", savedDraft.getSlug());

            return ResponseEntity.ok(response);
        }catch(Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi lưu tự động: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }

    }

    @PostMapping("/post/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, 
                            RedirectAttributes ra,
                            Principal principal) {
        try {
            User currentUser = userService.findByEmail(principal.getName());

            postService.deleteDraftPost(id, currentUser.getId());
            ra.addFlashAttribute("successMessage", "Đã xóa bài viết thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/post-management";
    }
}