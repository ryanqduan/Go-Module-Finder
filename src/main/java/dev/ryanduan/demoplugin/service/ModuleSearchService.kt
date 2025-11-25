package dev.ryanduan.demoplugin.service

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.HttpRequests
import java.net.URLEncoder
import dev.ryanduan.demoplugin.settings.GoModuleSettingsState
import com.intellij.openapi.project.ProjectManager
import com.google.gson.JsonParser

class ModuleSearchService {
    private val logger = Logger.getInstance(ModuleSearchService::class.java)
    private val librariesBase = "https://libraries.io/api"

    fun search(query: ModuleQuery): List<ModuleInfo> {
        return searchLibrariesIo(query)
    }

    private fun searchLibrariesIo(query: ModuleQuery): List<ModuleInfo> {
        val q = listOfNotNull(query.name, query.author, query.version).joinToString(" ").trim()
        if (q.isEmpty()) return emptyList()
        val apiKey = GoModuleSettingsState.getInstance(ProjectManager.getInstance().defaultProject).state.librariesApiKey
        val url = "$librariesBase/search?q=" + URLEncoder.encode(q, "UTF-8") + "&platforms=Go&per_page=" + query.limit + if (apiKey.isNotBlank()) "&api_key=$apiKey" else ""
        return try {
            val body = HttpRequests.request(url)
                .readString()
            val modules = parseLibrariesProjects(body)
            modules.filter { it.platform.equals("Go", ignoreCase = true) }
                .map { p ->
                    val versions = p.versions
                    val latest = p.latest_release_number ?: versions?.firstOrNull()
                    ModuleInfo(p.name, latest, Source.GODOC, versions)
                }
        } catch (e: Exception) {
            logger.warn("searchLibrariesIo error", e)
            emptyList()
        }
    }

    private fun parseLibrariesProjects(json: String): List<LibProjectExt> {
        val arr = try { JsonParser.parseString(json).asJsonArray } catch (e: Exception) { return emptyList() }
        val projects = mutableListOf<LibProjectExt>()
        for (el in arr) {
            val obj = el.asJsonObject
            val nameEl = obj.get("name")
            if (nameEl == null || nameEl.isJsonNull) continue
            val name = nameEl.asString
            val platform = obj.get("platform")?.let { if (!it.isJsonNull) it.asString else "" } ?: ""
            val latest = obj.get("latest_release_number")?.let { if (!it.isJsonNull) it.asString else null }
            val homepage = obj.get("homepage")?.let { if (!it.isJsonNull) it.asString else null }
            var versions: List<String>? = null
            val versArr = obj.get("versions")?.let { if (!it.isJsonNull) it.asJsonArray else null }
            if (versArr != null) {
                val items = mutableListOf<Pair<String, String?>>()
                for (v in versArr) {
                    val vobj = v.asJsonObject
                    val numEl = vobj.get("number")
                    if (numEl == null || numEl.isJsonNull) continue
                    val num = numEl.asString
                    val date = vobj.get("published_at")?.let { if (!it.isJsonNull) it.asString else null }
                    items.add(num to date)
                }
                versions = if (items.isNotEmpty()) items.sortedByDescending { it.second ?: "" }.map { it.first } else emptyList()
            }
            projects.add(LibProjectExt(name, platform, latest, homepage, versions))
        }
        return projects
    }

    private data class LibProjectExt(
        val name: String,
        val platform: String,
        val latest_release_number: String?,
        val homepage: String?,
        val versions: List<String>?
    )
}
