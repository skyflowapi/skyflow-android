package com.skyflow_android.core.container

import com.skyflow_android.collect.client.CollectContainer
import com.skyflow_android.core.Skyflow

class Container<T:ContainerProtocol>(
    internal val skyflow: Skyflow
) {
    internal val elements: MutableList<String> = mutableListOf();
    internal val revealElements: MutableList<String> = mutableListOf();
}


fun Container<CollectContainer>.collect(){

}

//fun Container<RevealContainer>.reveal(){
//    print("Reveal Called" + item)
//}


// class Container<T> where T:RevealContainer{
//     fun insert(){
//         print("Insert called")
//     }

//     fun reveal(){
//         print("reveal called")
//     }
// }

