package dev.ryanduan.gomodulefinder

import dev.ryanduan.gomodulefinder.service.ModuleQuery
import dev.ryanduan.gomodulefinder.service.ModuleSearchService
import dev.ryanduan.gomodulefinder.service.Source
import org.junit.Assert.assertTrue
import org.junit.Test
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class ModuleSearchServiceTest {
    @Test
    fun testLibrariesSearchLocal() {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/search") { ex ->
            val body = "[" +
                    "{" +
                    "\"name\": \"github.com/gin-gonic/gin\"," +
                    "\"platform\": \"Go\"," +
                    "\"latest_release_number\": \"v1.9.0\"" +
                    "}" +
                    "]"
            val bytes = body.toByteArray()
            ex.sendResponseHeaders(200, bytes.size.toLong())
            ex.responseBody.use { it.write(bytes) }
        }
        server.start()
        val port = server.address.port
        System.setProperty("gomodule.librariesBase", "http://localhost:$port")
        val service = ModuleSearchService()
        val list = service.search(ModuleQuery(name = "gin", author = null, version = null, source = Source.OFFICIAL, limit = 5))
        server.stop(0)
        assertTrue(list.any { it.module.contains("github.com/gin-gonic/gin") })
    }
}
