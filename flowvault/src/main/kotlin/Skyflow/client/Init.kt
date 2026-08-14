package Skyflow

import com.skyflow_android.BuildConfig

/**
 * FlowVault (v2) entry point. The shared body lives in the common generic [baseInit] helper;
 * this supplies the concrete [Client] and this module's BuildConfig.
 */
fun init(configuration: Configuration): Client =
    baseInit(BuildConfig.SDK_NAME, BuildConfig.SDK_VERSION, configuration.options.logLevel) {
        Client(configuration)
    }
