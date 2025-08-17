package com.abappi19.kmpsample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform