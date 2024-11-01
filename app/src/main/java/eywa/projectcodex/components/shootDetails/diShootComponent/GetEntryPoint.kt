package eywa.projectcodex.components.shootDetails.diShootComponent

import dagger.hilt.EntryPoints

inline fun <reified T> getEntryPoint(component: Any): T {
    return EntryPoints.get(component, T::class.java)
}
