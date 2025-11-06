package com.back.standard.extenctions

fun <T: Any> T?.getOrThrow(): T{
    return this ?: throw NoSuchElementException();
}