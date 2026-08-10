package Skyflow.composable

internal class ComposableErrorsList(internal val size: Int) {
    private var composableErrors = MutableList(size) { "" }

    internal fun setError(index: Int, error: String) {
        composableErrors[index] = error
    }

    internal fun getErrors(): String {
        val result = StringBuilder()
        for (i in 0 until size) {
            val space = if (composableErrors[i].isEmpty()) "" else " "
            result.append(composableErrors[i] + space)
        }
        return result.toString()
    }

    internal fun isEmpty(): Boolean {
        for (error in composableErrors) {
            if (error.isNotEmpty()) return false
        }
        return true
    }
}