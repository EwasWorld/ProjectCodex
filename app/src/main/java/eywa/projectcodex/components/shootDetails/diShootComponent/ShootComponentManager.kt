package eywa.projectcodex.components.shootDetails.diShootComponent

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.RetainedLifecycle
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShootComponentManager @Inject constructor(
        private val shootComponentBuilder: ShootComponentBuilder,
) {
    private val shootComponents = ConcurrentHashMap<Int, ShootComponent>()

    fun getShootComponent(shootId: Int): ShootComponent =
            shootComponents[shootId] ?: run {
                val component = shootComponentBuilder
                        .shootId(shootId)
                        .shootLifecycle(ShootLifecycleImpl())
                        .build()
                shootComponents[shootId] = component
                component
            }

    fun exitShootDetails(shootId: Int) {
        val component = shootComponents.remove(shootId) ?: return
        val lifecycle = getEntryPoint<ShootLifecycleEntryPoint>(component).getLifecycle()
        (lifecycle as ShootLifecycleImpl).clear()
    }
}

/**
 * Entry point is used to access what's in the custom component
 * Here we're accessing the bound instance - the shoot lifecycle
 */
@InstallIn(ShootComponent::class)
@EntryPoint
interface ShootLifecycleEntryPoint {
    fun getLifecycle(): ShootLifecycle
}

/**
 * Used to hold listeners for when the component is cleared
 * In this case, the listeners will cancel the coroutines
 */
interface ShootLifecycle : RetainedLifecycle

private class ShootLifecycleImpl : ShootLifecycle {
    private val listeners = mutableSetOf<RetainedLifecycle.OnClearedListener>()

    override fun addOnClearedListener(listener: RetainedLifecycle.OnClearedListener) {
        listeners.add(listener)
    }

    override fun removeOnClearedListener(listener: RetainedLifecycle.OnClearedListener) {
        listeners.remove(listener)
    }

    fun clear() {
        listeners.forEach { it.onCleared() }
    }
}
