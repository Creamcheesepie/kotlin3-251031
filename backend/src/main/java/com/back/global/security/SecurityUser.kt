package com.back.global.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
    val id: Long,
    username: String,
    password: String,
    val nickname: String,
    authorities: MutableCollection<out GrantedAuthority?>
) : User(username, password, authorities), // User를 상속받음과 동시에 해당 객체를 생성하겠음(생성자 호출
    OAuth2User { // 구현할 것임.
    override fun getAttributes(): Map<String?, Any?> = emptyMap()

    override fun getName(): String = nickname

}
