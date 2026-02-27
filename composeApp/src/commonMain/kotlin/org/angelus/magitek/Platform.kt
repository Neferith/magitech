package org.angelus.magitek

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform