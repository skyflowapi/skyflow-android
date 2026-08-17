package Skyflow

import Skyflow.composable.ComposableContainer

class ContainerType {
    companion object {
        val COLLECT = CollectContainer::class
        val REVEAL = RevealContainer::class
        val COMPOSABLE = ComposableContainer::class
    }
}