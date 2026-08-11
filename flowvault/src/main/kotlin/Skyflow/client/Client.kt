package Skyflow

import Skyflow.core.FlowDBAPIClient

/**
 * FlowVault (v2 / FlowDB) client. Extends the shared [BaseSkyflowClient] (which provides
 * `configuration`, `elementMap`, and the `container(...)` factories) and adds only the v2 api
 * client. Unlike the legacy `Client` it exposes no standalone client methods — all v2 work goes
 * through the container extensions (collect / reveal / update). See docs/sdk-split-plan.md.
 */
class Client internal constructor(
    configuration: Configuration,
) : BaseSkyflowClient(configuration) {

    internal val apiClient = FlowDBAPIClient(
        configuration.vaultID,
        configuration.vaultURL,
        configuration.tokenProvider,
        configuration.options.logLevel,
        okHttpClient = configuration.okHttpClient
    )
}
