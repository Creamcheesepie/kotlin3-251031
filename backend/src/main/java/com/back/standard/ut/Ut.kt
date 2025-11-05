package com.back.standard.ut

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

object Ut {
    object jwt {
        fun toString(secret: String, expireSeconds: Long, body: Map<String, Any>): String {
            val claimsBuilder = Jwts.claims()

            for (entry in body.entries) {
                claimsBuilder.add(entry.key, entry.value)
            }

            val claims = claimsBuilder.build()

            val issuedAt = Date()
            val expiration = Date(issuedAt.getTime() + 1000L * expireSeconds)

            val secretKey: Key = Keys.hmacShaKeyFor(secret.toByteArray())

            val jwt = Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()

            return jwt
        }

        @JvmStatic
        fun isValid(jwt: String, secretPattern: String): Boolean {
            val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))


            kotlin.runCatching {
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwt)
            }.getOrElse { return false }
           return true
        }

        @JvmStatic
        fun payloadOrNull(jwt: String, secretPattern: String): MutableMap<String?, Any?>? {
            val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))

            if (isValid(jwt, secretPattern)) {
                return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwt)
                    .getPayload() as MutableMap<String?, Any?>
            }

            return null
        }
    }
}