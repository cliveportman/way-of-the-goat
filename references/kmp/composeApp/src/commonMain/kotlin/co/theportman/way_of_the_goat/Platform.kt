package co.theportman.way_of_the_goat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform