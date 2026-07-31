package Skyflow

import Skyflow.core.*
import Skyflow.core.Logger
import Skyflow.utils.Utils
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import kotlin.reflect.KClass

class Client internal constructor(
    val configuration: Configuration,
) {
    internal val tag = Client::class.qualifiedName

    internal val apiClient = FlowDBAPIClient(
        configuration.vaultID,
        configuration.vaultURL,
        configuration.tokenProvider,
        configuration.options.logLevel,
        okHttpClient = configuration.okHttpClient
    )

    internal val elementMap = HashMap<String, Any>()

    // Internal stub kept for dormant PDB code (ConnectionApiCallback, SoapApiCallback) to compile.
    // Not part of the public API and not called by any live code path.
    @Suppress("unused")
    internal fun detokenize(records: JSONObject, callback: Callback) {
        callback.onFailure(Utils.constructError(Exception("detokenize is not available in FlowDB mode")))
    }

    fun <T : ContainerProtocol> container(type: KClass<T>): Container<T> {
        if (type == ContainerType.COLLECT) {
            Logger.info(tag, Messages.COLLECT_CONTAINER_CREATED.getMessage(), configuration.options.logLevel)
        } else if (type == ContainerType.REVEAL) {
            Logger.info(tag, Messages.REVEAL_CONTAINER_CREATED.getMessage(), configuration.options.logLevel)
        }
        return Container<T>(configuration, this)
    }

    fun <T : ContainerProtocol> container(
        type: KClass<T>,
        context: Context,
        options: ContainerOptions
    ): Container<T> {
        when (type) {
            ContainerType.COMPOSABLE -> {
                Logger.info(
                    tag,
                    Messages.COMPOSABLE_CONTAINER_CREATED.getMessage(),
                    configuration.options.logLevel
                )
            }
            else -> container(type)
        }
        return Container(configuration, this, context, options)
    }
}
