package Skyflow

import Skyflow.core.APIClient
import com.Skyflow.core.container.ContainerProtocol

class Container<T: ContainerProtocol>(
    internal val configuration: Configuration,
    internal val client: Client
) {
    internal val collectElements: MutableList<TextField> = mutableListOf();
    internal val revealElements: MutableList<Label> = mutableListOf();
}

