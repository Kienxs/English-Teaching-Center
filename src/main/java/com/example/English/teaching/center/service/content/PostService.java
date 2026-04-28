package com.example.English.teaching.center.service.content;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import com.example.English.teaching.center.dto.content.CommentRequest;
import com.example.English.teaching.center.dto.content.CommentResponse;
import com.example.English.teaching.center.dto.content.EditPostRequest;
import com.example.English.teaching.center.dto.content.PostListResponse;
import com.example.English.teaching.center.dto.content.SectionRequest;
import com.example.English.teaching.center.entity.Comment;
import com.example.English.teaching.center.entity.Post;
import com.example.English.teaching.center.entity.PostSection;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.CommentMapper;
import com.example.English.teaching.center.mapper.PostMapper;
import com.example.English.teaching.center.repository.CommentRepository;
import com.example.English.teaching.center.repository.PostRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.infra.RateLimitingService;
import com.example.English.teaching.center.utils.SlugUtils;

import io.github.bucket4j.Bucket;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RateLimitingService rateLimitingService;

// FUNCTIONS FOR STUDENT --------------------------------------------------------

    public Page<PostListResponse> getApprovedPostsByType(Post.PostType type, int pageNo) {
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
    public void saveComment(CommentRequest requestDTO, String email){
        String limitKey = "BLOG_COMMENT_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 5, 1);
        if(!bucket.tryConsume(1))
            throw new RateLimitException("Bạn bình luận quá nhanh! Vui lòng đợi 1 phút.");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        Post post = postRepository.findBySlugAndStatus(requestDTO.getPostSlug(), Post.PostStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        Comment comment = new Comment();
        comment.setContent(requestDTO.getContent());
        comment.setUser(user);
        comment.setPost(post);
        comment.setStatus(Comment.Status.APPROVED);
        commentRepository.save(comment);
    }

    public List<PostListResponse> getRelatedPosts(Post.PostType type, UUID currentId) {
        Pageable topFive = PageRequest.of(0, 5); 
        List<Post> posts = postRepository.findRelatedPosts(type, currentId, topFive);
        
        return posts.stream()
                    .map(postMapper::toListDTO) 
                    .toList();
    }

    public List<CommentResponse> getCommentsByCursor(UUID postId, LocalDateTime lastCreatedAt, int limit){
        Pageable pageable = PageRequest.of(0, limit);
        List<Comment> comments = commentRepository.findCommentsByCursor(postId, lastCreatedAt, pageable);

        return comments.stream()
                        .map(commentMapper::toDTO)
                        .toList();
    }

    @Transactional
    public void incrementViewCount(String slug, String email) {
        String limitKey = "VIEW_POST_" + slug + "_USER_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 1, 30);
        
        if(bucket.tryConsume(1)) 
            postRepository.incrementViewCount(slug);
    }

// FUNCTIONS FOR TEACHERS -----------------------------------------------

    public Page<Post> getPostsByAuthorId(UUID authorId, int pageNo, int pageSize){
        Pageable pageable = PageRequest.of(pageNo, pageSize, org.springframework.data.domain.Sort.by("createdAt").descending());
        return postRepository.findByAuthorIdAndIsDeletedFalse(authorId, pageable);
    }

    public java.util.Optional<Post> findPostBySlug(String slug){
        return postRepository.findBySlugAndIsDeletedFalse(slug);
    }

    public Post createNewDraftPost(){
        Post post = new Post();
        post.setStatus(Post.PostStatus.DRAFT);
        post.setType(Post.PostType.BLOG);
        return post;
    }

    @Transactional
    public Post saveOrUpdateFromDTO(EditPostRequest dto, UUID currentUserId) {
        String limitKey = "SAVE_POST_" + currentUserId ;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 10, 1);
        if(!bucket.tryConsume(1))
            throw new RateLimitException("Hệ thống đang bận, vui lòng lưu chậm lại!");

        Post post;
        UUID currentId = (dto.getId() != null) ? dto.getId() : UUID.randomUUID();

        // 1. KIỂM TRA LÀ THÊM MỚI (INSERT) HAY CẬP NHẬT (UPDATE)
        if (dto.getId() != null) {
            post = postRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết với ID: " + dto.getId()));

            if (!post.getAuthor().getId().equals(currentUserId)) {
                throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này!");
            }
        } else {
            post = new Post();
            User author = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));
            
            post.setAuthor(author);
            post.setStatus(Post.PostStatus.DRAFT); 
            post.setType(Post.PostType.BLOG);
            post.setViewCount(0);
            post.setIsDeleted(false);
        }

        // 2. MAP DỮ LIỆU
        post.setTitle(dto.getTitle());
        post.setSummary(dto.getSummary());
        post.setThumbnailUrl(dto.getThumbnailUrl());
        post.getSections().clear();

        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            int order = 0;
            for (SectionRequest secDto : dto.getSections()) {
                PostSection newSection = new PostSection();
                newSection.setSectionTitle(secDto.getSectionTitle());
                newSection.setSectionContent(secDto.getSectionContent());
                newSection.setImageUrl(secDto.getImageUrl());
                newSection.setSectionOrder(order++); 
                
                post.addSection(newSection); 
            }
        }

        // 3. XỬ LÝ SLUG 
        String baseSlug = dto.getSlug();
        if (baseSlug == null || baseSlug.trim().isEmpty()) {
            baseSlug =SlugUtils.makeSlug((dto.getTitle()));
            if(baseSlug.isEmpty()) 
                baseSlug = "bai-viet-moi";
        }

        String finalSlug = baseSlug;
        int count = 1;
        while (postRepository.existsBySlugAndIdNot(finalSlug, currentId)) {
            finalSlug = baseSlug + "-" + count++;
        }

        post.setSlug(finalSlug);

        return postRepository.save(post);
    }

    @Transactional
    public void deleteDraftPost(UUID postId, UUID authorId){
        String limitKey = "DELETE_POST_" + authorId ;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 5, 5);
        if(!bucket.tryConsume(1))
            throw new RateLimitException("Thao tác quá nhanh!");

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        if(!post.getAuthor().getId().equals(authorId))
            throw new RuntimeException("Bạn không có quyền xóa bài viết này!");
        
        post.setIsDeleted(true);
        postRepository.save(post);
    }
}