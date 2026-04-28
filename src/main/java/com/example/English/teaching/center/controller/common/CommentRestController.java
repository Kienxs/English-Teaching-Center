package com.example.English.teaching.center.controller.common;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.English.teaching.center.dto.content.CommentRequest;
import com.example.English.teaching.center.dto.content.CommentResponse;
import com.example.English.teaching.center.service.content.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentRestController {
    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID postId,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<CommentResponse> comments = postService.getCommentsByCursor(postId, lastCreatedAt, limit);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addComment(Principal principal,
                @ModelAttribute CommentRequest requestDTO){

        if(principal == null) 
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để bình luận");

        String cleanContent = requestDTO.getContent();

        if( cleanContent == null || cleanContent.trim().isEmpty())
            return ResponseEntity.badRequest().body("Nội dung không được để trống");

        try{
            postService.saveComment(requestDTO, principal.getName());
            
            return ResponseEntity.ok("Bình luận thành công");
        }catch(Exception e){
            return ResponseEntity.status(500).body("Có lỗi xảy ra: " + e.getMessage());
        }
    }
}