package com.example.English.teaching.center.service;

import java.util.Optional;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.English.teaching.center.entity.BlogPost;
import com.example.English.teaching.center.entity.BlogPostSection;
import com.example.English.teaching.center.entity.Teacher;
import com.example.English.teaching.center.repository.BlogPostRepository;
import com.example.English.teaching.center.repository.TeacherRepository;
import com.example.English.teaching.center.utils.SlugUtils;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final TeacherRepository teacherRepository;

    public BlogPostService(BlogPostRepository blogPostRepository, 
                            TeacherRepository teacherRepository) {
        this.blogPostRepository = blogPostRepository;
        this.teacherRepository = teacherRepository;
    }

// Process for student ---------------------------------------------------------------------
    public Optional<BlogPost> findBlogPostById(Long id) {
        return blogPostRepository.findById(id);
    }

// Process for teacher ---------------------------------------------------------------------
    public Page<BlogPost> getBlogPostsByAuthor_Id(Long teacherId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        return blogPostRepository.findByAuthor_Id(teacherId, pageable);
    }

    public Optional<BlogPost> findBlogPostBySlug(String slug) {
        return blogPostRepository.findBySlug(slug);
    }

    public BlogPost createNewDraftBlogPost(){
        BlogPost newPost = new BlogPost();
        newPost.setStatus(BlogPost.Status.DRAFT);
        newPost.setSections(new ArrayList<>());
        return newPost;
    }

    @Transactional
    public BlogPost saveOrUpdateBlogPost(BlogPost blogPost, Long userId) {
        Teacher teacher = teacherRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
        String baseSlug = SlugUtils.makeSlug(blogPost.getTitle());
        String finalSlug = baseSlug;
        int count = 1;
        Long currentId = (blogPost.getId() != null) ? blogPost.getId() : -1L;
        
        while (blogPostRepository.existsBySlugAndIdNot(finalSlug, currentId)) {
            finalSlug = baseSlug + "-" + count++;
        }
        blogPost.setSlug(finalSlug);

        if(blogPost.getId() == null){
            // Tạo mới
            blogPost.setAuthor(teacher);
            blogPost.setStatus(BlogPost.Status.DRAFT);
        }else{
            // Cập nhật
            BlogPost existingPost = blogPostRepository.findById(blogPost.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

            if (!existingPost.getAuthor().getId().equals(userId)) {
                throw new RuntimeException("Bạn không có quyền chỉnh sửa!");
            }

            blogPost.setAuthor(existingPost.getAuthor());
            blogPost.setCreatedAt(existingPost.getCreatedAt());
            blogPost.setViewCount(existingPost.getViewCount());
        }

        if(blogPost.getSections() != null){
            for(int i=0; i<blogPost.getSections().size(); i++){
                BlogPostSection section = blogPost.getSections().get(i);
                section.setBlogPost(blogPost); 
                section.setSortOrder(i); 
            }
        }

        return blogPostRepository.save(blogPost);
    }

    @Transactional
    public void deleteDraftBlogPost(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết trên Blog"));

        if(blogPost.getStatus() != BlogPost.Status.DRAFT){
            throw new RuntimeException("Chỉ được phép xóa các khóa học ở trạng thái Nháp!");
        }

        blogPostRepository.delete(blogPost);
    }
}