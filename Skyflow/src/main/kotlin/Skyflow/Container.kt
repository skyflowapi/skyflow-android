package Skyflow

import Skyflow.core.APIClient
import com.Skyflow.core.container.ContainerProtocol

class Container<T: ContainerProtocol>(
    internal val apiClient: APIClient
) {
    internal val elements: MutableList<Element> = mutableListOf();
    internal val revealElements: MutableList<Label> = mutableListOf();
}

