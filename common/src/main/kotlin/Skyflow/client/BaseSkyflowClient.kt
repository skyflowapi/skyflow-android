package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import kotlin.reflect.KClass

/**
 * Contract-agnostic base client shared by both SDKs (see docs/sdk-split-plan.md).
 *
 * It owns only what is common to every backend contract: the [configuration], the element
 * registry ([elementMap]), and the `container(...)` factories. Each product's concrete
 * `Client` extends this:
 *  - the legacy (`skyflow-android-sdk`) `Client` adds the v1 client methods
 *    (`insert` / `get` / `getById` / `detokenize` / connection calls) and the v1 api client;
 *  - the FlowVault (`skyflow-flowvault-android-sdk`) `Client` adds only its v2 api client and
 *    has no standalone client methods.
 *
 * `container()` lives here because it is identical for both products. Contract-specific work
 * (the api client, request building) is reached from the per-product container extensions via
 * the concrete `Client`.
 */
abstract class BaseSkyflowClient internal constructor(
    val configuration: BaseConfiguration,
) : ISkyflowClient {
    internal val tag = this::class.qualifiedName
    internal val elementMap = HashMap<String, Any>()

    override fun <T : ContainerProtocol> container(type: KClass<T>): Container<T> {
        if (type == ContainerType.COLLECT) {
            Logger.info(tag, Messages.COLLECT_CONTAINER_CREATED.getMessage(), configuration.options.logLevel)
        } else if (type == ContainerType.REVEAL) {
            Logger.info(tag, Messages.REVEAL_CONTAINER_CREATED.getMessage(), configuration.options.logLevel)
        }
        return Container(configuration, this)
    }

    override fun <T : ContainerProtocol> container(
        type: KClass<T>,
        context: Context,
        options: ContainerOptions
    ): Container<T> {
        when (type) {
            ContainerType.COMPOSABLE -> Logger.info(
                tag,
                Messages.COMPOSABLE_CONTAINER_CREATED.getMessage(),
                configuration.options.logLevel
            )
            else -> container(type)
        }
        return Container(configuration, this, context, options)
    }
}
