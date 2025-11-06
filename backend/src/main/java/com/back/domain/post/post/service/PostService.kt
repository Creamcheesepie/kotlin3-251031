package com.back.domain.post.post.service

import com.back.domain.member.member.entity.Member
import com.back.domain.post.comment.entity.Comment
import com.back.domain.post.post.entity.Post
import com.back.domain.post.post.repository.PostRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class PostService(
    private val postRepository: PostRepository
) {
    fun write(author: Member, title: String?, content: String?): Post {
        val post = Post(author, title, content)

        return postRepository.save<Post>(post)
    }

    fun count(): Long {
        return postRepository.count()
    }

    // TODO : Optional 처리
    fun findById(id: Long): Post? {
        return postRepository.findById(id).getOrNull()
    }

    fun findAll(): MutableList<Post> {
        return postRepository.findAll()
    }

    fun modify(post: Post, title: String, content: String) {
        post.update(title, content)
    }

    fun writeComment(author: Member, post: Post, content: String): Comment {
        return post.addComment(author, content)
    }

    fun deleteComment(post: Post, commentId: Long) {
        post.deleteComment(commentId)
    }

    fun modifyComment(post: Post, commentId: Long, content: String) {
        post.updateComment(commentId, content)
    }

    fun delete(post: Post) {
        postRepository.delete(post)
    }

    fun flush() {
        postRepository.flush()
    }
}
