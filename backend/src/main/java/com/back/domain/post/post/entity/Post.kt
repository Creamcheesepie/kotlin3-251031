package com.back.domain.post.post.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.comment.entity.Comment
import com.back.global.exception.ServiceException
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
class Post(
    id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    val author: Member,
    var title: String?,
    var content: String?
) : BaseEntity(id) {

    @OneToMany(
        mappedBy = "post",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true
    )
    val comments: MutableList<Comment> = mutableListOf()

    constructor(author: Member,title: String?,content: String?) : this(0,author, title, content) {}

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }

    fun addComment(author: Member, content: String): Comment {
        val comment = Comment(author, content, this)
        this.comments.add(comment)

        return comment
    }

    fun deleteComment(commentId: Long) {
        val comment = findCommentById(commentId).get()
        this.comments.remove(comment)
    }

    fun updateComment(commentId: Long, content: String): Comment {
        val comment = findCommentById(commentId).get()
        comment.update(content)
        return comment
    }

    fun findCommentById(commentId: Long): Optional<Comment> {
        return comments.stream()
            .filter { c: Comment? -> c!!.id == commentId }
            .findFirst()
    }

    fun checkActorModify(actor: Member) {
        if (!this.author.equals(actor)) {
            throw ServiceException("403-1", "수정 권한이 없습니다.")
        }
    }

    fun checkActorDelete(actor: Member) {
        if (!this.author.equals(actor)) {
            throw ServiceException("403-2", "삭제 권한이 없습니다.")
        }
    }
}
