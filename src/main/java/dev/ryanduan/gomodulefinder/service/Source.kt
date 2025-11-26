package dev.ryanduan.gomodulefinder.service

import dev.ryanduan.gomodulefinder.GoModuleBundle

enum class Source(val displayKey: String, val proxy: String?) {
    OFFICIAL("source.official", "https://proxy.golang.org"),
    GOPROXY_IO("source.goproxyIo", "https://goproxy.io"),
    GOPROXY_CN("source.goproxyCn", "https://goproxy.cn"),
    CUSTOM("source.custom", null),
    GODOC("source.godoc", null);

    fun display(): String = GoModuleBundle.message(displayKey)
}
