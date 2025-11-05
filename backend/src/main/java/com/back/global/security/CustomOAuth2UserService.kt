package com.back.global.security

import com.back.domain.member.member.service.MemberService
import lombok.RequiredArgsConstructor
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@RequiredArgsConstructor
class CustomOAuth2UserService(private val memberService: MemberService) : DefaultOAuth2UserService() {
    @Transactional
    override fun loadUser(
        userRequest: OAuth2UserRequest
    ): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val oauthUserId = oAuth2User.getName()
        val providerTypeCode = userRequest.getClientRegistration().getRegistrationId().uppercase(Locale.getDefault())

        val attributes = oAuth2User.getAttributes()
        val attributesProperties = attributes.get("properties") as MutableMap<String?, Any?>

        val userNicknameAttributeName = "nickname"
        val profileImgUrlAttributeName = "profile_image"

        val nickname = attributesProperties.get(userNicknameAttributeName).toString()
        val profileImgUrl = attributesProperties.get(profileImgUrlAttributeName).toString()
        val username = providerTypeCode + "__${oauthUserId}"
        val password = ""
        val member = memberService.modifyOrJoin(username, password, nickname, profileImgUrl)

        return SecurityUser(
            member.id,
            member.username,
            member.password,
            member.nickname,
            member.authorities,
        )
    }
}
