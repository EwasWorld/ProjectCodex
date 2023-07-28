package eywa.projectcodex.components.mainMenu

data class AppVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
) : Comparable<AppVersion> {
    constructor(version: List<Int>) : this(version[0], version[1], version[2])
    constructor(version: String) : this(version.split(".").map { it.toInt() })

    override fun compareTo(other: AppVersion): Int {
        fun compareInt(a: Int, b: Int) = a.compareTo(b).takeIf { it != 0 }

        return compareInt(major, other.major)
                ?: compareInt(minor, other.minor)
                ?: compareInt(patch, other.patch)
                ?: 0
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }
}
