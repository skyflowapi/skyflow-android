package Skyflow

@Description("This class contains the valid skyflow container types.")
class ContainerType {
    companion object{
        val COLLECT = CollectContainer::class;
        val REVEAL = RevealContainer::class;
    }
}