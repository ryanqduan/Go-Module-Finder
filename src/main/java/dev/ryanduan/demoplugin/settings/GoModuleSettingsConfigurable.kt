package dev.ryanduan.demoplugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.ryanduan.demoplugin.service.Source
import javax.swing.JComponent
import javax.swing.JPanel

class GoModuleSettingsConfigurable(private val project: Project) : Configurable {
    private val sourceBox = com.intellij.openapi.ui.ComboBox(Source.values())
    private val goPathField = JBTextField()
    private val apiKeyField = JBTextField()
    private var panel: JPanel? = null

    override fun createComponent(): JComponent {
        val state = GoModuleSettingsState.getInstance(project).state
        sourceBox.selectedItem = Source.valueOf(state.defaultSource)
        goPathField.text = state.goPath
        apiKeyField.text = state.librariesApiKey
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("默认来源", sourceBox)
            .addLabeledComponent("go命令路径", goPathField)
            .addLabeledComponent("Libraries.io API Key", apiKeyField)
            .panel
        return panel as JComponent
    }

    override fun isModified(): Boolean {
        val state = GoModuleSettingsState.getInstance(project).state
        return sourceBox.item.name != state.defaultSource || goPathField.text != state.goPath || apiKeyField.text != state.librariesApiKey
    }

    override fun apply() {
        val state = GoModuleSettingsState.getInstance(project).state
        state.defaultSource = sourceBox.item.name
        state.goPath = goPathField.text.trim().ifEmpty { "go" }
        state.librariesApiKey = apiKeyField.text.trim()
    }

    override fun getDisplayName(): String = "Go Module Finder"
}
