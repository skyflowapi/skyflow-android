package Skyflow

import com.Skyflow.core.container.ContainerProtocol

/**
 * Neutral container marker types, shared by both SDKs and referenced by [ContainerType].
 * The contract-specific container operations (`create` / `collect` / `reveal` / `post`) are
 * extension functions defined in each product's module (see docs/sdk-split-plan.md).
 */
open class CollectContainer : ContainerProtocol

class RevealContainer : ContainerProtocol
