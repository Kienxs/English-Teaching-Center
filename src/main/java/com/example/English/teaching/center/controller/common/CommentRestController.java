package com.example.English.teaching.center.controller.common;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.English.teaching.center.dto.CommentDTO;
import com.example.English.teaching.center.service.content.PostService;

@RestController
@RequestMapping("/api/comments")
public class CommentRestController {
    private final PostService postService;

    public CommentRestController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDTO>> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<CommentDTO> comments = postService.getCommentsByCursor(postId, lastId, limit);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestParam String postSlug,
                                        @RequestParam String content,
                                        Principal principal){

        if(principal == null) 
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để bình luận");

        if(content == null || content.trim().isEmpty())
            return ResponseEntity.badRequest().body("Nội dung không được để trống");

        try{
            postService.saveComment(postSlug, principal.getName(), content.trim());
            
            return ResponseEntity.ok("Bình luận thành công");
        }catch(Exception e){
            return ResponseEntity.status(500).body("Có lỗi xảy ra: " + e.getMessage());
        }

    }
}
