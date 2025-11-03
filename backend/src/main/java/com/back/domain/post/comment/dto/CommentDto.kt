package com.back.domain.post.comment.dto

import com.back.domain.post.comment.entity.Comment
import java.time.LocalDateTime

@JvmRecord
data class CommentDto(
    val id: Long?,
    val createDate: LocalDateTime?,
    val modifyDate: LocalDateTime?,
    val content: String?,
    val authorId: Long?,
    val authorName: String?,
    val postId: Long?
) {
    constructor(comment: Comment) : this(
        comment.getId(),
        comment.getCreateDate(),
        comment.getModifyDate(),
        comment.getContent(),
        comment.getAuthor().getId(),
        comment.getAuthor().getName(),
        comment.getPost().getId()
    )
}
