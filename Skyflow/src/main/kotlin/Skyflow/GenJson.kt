package com.Skyflow

import Skyflow.CollectContainer
import Skyflow.Description
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KParameter
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.File
import kotlin.reflect.*
//import java.lang.reflect.Modifier
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

data class Arg(val name: String, val type: String, val description: String?)
data class Method(val name: String, val args: List<Arg>, val description: String?, val returnType: String)
data class CompanionObject(val properties:List<Arg>, val methods: List<Method>)
data class ClassInfo(
    val name: String,
    val type: String,
    val args: List<Arg>,
    var methods: List<Method>,
    val enumValues: List<String>,
    val companionObject: CompanionObject?,
    val description: String?
)

/**
 * Student Class with name and age
 * @property name name of student
 * @property age age of student
 */
class Student(val name: String, val age: Int) {

    /**
     * Description for display
     */
    @Description("Random method in Student class")
    fun display() {
        println("$name is $age years old")
    }

    /**
     * Random method in Student class
     * @param arg1 argument 1
     * @param arg2 argument 2
     */
    @Description("Random method in Student class")
    @Deprecated("Support for this method will be removed soon. Please contact admin", level = DeprecationLevel.WARNING)
    fun method(arg1: String, arg2: String) {
        println(arg1)
        println(arg2)
    }
}
class GenJson  {}

fun getMethodDetails(method: KFunction<*>): Method {
    return  if (method.parameters.isNotEmpty()) {
        Method(
            method.name,
            method.parameters
                .filterNot { it.kind == KParameter.Kind.INSTANCE }
                .map {
                    Arg(
                        it.name.toString(),
                        it.type.toString(),
                        it.findAnnotation<Description>()?.text ?: ""
                    )
                },
            method.findAnnotation<Description>()?.text ?: "",
            method.returnType.toString()
        )
    } else {
        Method(
            method.name,
            emptyList(),
            method.findAnnotation<Description>()?.text ?: "",
            method.returnType.toString()
        )
    }
}

fun getCompanionObjectDetails(module: KClass<out Any>): CompanionObject {
    // get properties and methods defined inside companion object
    val properties = module.companionObject?.declaredMemberProperties
        ?.map { Arg(
            it.name,
            it.returnType.toString(),
            it.findAnnotation<Description>()?.text ?: ""
        )}!!
    val methods = module.companionObject?.declaredFunctions
        ?.filterNot { method -> method.hasAnnotation<Deprecated>() }
        ?.filterNot { method -> method.visibility == KVisibility.INTERNAL }
        ?.map { method -> getMethodDetails(method) }!!
    return CompanionObject(properties, methods)
}

fun main() {
    // This list is for classes
    val packages = listOf("Client", "Configuration", "TokenProvider", "LogLevel", "Env", "utils.EventName", "ContainerType", "Callback", "Options",
        "CollectElementInput", "CollectElementOptions", "CollectOptions", "RevealElementInput", "RevealElementOptions", "RevealOptions",
        "InsertOptions", "SkyflowElementType", "RedactionType", "Styles", "Style", "Padding", "CollectContainer", "RevealContainer")

    // If a file has class and methods outside class. Provide it in both lists to get class details from above list and methods details outside class from below

    // This list is for files that have methods outside the class or with no class. Provide whole filename along with Kt extension.
    val ktPackages = listOf("CollectContainerKt", "RevealContainerKt", "InitKt")
    var list = listOf<ClassInfo>()
    var args: List<Arg>
    var methods: List<Method>
    var enumValues: List<String>
    var companionObject: CompanionObject?
    var classInfo: ClassInfo

    for( packageName in packages) {
        val module = Class.forName("Skyflow."+packageName).kotlin

        // check for enum Class
        if(module.java.isEnum) {

            // get enum values
            enumValues = module.java.enumConstants.map { it.toString() }

            // get methods in the class
            methods = module.declaredFunctions
                .filterNot { method -> method.hasAnnotation<Deprecated>() }
                .filterNot { method -> method.visibility == KVisibility.INTERNAL }
                .filterNot { method -> method.name == "valueOf" || method.name == "values"}
                .map { method -> getMethodDetails(method) }

            // get companion object details
            companionObject = if(module.companionObject?.isCompanion == true) {
                getCompanionObjectDetails(module)
            } else {
                null
            }

            // class details
            classInfo = ClassInfo(
                module.simpleName ?: "",
                "enum",
                emptyList(),
                methods,
                enumValues,
                companionObject,
                module.findAnnotation<Description>()?.text ?: ""
            )

            list += classInfo
        }
        else {
            // get constructor arguments
            args = when (module.java.isInterface) {
                true -> emptyList()
                false -> module.constructors?.first()
                    ?.parameters?.map {
                        Arg(
                            it.name!!,
                            it.type.toString(),
                            it.findAnnotation<Description>()?.text ?: ""
                        )
                    }
            }

            methods = module.declaredFunctions
                .filterNot { method -> method.hasAnnotation<Deprecated>() }
                .filterNot { method -> method.visibility == KVisibility.INTERNAL }
                .map { method -> getMethodDetails(method) }

            companionObject = if(module.companionObject?.isCompanion == true) {
                getCompanionObjectDetails(module)
            } else {
                null
            }

            classInfo = ClassInfo(
                module.simpleName ?: "",
                if (module.java.isInterface) "interface" else "class",
                args,
                methods,
                emptyList(),
                companionObject,
                module.findAnnotation<Description>()?.text ?: ""
            )
            list += classInfo
        }
    }

    for( ktPackage in ktPackages) {
        val module = Class.forName("Skyflow."+ktPackage)

        // to get methods outside class or from a file with no class
        methods = module.declaredMethods
//            .filterNot { method -> method.getAnnotation()<Deprecated>() }
            .filterNot { method -> method.kotlinFunction?.visibility == KVisibility.INTERNAL }
            .filterNot { method -> method.name.contains("\$default")}
            .map { method ->
                if (method.kotlinFunction?.parameters?.isNotEmpty() == true) {
                    Method(
                        method.name,
                        method.kotlinFunction?.parameters!!
                            .filterNot { it.name.toString() == "null" }
                            .filterNot { it.kind == KParameter.Kind.INSTANCE }
                            .map {
                                Arg(
                                    it.name.toString(),
                                    it.type.toString(),
                                    it.findAnnotation<Description>()?.text ?: ""
                                )
                            },
                        method.kotlinFunction?.findAnnotation<Description>()?.text ?: "",
                        method.returnType.simpleName.toString()
                    )
                } else {
                    Method(
                        method.name,
                        emptyList(),
                        method.kotlinFunction?.findAnnotation<Description>()?.text ?: "",
                        method.returnType.simpleName.toString()
                    )
                }
            }

        // find class details in list
        val obj = list.find { it.name == module.simpleName.replace("Kt", "") }

        // if class details present add methods to that object or create new class info
        if(obj == null) {
            classInfo = ClassInfo(
                module.simpleName.replace("Kt", "") ?: "",
                "",
                emptyList(),
                methods,
                emptyList(),
                null,
                ""
            )
            list += classInfo
        }
        else {
            obj.methods += methods
        }
    }

    val mapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)
    val json = mapper.writeValueAsString(list)

    // code to write JSON to file
    val filename = "output.json" // replace with desired filename
    val path = "docs/json/" // replace with desired path
    val file = File("$path$filename")
    file.writeText(json)

}
