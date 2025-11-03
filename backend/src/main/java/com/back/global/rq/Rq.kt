package com.back.global.rq

import com.back.domain.member.member.entity.Member
import com.back.global.exception.ServiceException
import com.back.global.security.SecurityUser
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

@Component
@RequiredArgsConstructor
class Rq {
    private val request: HttpServletRequest? = null
    private val response: HttpServletResponse? = null

    val actor: Member?
        get() = Optional.ofNullable<Authentication?>(
            SecurityContextHolder
                .getContext().getAuthentication
                    ()
        )
            .map<Any?>(Function { obj: Authentication? -> obj!!.getPrincipal() })
            .filter(Predicate { principal: Any? -> principal is SecurityUser })
            .map<SecurityUser?>(Function { principal: Any? -> principal as SecurityUser })
            .map<Member?>(Function { securityUser: SecurityUser? ->
                Member(
                    securityUser!!.getId(),
                    securityUser.getUsername(),
                    securityUser.getNickname()
                )
            })
            .orElseThrow<ServiceException?>(Supplier {
                ServiceException(
                    "401-1",
                    "로그인 후 이용해주세요."
                )
            })

    fun setHeader(name: String?, value: String?) {
        response!!.setHeader(name, value)
    }

    fun getHeader(name: String?, defaultValue: String?): String? {
        return Optional
            .ofNullable<String?>(request!!.getHeader(name))
            .filter(Predicate { headerValue: String? -> !headerValue!!.isBlank() })
            .orElse(defaultValue)
    }

    fun getCookieValue(name: String?, defaultValue: String?): String? {
        return Optional
            .ofNullable<Array<Cookie?>?>(request!!.getCookies())
            .flatMap<String?>(
                Function { cookies: Array<Cookie?>? ->
                    Arrays.stream<Cookie?>(cookies)
                        .filter { cookie: Cookie? -> cookie!!.getName() == name }
                        .map<String?> { obj: Cookie? -> obj!!.getValue() }
                        .filter { value: String? -> !value!!.isBlank() }
                        .findFirst()
                }
            )
            .orElse(defaultValue)
    }

    fun setCookie(name: String?, value: String?) {
        var value = value
        if (value == null) value = ""

        val cookie = Cookie(name, value)
        cookie.setPath("/")
        cookie.setHttpOnly(true)
        cookie.setDomain("localhost")
        cookie.setSecure(true)
        cookie.setAttribute("SameSite", "Strict")

        // 값이 없다면 해당 쿠키변수를 삭제하라는 뜻
        if (value.isBlank()) {
            cookie.setMaxAge(0)
        }

        response!!.addCookie(cookie)
    }

    fun deleteCookie(name: String?) {
        setCookie(name, null)
    }

    @Throws(IOException::class)
    fun sendRedirect(url: String?) {
        response!!.sendRedirect(url)
    }
}
