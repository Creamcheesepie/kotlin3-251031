package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import java.time.LocalDateTime

data class MemberDto(
    val id: Long?,
    val createDate: LocalDateTime?,
    val modifyDate: LocalDateTime?,
    val name: String?,
    val isAdmin: Boolean
) {
    constructor(member: Member) : this(
        member.id,
        member.createDate,
        member.modifyDate,
        member.nickname,
        member.isAdmin
    )
}
