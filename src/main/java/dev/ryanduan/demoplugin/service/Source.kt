package dev.ryanduan.demoplugin.service

enum class Source(val display: String, val proxy: String?) {
    OFFICIAL("官方仓库", "https://proxy.golang.org"),
    GOPROXY_IO("goproxy.io", "https://goproxy.io"),
    GOPROXY_CN("goproxy.cn", "https://goproxy.cn"),
    GODOC("godoc", null)
}

