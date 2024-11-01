package eywa.projectcodex.components.shootDetails.diShootComponent

import dagger.BindsInstance
import dagger.hilt.DefineComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Scope

/**
 * Scope determines whether a new instance is created or the same instance is used
 */
@Scope
annotation class ShootScoped

/**
 * Component determines the lifetime of the instances created
 * For Singleton it's from Application.onCreate to Application.onDestroy
 *
 * To avoid excessive dependencies, methods are not allowed, instead they should be accessed with entry points
 * https://dagger.dev/hilt/custom-components
 *
 * https://github.com/steviek/UserComponentDemo
 */
@ShootScoped
@DefineComponent(parent = SingletonComponent::class)
interface ShootComponent

@DefineComponent.Builder
interface ShootComponentBuilder {
    fun shootId(@BindsInstance shootId: Int): ShootComponentBuilder
    fun shootLifecycle(@BindsInstance shootLifecycle: ShootLifecycle): ShootComponentBuilder
    fun build(): ShootComponent
}
