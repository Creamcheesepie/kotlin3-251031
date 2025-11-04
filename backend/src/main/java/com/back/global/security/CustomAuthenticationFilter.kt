package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.function.Supplier

@Component
@RequiredArgsConstructor
class CustomAuthenticationFilter : OncePerRequestFilter() {
    private val memberService: MemberService? = null
    private val rq: Rq? = null

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.debug("CustomAuthenticationFilter called")

        try {
            authenticate(request, response, filterChain)
        } catch (e: ServiceException) {
            val rsData = e.getRsData()
            response.setContentType("application/json")
            response.setStatus(rsData.statusCode)
            response.getWriter().write(
                """
                    {
                        "resultCode": "${rsData.resultCode}",
                        "msg": "${rsData.msg}","
                    }
                    
                    """.trimIndent()
            )
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws(ServletException::class, IOException::class)
    private fun authenticate(request: HttpServletRequest, response: HttpServletResponse?, filterChain: FilterChain) {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response)
            return
        }

        if (mutableListOf<String?>("/api/v1/members/join", "/api/v1/members/login").contains(request.getRequestURI())) {
            filterChain.doFilter(request, response)
            return
        }


        val apiKey: String
        val accessToken: String

        val headerAuthorization = rq!!.getHeader("Authorization", "")

        if (!headerAuthorization!!.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) throw ServiceException(
                "401-2",
                "Authorization 헤더가 Bearer 형식이 아닙니다."
            )

            val headerAuthorizationBits = headerAuthorization.split(" ".toRegex(), limit = 3).toTypedArray()

            apiKey = headerAuthorizationBits[1]
            accessToken = if (headerAuthorizationBits.size == 3) headerAuthorizationBits[2] else ""
        } else {
            apiKey = rq.getCookieValue("apiKey", "")
            accessToken = rq.getCookieValue("accessToken", "")
        }

        val isAdiKeyExists = !apiKey.isBlank()
        val isAccessTokenExists = !accessToken.isBlank()

        if (!isAdiKeyExists && !isAccessTokenExists) {
            filterChain.doFilter(request, response)
            return
        }

        var member: Member? = null
        var isAccessTokenValid = false
        if (isAccessTokenExists) {
            val payload = memberService!!.payloadOrNull(accessToken)

            if (payload != null) {
                val id = payload.get("id") as Long
                val username = payload.get("username") as String?
                val nickname = payload.get("nickname") as String?

                member = Member(id, username, nickname)
                isAccessTokenValid = true
            }
        }

        if (member == null) {
            member = memberService!!
                .findByApiKey(apiKey)
                .orElseThrow<ServiceException?>(Supplier { ServiceException("401-3", "API 키가 유효하지 않습니다.") })
        }

        if (isAccessTokenExists && !isAccessTokenValid) {
            val newAccessToken = memberService!!.genAccessToken(member)
            rq.setCookie("accessToken", newAccessToken)
            rq.setHeader("accessToken", newAccessToken)
        }

        val user: UserDetails = SecurityUser(
            member.getId(),
            member.getUsername(),
            "",
            member.getNickname(),
            member.getAuthorities()
        )

        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            user,
            user.getPassword(),
            user.getAuthorities()
        )


        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication)


        filterChain.doFilter(request, response)
    }
}
