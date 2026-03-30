package com.example.English.teaching.center.service.content;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import com.example.English.teaching.center.dto.CommentDTO;
import com.example.English.teaching.center.dto.PostListDTO;
import com.example.English.teaching.center.entity.Comment;
import com.example.English.teaching.center.entity.Post;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.mapper.CommentMapper;
import com.example.English.teaching.center.mapper.PostMapper;
import com.example.English.teaching.center.repository.CommentRepository;
import com.example.English.teaching.center.repository.PostRepository;
import com.example.English.teaching.center.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PostService {
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public PostService(PostMapper postMapper, 
                        PostRepository postRepository,
                        CommentRepository commentRepository,
                        UserRepository userRepository,
                        CommentMapper commentMapper) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public Page<PostListDTO> getApprovedPostsByType(Post.PostType type, int pageNo) {
        int pageSize = 6;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        Page<Post> postPage = postRepository.findByTypeAndStatusOrderByCreatedAtDesc(type, Post.PostStatus.APPROVED, pageable);

        return postPage.map(postMapper::toListDTO);
    }

    public Post getPostBySlug(String slug) {
        return postRepository.findBySlugAndStatus(slug, Post.PostStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));
    }

    @Transactional
    public void saveComment(String postSlug,
                            String email,
                            String content){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        Post post = postRepository.findBySlugAndStatus(postSlug, Post.PostStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);
        comment.setStatus(Comment.Status.APPROVED);
        commentRepository.save(comment);
    }

    public List<PostListDTO> getRelatedPosts(Post.PostType type, Long currentId) {
        Pageable topFive = PageRequest.of(0, 5); 
        List<Post> posts = postRepository.findRelatedPosts(type, currentId, topFive);
        
        return posts.stream()
                    .map(postMapper::toListDTO) 
                    .toList();
    }

    public List<CommentDTO> getCommentsByCursor(Long postId, Long lastId, int limit){
        Pageable pageable = PageRequest.of(0, limit);
        List<Comment> comments = commentRepository.findCommentsByCursor(postId, lastId, pageable);

        return comments.stream()
                        .map(commentMapper::toDTO)
                        .toList();
    }

    @Transactional
    public void incrementViewCount(String slug) {
        postRepository.incrementViewCount(slug);
    }
}
