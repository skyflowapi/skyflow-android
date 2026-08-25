package Skyflow

import Skyflow.composable.ComposableStyles
import Skyflow.core.FlowDBAPIClient
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import kotlin.reflect.KClass

/**
 * FlowVault (v2 / FlowDB) client. Extends the shared [BaseSkyflowClient] (which provides
 * `configuration`, `elementMap`, and the `container(...)` factories) and adds only the v2 api
 * client. Unlike the legacy `Client` it exposes no standalone client methods — all v2 work goes
 * through the container extensions (collect / reveal; update happens within collect when an element
 * carries a skyflowId). See docs/sdk-split-plan.md.
 */
class Client internal constructor(
    configuration: Configuration,
) : BaseSkyflowClient(configuration) {

    internal val apiClient = FlowDBAPIClient(
        configuration.vaultID,
        configuration.vaultURL,
        configuration.tokenProvider,
        configuration.options.logLevel,
        okHttpClient = configuration.okHttpClient
    )

    // ContainerOptions.styles/errorTextStyles are nullable and a caller may pass null explicitly.
    // The shared Container/composable code force-unwraps them (kept as-is for v1 parity — the legacy
    // SDK crashes on null). For v2, default them here BEFORE the shared Container constructor runs so
    // a null-styles composable container renders with the default styles instead of throwing NPE.
    override fun <T : ContainerProtocol> container(
        type: KClass<T>,
        context: Context,
        options: ContainerOptions
    ): Container<T> {
        val safeOptions = if (options.styles != null && options.errorTextStyles != null) options
        else ContainerOptions(
            options.layout,
            options.styles ?: ComposableStyles.getStyles(),
            options.errorTextStyles ?: ComposableStyles.getErrorTextStyles()
        )
        return super.container(type, context, safeOptions)
    }
}
