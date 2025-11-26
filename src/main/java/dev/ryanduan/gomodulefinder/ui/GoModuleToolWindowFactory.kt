package dev.ryanduan.gomodulefinder.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import dev.ryanduan.gomodulefinder.GoModuleBundle

class GoModuleToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val finderPanel = GoModuleToolWindow(project)
        val finderContent = ContentFactory.getInstance().createContent(
            finderPanel.component,
            GoModuleBundle.message("toolwindow.tab.finder.title"),
            false
        )
        toolWindow.contentManager.addContent(finderContent)

        val settingsPanel = GoSettingsToolWindow(project) { finderPanel.refreshDefaults() }
        val settingsContent = ContentFactory.getInstance().createContent(
            settingsPanel.component,
            GoModuleBundle.message("toolwindow.tab.settings.title"),
            false
        )
        toolWindow.contentManager.addContent(settingsContent)
    }
}
