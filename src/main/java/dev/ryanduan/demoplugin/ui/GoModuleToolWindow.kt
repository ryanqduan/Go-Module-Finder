package dev.ryanduan.demoplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import javax.swing.JButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import dev.ryanduan.demoplugin.service.ModuleInfo
import dev.ryanduan.demoplugin.service.ModuleQuery
import dev.ryanduan.demoplugin.service.ModuleSearchService
import dev.ryanduan.demoplugin.service.Source
import dev.ryanduan.demoplugin.exec.GoDownloader
import dev.ryanduan.demoplugin.settings.GoModuleSettingsState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.openapi.ui.popup.JBPopupFactory
import javax.swing.ListSelectionModel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.table.DefaultTableModel

class GoModuleToolWindow(private val project: Project) {
    private lateinit var nameAuto: TextFieldWithAutoCompletion<String>
    private val nameFieldProvider = dev.ryanduan.demoplugin.completion.ModuleNameCompletionProvider { nameAuto.text }
    private val authorField = JBTextField()
    private val versionField = JBTextField()
    private val sourceBox = ComboBox(Source.values())
    private val searchButton = JButton("搜索")
    private val downloadButton = JButton("下载")
    private val tableModel = DefaultTableModel(arrayOf("模块", "最新版本", "来源"), 0)
    private val table = JBTable(tableModel)
    private val selectedVersions = mutableMapOf<String, String>()
    private var latestResults: List<ModuleInfo> = emptyList()

    val component: JPanel = JBPanel<JBPanel<*>>()
    private val searchService = ModuleSearchService()

    init {
        nameAuto = TextFieldWithAutoCompletion(project, nameFieldProvider, false, "")
        val form = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("模块名称"), nameAuto, 1, false)
            .addLabeledComponent(JBLabel("作者"), authorField, 1, false)
            .addLabeledComponent(JBLabel("版本号"), versionField, 1, false)
            .addLabeledComponent(JBLabel("来源"), sourceBox, 1, false)
            .addComponent(com.intellij.util.ui.JBUI.Panels.simplePanel(searchButton).addToRight(downloadButton))
            .panel
        form.border = JBUI.Borders.empty(8)
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        val listPanel = JScrollPane(table)
        val root = JBPanel<JBPanel<*>>()
        root.layout = com.intellij.util.ui.JBUI.Panels.simplePanel().layout
        root.add(form, java.awt.BorderLayout.NORTH)
        root.add(listPanel, java.awt.BorderLayout.CENTER)
        component.layout = java.awt.BorderLayout()
        component.add(root, java.awt.BorderLayout.CENTER)

        val defSrc = GoModuleSettingsState.getInstance(project).state.defaultSource
        sourceBox.selectedItem = Source.valueOf(defSrc)
        searchButton.addActionListener {
            doSearch()
        }
        downloadButton.addActionListener {
            doDownload()
        }
        table.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                val row = table.rowAtPoint(e.point)
                val col = table.columnAtPoint(e.point)
                if (row >= 0 && col == 1) {
                    val module = tableModel.getValueAt(row, 0).toString()
                    loadAndShowVersions(module, row)
                }
            }
        })
        nameAuto.addKeyListener(object : java.awt.event.KeyAdapter() {
            override fun keyReleased(e: java.awt.event.KeyEvent) {
                nameFieldProvider.refresh()
            }
        })
    }

    private fun doSearch() {
        val query = ModuleQuery(
            name = nameAuto.text.trim().ifEmpty { null },
            author = authorField.text.trim().ifEmpty { null },
            version = versionField.text.trim().ifEmpty { null },
            source = sourceBox.item,
            limit = 50
        )
        object : Task.Backgroundable(project, "搜索 Go 模块", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "搜索中"
                val results = searchService.search(query)
                ApplicationManager.getApplication().invokeLater {
                    tableModel.rowCount = 0
                    results.forEach { addRow(it) }
                    latestResults = results
                }
                // 使用 libraries.io 返回的 latest_release_number 已填充 latest，无需再查 proxy；如需补全缺失项，可在此添加二次查询
            }
        }.queue()
    }

    private fun addRow(info: ModuleInfo) {
        tableModel.addRow(arrayOf(info.module, info.latest ?: "", info.source.display))
    }

    private fun doDownload() {
        val rows = table.selectedRows
        if (rows.isEmpty()) return
        rows.forEach { r ->
            val module = tableModel.getValueAt(r, 0).toString()
            val latest = tableModel.getValueAt(r, 1).toString().ifEmpty { null }
            val override = selectedVersions[module]
            val version = versionField.text.trim().ifEmpty { override ?: latest }
            GoDownloader.download(project, module, version, sourceBox.item)
        }
    }

    private fun loadAndShowVersions(module: String, row: Int) {
        val info = latestResults.firstOrNull { it.module == module }
        val versions = info?.versions ?: emptyList()
        if (versions.isEmpty()) return
        val popup = JBPopupFactory.getInstance()
            .createPopupChooserBuilder(versions)
            .setTitle("选择版本")
            .setResizable(false)
            .setItemChosenCallback { chosen ->
                selectedVersions[module] = chosen
                tableModel.setValueAt(chosen, row, 1)
            }
            .createPopup()
        popup.showInFocusCenter()
    }
}
