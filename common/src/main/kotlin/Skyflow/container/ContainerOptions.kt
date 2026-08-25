package Skyflow

import Skyflow.composable.ComposableStyles

class ContainerOptions(
    val layout: Array<Int>,
    val styles: Styles? = ComposableStyles.getStyles(),
    val errorTextStyles: Styles? = ComposableStyles.getErrorTextStyles()
) {
}