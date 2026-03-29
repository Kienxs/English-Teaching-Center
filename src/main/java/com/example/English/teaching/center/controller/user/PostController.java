package com.example.English.teaching.center.controller.user;

import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.English.teaching.center.dto.PostListDTO;
import com.example.English.teaching.center.entity.Post;
import com.example.English.teaching.center.service.content.PostService;

@Controller
@RequestMapping("/user")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/blog")
    public String blogPage(@RequestParam(value = "page", defaultValue = "1") int page,
                           Model model) {
        Page<PostListDTO> postPage = postService.getApprovedPostsByType(Post.PostType.BLOG, page);
        model.addAttribute("postPage", postPage);
        return "user/blog";
    }

    @GetMapping("/news")
    public String newsPage(@RequestParam(value = "page", defaultValue = "1") int page,
                           Model model) {
        Page<PostListDTO> postPage = postService.getApprovedPostsByType(Post.PostType.NEWS, page);
        model.addAttribute("postPage", postPage);
        return "user/news";
    }

    @GetMapping("/post/{slug}")
    public String postDetail(@PathVariable String slug, Model model) {
        Post post = postService.getPostBySlug(slug);
        model.addAttribute("post", post);

        List<PostListDTO> related = postService.getRelatedPosts(post.getType(), post.getId());
        model.addAttribute("relatedPosts", related);

        String activeTab = (post.getType() == Post.PostType.NEWS) ? "news" : "blog";
        model.addAttribute("activeTab", activeTab);

        return "user/post-detail";
    }

    @PostMapping("/post-comment")
    public String postComment(@RequestParam String postSlug,
                              @RequestParam String content,
                              Principal principal) {
        if(principal != null && content != null && !content.trim().isEmpty()){
            postService.saveComment(postSlug, principal.getName(), content.trim());
        }

        return "redirect:/user/post/" + postSlug;
    }
}
