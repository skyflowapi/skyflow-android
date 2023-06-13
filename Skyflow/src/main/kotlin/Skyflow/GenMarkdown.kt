package Skyflow

import com.Skyflow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

fun generateArgList(args: List<Arg>): StringBuilder {
    val sb = StringBuilder()
    for ((i, arg) in args.withIndex()) {
        sb.append("\n    ${arg.name}: ${arg.type}")
        if (i != args.lastIndex) {
            sb.append(",")
        }
    }
    sb.append("\n    ")
    return sb
}

fun generateTableForArgs(args: List<Arg>): StringBuilder {
    val sb = StringBuilder()
    for ((i, arg) in args.withIndex()) {
        sb.append("| ${arg.name} | ${arg.type} | ${arg.description} |\n")
    }
    return sb
}

fun extractItemName(line: String): String {
    val start = line.indexOf("[") + 1
    val end = line.indexOf("]")
    return line.substring(start, end)
}

fun sortValues(sb: StringBuilder): StringBuilder {
    // Store lines in a list
    val lines = sb.toString().split("\n").filter { it.isNotEmpty() }

    // Sort the lines based on item.name
    val sortedLines = lines.sortedBy { extractItemName(it) }

    // Clear the StringBuilder
    sb.setLength(0)

    // Append the sorted lines back to the StringBuilder
    sortedLines.forEach { sb.append("$it\n") }

    // Print the sorted StringBuilder
    return sb
}

fun main() {
    val jsonString = File("docs/json/output.json").readText()
    val list = Json.decodeFromString<List<ClassInfo>>(jsonString)
//    println(list)

//        .filter { item -> item.companionObject != null  }
    val overviewSb = StringBuilder()
    val classSb = StringBuilder()
    val staticMethodSb = StringBuilder().append("## Static Method\n\n")
    val interfaceSb = StringBuilder()
    val enumSb = StringBuilder()
    list.forEach { item ->
        val sb = StringBuilder()
        var itemType = ""
        sb.append("{% env enable=\"androidSdkRef\" %}\n\n")
        sb.append("# Skyflow.${item.name}")

        if(item.type == "class" || item.type == "enum") {
            itemType = "classes"
            sb.append("\n\n## Class ${item.name}\n")
            if(item.description?.isNotEmpty() == true) {
                sb.append("${item.description}\n")
            }
            sb.append("\n```kotlin\n")
            if(item.type == "enum") {
                sb.append("enum ")
                itemType = "enums"
            }
            sb.append("class ${item.name}")
            if(item.args.isNotEmpty()) {
                sb.append("(${generateArgList(item.args)})")
            }
            sb.append(" {")

            // Constructor
//            if (item.args.isNotEmpty()) {
//                sb.append("\n### Constructor\n\n")
//                sb.append("```kotlin\n")
//                sb.append("    constructor(")
//                for ((i, arg) in item.args.withIndex()) {
//                    sb.append("${arg.name}: ${arg.type}")
//                    if (i != item.args.lastIndex) {
//                        sb.append(", ")
//                    }
//                }
//                sb.append(")\n")
//                sb.append("```\n\n")
//            }
            if (item.companionObject != null) {
                sb.append("\n\n### Companion object\n\n")
                sb.append("    companion object {\n")
                for (property in item.companionObject.properties) {
                    sb.append("        ${property.name}: ${property.type}\n")
                }
                sb.append("    }\n")
            }
            sb.append("}\n```")

            if(item.enumValues.isNotEmpty()) {
                sb.append("\n\n### Enum Values\n\n")
                sb.append("| Value |\n| --- |\n")
                item.enumValues?.forEach {
                    sb.append("| ${it} |\n")
                }
//                sb.append("\n")
            }

            if(item.args.isNotEmpty()) {
                sb.append("\n\n### Parameters\n\n")
                sb.append("| Name | Type | Description |\n| --- | --- | --- |\n")
                sb.append("${generateTableForArgs(item.args)}")
            }
        }
        else if (item.type == "interface") {
            itemType = "interfaces"
            sb.append("\n\n## Interface ${item.name}\n")
            if(item.description?.isNotEmpty() == true) {
                sb.append("${item.description}\n")
            }
            sb.append("\n```kotlin\n")
            sb.append("interface ${item.name}")

            sb.append(" {}\n```")

        }
        if(item.methods.isNotEmpty()) {
            sb.append("\n\n## Functions\n")
            for (method in item.methods) {
                sb.append("\n### ${method.name}\n")
                if(method.description?.isNotEmpty() == true) {
                    sb.append("${method.description}\n")
                }
                sb.append("\n```kotlin\n")
                sb.append("fun ${method.name}(${generateArgList(method.args)}) : ${method.returnType} {}")
                sb.append("\n```")
                if(method.args.isNotEmpty()) {
                    sb.append("\n\n#### Parameters\n\n")
                    sb.append("| Name | Type | Description |\n| --- | --- | --- |\n")
                    sb.append("${generateTableForArgs(method.args)}")
                }
            }
        }
        sb.append("\n{% /env %}")
        when (itemType) {
            "classes" -> {
                classSb.append("- [${item.name}](/sdks/skyflow-android/${itemType}/${item.name})\n")
            }
            "interfaces" -> {
                interfaceSb.append("- [${item.name}](/sdks/skyflow-android/${itemType}/${item.name})\n")
            }
            "enums" -> {
                enumSb.append("- [${item.name}](/sdks/skyflow-android/${itemType}/${item.name})\n")
            }
            "" -> {
                staticMethodSb.append("[${item.name}](/sdks/skyflow-android/${item.name})\n")
            }
        }
        val filename = "${item.name}.md" // replace with desired filename
        var path = "docs/markdown/" // replace with desired path
        if(itemType != "")
        {
            path = "docs/markdown/${itemType}/"
        }
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File("$path$filename")
        file.writeText(sb.toString())
    }
    overviewSb.append("{% env enable=\"androidSdkRef\" %}\n\n")
    overviewSb.append("# Android\n\n")
    overviewSb.append("Some documentation for overview page\n\n")
    overviewSb.append("${staticMethodSb}\n")
    overviewSb.append("## Classes\n\n")
    overviewSb.append("${sortValues(classSb)}\n")
    overviewSb.append("## Interfaces\n\n")
    overviewSb.append("${sortValues(interfaceSb)}\n")
    overviewSb.append("## Enums\n\n")
    overviewSb.append("${sortValues(enumSb)}\n")
    overviewSb.append("{% /env %}")
    val overviewFile = File("docs/markdown/Overview.md")
    overviewFile.writeText(overviewSb.toString())
}