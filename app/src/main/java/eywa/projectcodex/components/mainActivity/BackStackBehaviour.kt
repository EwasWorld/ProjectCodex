package eywa.projectcodex.components.mainActivity

/**
 * Used as an argument type in the nav graph. This represents the behaviour of the back button when the given fragment
 * is on the top of the back stack
 */
enum class BackStackBehaviour {
    /**
     * When this location is popped off of the back stack, remove all other instances of it from the stack.
     * This means this location can only be returned to once
     */
    SINGLE,

    /**
     * Do not add this location to the back stack
     */
    NONE,

    /**
     * Return to this fragment as normal when it's at the top of the back stack
     */
    NORMAL
}