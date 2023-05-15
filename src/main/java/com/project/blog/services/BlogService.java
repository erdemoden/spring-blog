package com.project.blog.services;

import com.project.blog.DTOS.PostLikeId;
import com.project.blog.entities.Blogs;
import com.project.blog.entities.User;
import com.project.blog.repositories.BlogsRepository;
import com.project.blog.repositories.UserRepository;
import com.project.blog.requests.BlogCreateRequest;
import com.project.blog.responses.FindBlogsByTitle;
import com.project.blog.responses.UserBlogLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final BlogsRepository blogsRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    public Blogs saveBlog(BlogCreateRequest blogCreateRequest,String authorization){
        Optional<User> user = userService.getUserFromAuth(authorization);
        List<User> followers = new LinkedList<>();
        Blogs blogs = new Blogs();
        blogs.setSubject(blogCreateRequest.getSubject());
        blogs.setTitle(blogCreateRequest.getTitle());
        blogs.setOwner(user.get());
        followers.add(user.get());
        blogs.setFollowers(followers);
        blogsRepository.save(blogs);
        return blogs;
    }
    public  void deleteBlog(long blogid){
        Blogs blogs = blogsRepository.findById(blogid).orElse(null);
        blogsRepository.delete(blogs);
    }
    public List<Blogs> findByUser(long userid){
        User user = userRepository.findById(userid).orElse(null);
        return user.getFollowerBlogs();
    }
    public List<UserBlogLike> findByUserAndBlogLike(String name){
        List<UserBlogLike> userBlogLike = new LinkedList<>();
        List <User> users = userRepository.findByUsernameLike(name);
        List <Blogs> blogs = blogsRepository.findByTitleLike(name);
        if(users.size()>0){
            users.stream().forEach(user-> userBlogLike.add(new UserBlogLike(user.getUsername(),"user")));
        }
        if(blogs.size()>0){
            blogs.stream().forEach(blog-> userBlogLike.add(new UserBlogLike(blog.getTitle()+"/B","blog")));
        }
        return userBlogLike;
    }
    public FindBlogsByTitle findByTitle(String title){
        Blogs blogs = blogsRepository.findByTitle(title);
        FindBlogsByTitle findBlogsByTitle = new FindBlogsByTitle();
        findBlogsByTitle.setTitle(blogs.getTitle());
        findBlogsByTitle.setSubject(blogs.getSubject());
        findBlogsByTitle.setId(blogs.getId());
        List<PostLikeId> postLikeIds= new ArrayList<>();
        blogs.getPosts().stream().forEach(post->{
            PostLikeId postLikeId = new PostLikeId();
            postLikeId.setUserName(post.getUser().getUsername());
            postLikeId.setUserPhoto(post.getUser().getUserphoto());
            postLikeId.setPost(post.getPost());
            postLikeId.setId(post.getId());
            postLikeId.setLikes(post.getLikes().size());
            postLikeId.setComments(post.getComments());
            postLikeIds.add(postLikeId);
        });
        findBlogsByTitle.setPostLikeIdList(postLikeIds);
        return findBlogsByTitle;
    }
}
