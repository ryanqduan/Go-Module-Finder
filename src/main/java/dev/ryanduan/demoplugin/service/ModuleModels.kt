package dev.ryanduan.demoplugin.service

data class ModuleQuery(
    val name: String?,
    val author: String?,
    val version: String?,
    val source: Source,
    val limit: Int
)

data class ModuleInfo(
    val module: String,
    val latest: String?,
    val source: Source,
    val versions: List<String>? = null
)

data class LibProject(
    val name: String,
    val platform: String,
    val latest_release_number: String?,
    val homepage: String?,
)
