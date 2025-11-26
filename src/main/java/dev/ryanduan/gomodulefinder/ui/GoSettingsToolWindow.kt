package dev.ryanduan.gomodulefinder.ui

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import com.intellij.util.ui.JBUI
import dev.ryanduan.gomodulefinder.settings.GoModuleSettingsConfigurable
import dev.ryanduan.gomodulefinder.GoModuleBundle

class GoSettingsToolWindow(private val project: Project, private val onApplied: (() -> Unit)? = null) {
    val component: JPanel = JBPanel<JBPanel<*>>()

    init {
        val configurable: Configurable = GoModuleSettingsConfigurable(project)
        val settingsComponent = configurable.createComponent()
        val root = JPanel(BorderLayout())
        if (settingsComponent != null) {
            root.add(settingsComponent, BorderLayout.PAGE_START)
        }
        val savePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val saveButton = JButton(GoModuleBundle.message("go.settings.save.button"))
        savePanel.add(saveButton)
        root.add(savePanel, BorderLayout.SOUTH)
        component.layout = BorderLayout()
        root.border = JBUI.Borders.empty(8)
        component.add(root, BorderLayout.CENTER)
        saveButton.addActionListener {
            if (configurable.isModified) {
                configurable.apply()
                onApplied?.invoke()
            }
        }
    }
}
