package dev.ryanduan.demoplugin

import dev.ryanduan.demoplugin.service.ModuleQuery
import dev.ryanduan.demoplugin.service.ModuleSearchService
import dev.ryanduan.demoplugin.service.Source
import org.junit.Assert.assertTrue
import org.junit.Test
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class ModuleSearchServiceTest {
    @Test
    fun testGodocSearch() {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/search") { ex ->
            val body = "{" +
                    "\"results\": [" +
                    "{" + "\"import_path\": \"github.com/gin-gonic/gin\"}" +
                    "]}"
            val bytes = body.toByteArray()
            ex.sendResponseHeaders(200, bytes.size.toLong())
            ex.responseBody.use { it.write(bytes) }
        }
        server.start()
        val port = server.address.port
        System.setProperty("gomodule.godocBase", "http://localhost:$port")
        val service = ModuleSearchService()
        val list = service.search(ModuleQuery(name = "gin", author = null, version = null, source = Source.GODOC, limit = 5))
        server.stop(0)
        assertTrue(list.any { it.module.contains("github.com/gin-gonic/gin") })
    }
}

