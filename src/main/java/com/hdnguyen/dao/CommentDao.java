package com.hdnguyen.dao;

import com.hdnguyen.entity.Comment;
import com.hdnguyen.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CommentDao extends JpaRepository<Comment, Long> {
    List<Comment> findByCommentIsNullAndGroup(Group group);
}
