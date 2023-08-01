package Skyflow

@Description("Contains valid Skyflow container types.")
class ContainerType {
    companion object{
        val COLLECT = CollectContainer::class;
        val REVEAL = RevealContainer::class;
    }
}