package Skyflow

import com.Skyflow.core.container.ContainerProtocol

class Container<T: ContainerProtocol>(
    internal val client: Client
) {
    internal val elements: MutableList<Element> = mutableListOf();
    internal val revealElements: MutableList<Label> = mutableListOf();
}

