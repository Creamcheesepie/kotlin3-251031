package com.back.global.rsData

import com.fasterxml.jackson.annotation.JsonIgnore

class RsData<T> @JvmOverloads constructor( // 기본 파라미터를 외부에서 쓰기 위해서는 @JvmOverloads가 필요하다.
    val resultCode: String,
    val msg: String?,
    val data: T? = null
) {

    @get:JsonIgnore
    val statusCode: Int
        get() {
            val statusCode =
                resultCode.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            return statusCode.toInt()
        }
}
