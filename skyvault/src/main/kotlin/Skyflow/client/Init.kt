package Skyflow

import com.skyflow_android.BuildConfig

/**
 * Legacy (v1) entry point. Public signature is unchanged from 1.27.0; the shared body lives in the
 * common generic [baseInit] helper. Supplies the concrete [Client] and this module's BuildConfig.
 */
fun init(configuration: Configuration): Client =
    baseInit(BuildConfig.SDK_NAME, BuildConfig.SDK_VERSION, configuration.options.logLevel) {
        Client(configuration)
    }
