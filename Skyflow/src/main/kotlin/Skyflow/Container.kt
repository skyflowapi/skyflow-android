package Skyflow

import android.content.Context
import android.widget.LinearLayout
import com.Skyflow.core.container.ContainerProtocol

class Container<T : ContainerProtocol> internal constructor(
    internal val configuration: Configuration,
    internal val client: Client,
) {
    internal constructor(
        configuration: Configuration,
        client: Client,
        context: Context,
        options: ContainerOptions
    ) : this(configuration, client) {
        this.context = context
        this.composableLayout = LinearLayout(context)
        this.composableLayout.layoutParams = composableLayoutParams
        this.composableLayoutParams.setMargins(0, 20, 0, 0)
        this.composableLayout.orientation = LinearLayout.VERTICAL
        this.options = options

        val padding = this.options.styles!!.base.padding
        this.composableLayout.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        setTotalComposableElements()
    }

    private val composableLayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    internal val collectElements: MutableList<TextField> = mutableListOf();
    internal val revealElements: MutableList<Label> = mutableListOf()
    internal lateinit var context: Context
    internal lateinit var composableLayout: LinearLayout
    internal lateinit var options: ContainerOptions
    internal var totalComposableElements: Int = 0

    private fun setTotalComposableElements() {
        for (value in options.layout) {
            totalComposableElements += value
        }
    }
}

