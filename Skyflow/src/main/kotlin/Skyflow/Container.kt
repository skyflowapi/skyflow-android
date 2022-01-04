package Skyflow

import Skyflow.core.APIClient
import com.Skyflow.core.container.ContainerProtocol

class Container<T: ContainerProtocol>(
    internal val configuration: Configuration,
    internal val client: Client
) {
    internal val elements: MutableList<TextField> = mutableListOf();
    internal val revealElements: MutableList<Label> = mutableListOf();
}

