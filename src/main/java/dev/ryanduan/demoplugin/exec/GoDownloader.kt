package dev.ryanduan.demoplugin.exec

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
import dev.ryanduan.demoplugin.service.Source
import dev.ryanduan.demoplugin.settings.GoModuleSettingsState

object GoDownloader {
    fun download(project: Project, module: String, version: String?, source: Source) {
        val goPath = GoModuleSettingsState.getInstance(project).state.goPath
        val cmd = GeneralCommandLine(goPath, "get", if (version != null) "$module@$version" else module)
        if (source.proxy != null) {
            cmd.withEnvironment("GOPROXY", source.proxy)
        }
        object : Task.Backgroundable(project, "下载 $module", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "准备下载"
                try {
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
                        group.createNotification("下载完成", NotificationType.INFORMATION).notify(project)
                    } else {
                        group.createNotification("下载失败\n$output", NotificationType.ERROR).notify(project)
                    }
                } catch (e: Exception) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("GoModulePlugin")
                        .createNotification("下载异常 ${e.message}", NotificationType.ERROR)
                        .notify(project)
                }
            }
        }.queue()
    }
}
