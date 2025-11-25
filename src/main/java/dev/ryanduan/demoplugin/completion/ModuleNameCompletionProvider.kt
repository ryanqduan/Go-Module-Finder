package dev.ryanduan.demoplugin.completion

import com.intellij.ui.TextFieldWithAutoCompletion
import dev.ryanduan.demoplugin.service.ModuleQuery
import dev.ryanduan.demoplugin.service.ModuleSearchService
import dev.ryanduan.demoplugin.service.Source
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ModuleNameCompletionProvider(private val textProvider: () -> String) : TextFieldWithAutoCompletion.StringsCompletionProvider(emptyList(), null) {
    private val service = ModuleSearchService()
    private var future: ScheduledFuture<*>? = null

    fun refresh() {
        val q = textProvider().trim()
        if (q.isEmpty()) return
        future?.cancel(false)
        future = AppExecutorUtil.getAppScheduledExecutorService().schedule({
            val list = service.search(ModuleQuery(name = q, author = null, version = null, source = Source.GODOC, limit = 20))
            val items = list.map { it.module }
            ApplicationManager.getApplication().invokeLater {
                setItems(items)
            }
        }, 300, TimeUnit.MILLISECONDS)
    }
}
