package dev.ryanduan.gomodulefinder.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.ui.SimpleListCellRenderer
import dev.ryanduan.gomodulefinder.GoModuleBundle
import dev.ryanduan.gomodulefinder.service.Source
import javax.swing.JComponent
import javax.swing.JPanel

class GoModuleSettingsConfigurable(private val project: Project) : Configurable {
    private val sourceBox = com.intellij.openapi.ui.ComboBox(Source.values())
    private val goPathField = JBTextField()
    private val apiKeyField = JBTextField()
    private val customProxyField = JBTextField()
    private var panel: JPanel? = null

    override fun createComponent(): JComponent {
        val state = GoModuleSettingsState.getInstance(project).state
        sourceBox.selectedItem = Source.valueOf(state.defaultSource)
        sourceBox.renderer = SimpleListCellRenderer.create("") { it?.display() ?: "" }
        goPathField.text = state.goPath
        apiKeyField.text = state.librariesApiKey
        customProxyField.text = state.customProxy
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(GoModuleBundle.message("go.settings.defaultSource.label"), sourceBox)
            .addLabeledComponent(GoModuleBundle.message("go.settings.goPath.label"), goPathField)
            .addLabeledComponent(GoModuleBundle.message("go.settings.apiKey.label"), apiKeyField)
            .addLabeledComponent(GoModuleBundle.message("go.settings.customProxy.label"), customProxyField)
            .panel
        return panel as JComponent
    }

    override fun isModified(): Boolean {
        val state = GoModuleSettingsState.getInstance(project).state
        return sourceBox.item.name != state.defaultSource ||
                goPathField.text != state.goPath ||
                apiKeyField.text != state.librariesApiKey ||
                customProxyField.text != state.customProxy
    }

    override fun apply() {
        val state = GoModuleSettingsState.getInstance(project).state
        state.defaultSource = sourceBox.item.name
        state.goPath = goPathField.text.trim().ifEmpty { "go" }
        state.librariesApiKey = apiKeyField.text.trim()
        state.customProxy = customProxyField.text.trim()
    }

    override fun getDisplayName(): String = GoModuleBundle.message("go.settings.displayName")
}
