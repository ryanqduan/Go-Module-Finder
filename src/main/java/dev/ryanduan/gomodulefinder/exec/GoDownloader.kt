package dev.ryanduan.gomodulefinder.exec

import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.execution.configurations.GeneralCommandLine
import dev.ryanduan.gomodulefinder.service.Source
import dev.ryanduan.gomodulefinder.GoModuleBundle
import dev.ryanduan.gomodulefinder.settings.GoModuleSettingsState
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

object GoDownloader {
    fun download(project: Project, module: String, version: String?, source: Source) {
        val goPath = GoModuleSettingsState.getInstance(project).state.goPath
        val cmd = GeneralCommandLine(goPath, "get", if (version != null) "$module@$version" else module)
        val state = GoModuleSettingsState.getInstance(project).state
        val proxy = when {
            source.proxy != null -> source.proxy
            source == Source.CUSTOM && state.customProxy.isNotBlank() -> state.customProxy
            else -> null
        }
        if (proxy != null) {
            cmd.withEnvironment("GOPROXY", proxy)
        }
        object : Task.Backgroundable(project, GoModuleBundle.message("downloader.task.title", module), true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = GoModuleBundle.message("downloader.preparing")
                try {
                    val workDir = resolveGoModuleDir(project)
                    if (workDir == null) {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("GoModulePlugin")
                            .createNotification(GoModuleBundle.message("downloader.noGoMod"), NotificationType.ERROR)
                            .notify(project)
                        return
                    }
                    cmd.withWorkDirectory(workDir)
                    val handler = OSProcessHandler(cmd)
                    ProcessTerminatedListener.attach(handler)
                    val group = NotificationGroupManager.getInstance().getNotificationGroup("GoModulePlugin")
                    val output = StringBuilder()
                    handler.addProcessListener(object : ProcessAdapter() {
                        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                            if (outputType == ProcessOutputTypes.STDOUT || outputType == ProcessOutputTypes.STDERR) {
                                output.append(event.text)
                                indicator.text2 = event.text.trim()
                            }
                        }
                    })
                    handler.startNotify()
                    handler.waitFor()
                    val exit = handler.exitCode ?: -1
                    if (exit == 0) {
                        val dirFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(workDir)
                        if (dirFile != null) {
                            VfsUtil.markDirtyAndRefresh(true, true, true, dirFile)
                        }
                        group.createNotification(GoModuleBundle.message("downloader.done"), NotificationType.INFORMATION).notify(project)
                    } else {
                        group.createNotification(GoModuleBundle.message("downloader.fail", output.toString()), NotificationType.ERROR).notify(project)
                    }
                } catch (e: Exception) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("GoModulePlugin")
                        .createNotification(GoModuleBundle.message("downloader.exception", e.message ?: ""), NotificationType.ERROR)
                        .notify(project)
                }
            }
        }.queue()
    }

    private fun resolveGoModuleDir(project: Project): String? {
        val base = project.basePath
        if (base != null) {
            val baseMod = LocalFileSystem.getInstance().findFileByPath("$base/go.mod")
            if (baseMod != null && baseMod.exists()) return base
        }
        val files = FilenameIndex.getVirtualFilesByName("go.mod", GlobalSearchScope.projectScope(project))
            .toList()
        val first = files.firstOrNull()
        return first?.parent?.path
    }
}
