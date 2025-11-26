package dev.ryanduan.gomodulefinder

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE: String = "messages.GoModuleBundle"

object GoModuleBundle : DynamicBundle(BUNDLE) {
    @JvmStatic
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String = getMessage(key, *params)
}
