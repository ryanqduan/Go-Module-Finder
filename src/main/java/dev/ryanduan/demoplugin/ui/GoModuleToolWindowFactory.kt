package dev.ryanduan.demoplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class GoModuleToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = GoModuleToolWindow(project)
        val content = ContentFactory.getInstance().createContent(panel.component, "Go Module Finder", false)
        toolWindow.contentManager.addContent(content)
    }
}

