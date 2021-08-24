package com.skyflow_android.core.container

import com.skyflow_android.collect.client.CollectContainer
import com.skyflow_android.collect.elements.SkyflowElement
import com.skyflow_android.core.Skyflow
import com.skyflow_android.reveal.client.RevealContainer
import com.skyflow_android.reveal.elements.SkyflowLabel

class Container<T:ContainerProtocol>(
    internal val skyflow: Skyflow
) {
    internal val elements: MutableList<SkyflowElement> = mutableListOf();
    internal val revealElements: MutableList<SkyflowLabel> = mutableListOf();
}

