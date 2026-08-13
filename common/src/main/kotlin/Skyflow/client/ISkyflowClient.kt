package Skyflow

import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import kotlin.reflect.KClass


interface ISkyflowClient {
    fun <T : ContainerProtocol> container(type: KClass<T>): Container<T>

    fun <T : ContainerProtocol> container(
        type: KClass<T>,
        context: Context,
        options: ContainerOptions
    ): Container<T>
}
