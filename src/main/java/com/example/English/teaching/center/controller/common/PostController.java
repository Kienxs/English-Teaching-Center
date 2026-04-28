package com.example.English.teaching.center.controller.common;

import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.English.teaching.center.dto.content.PostListResponse;
import com.example.English.teaching.center.entity.Post;
import com.example.English.teaching.center.service.content.PostService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class PostController {
    private final PostService postService;

    @GetMapping("/blog")
    public String blogPage(@RequestParam(value = "page", defaultValue = "1") int page,
                           Model model) {
        Page<PostListResponse> postPage = postService.getApprovedPostsByType(Post.PostType.BLOG, page);
        model.addAttribute("postPage", postPage);
        return "user/blog";
    }

    @GetMapping("/news")
    public String newsPage(@RequestParam(value = "page", defaultValue = "1") int page,
                           Model model) {
        Page<PostListResponse> postPage = postService.getApprovedPostsByType(Post.PostType.NEWS, page);
        model.addAttribute("postPage", postPage);
        return "user/news";
    }

    @GetMapping("/post/{slug}")
    public String postDetail(@PathVariable String slug, 
                             Model model,
                             Principal principal) { 

        String email = principal.getName();

        postService.incrementViewCount(slug, email); 

        Post post = postService.getPostBySlug(slug);
        model.addAttribute("post", post);

        List<PostListResponse> related = postService.getRelatedPosts(post.getType(), post.getId());
        model.addAttribute("relatedPosts", related);

        String activeTab = (post.getType() == Post.PostType.NEWS) ? "news" : "blog";
        model.addAttribute("activeTab", activeTab);

        return "user/post-detail";
    }
}