package com.example.English.teaching.center.controller.common;

import org.springframework.ui.Model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.English.teaching.center.dto.PostListDTO;
import com.example.English.teaching.center.entity.Post;
import com.example.English.teaching.center.service.content.PostService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public String postDetail(@PathVariable String slug, 
                             Model model,
                             HttpServletRequest request, 
                             HttpServletResponse response) { 
        boolean hasViewed = false;
        Cookie[] cookies = request.getCookies();
        String cookieName = "viewed_post_" + slug;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    hasViewed = true;
                    break;
                }
            }
        }

        if (!hasViewed) {
            postService.incrementViewCount(slug); 

            Cookie viewCookie = new Cookie(cookieName, "true");
            viewCookie.setMaxAge(24 * 60 * 60); //  24 giờ 
            viewCookie.setPath("/"); 
            viewCookie.setHttpOnly(true); 
            
            response.addCookie(viewCookie); 
        }

        Post post = postService.getPostBySlug(slug);
        model.addAttribute("post", post);

        List<PostListDTO> related = postService.getRelatedPosts(post.getType(), post.getId());
        model.addAttribute("relatedPosts", related);

        String activeTab = (post.getType() == Post.PostType.NEWS) ? "news" : "blog";
        model.addAttribute("activeTab", activeTab);

        return "user/post-detail";
    }
}