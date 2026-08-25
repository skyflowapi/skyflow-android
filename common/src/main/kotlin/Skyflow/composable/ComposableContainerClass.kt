package Skyflow.composable

import com.Skyflow.core.container.ContainerProtocol

/**
 * Neutral composable-container marker type, shared by both SDKs and referenced by
 * `ContainerType`. The contract-specific operations are extension functions defined in each
 * product's module (see docs/sdk-split-plan.md).
 */
class ComposableContainer : ContainerProtocol
