package eywa.projectcodex.common.helpShowcase

import eywa.projectcodex.common.utils.ResOrActual
import kotlin.reflect.KClass

interface DynamicHelpShowcaseInfo {
    /**
     * The screen type this [DynamicHelpShowcaseInfo] is used for
     */
    val type: KClass<out ActionBarHelp>

    /**
     * Called when the help showcase is started by the user.
     * On completing this function, the [DynamicHelpShowcaseInfo] should not change until [end] is called.
     * Doing so will result in undefined behaviour.
     */
    fun start(): Collection<HelpShowcaseItem>

    /**
     * Called when the help showcase has been completed
     */
    fun end()

    fun getInfoShowcases(key: ResOrActual<String>): HelpShowcaseItem?
}
