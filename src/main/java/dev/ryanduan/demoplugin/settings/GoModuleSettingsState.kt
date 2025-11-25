package dev.ryanduan.demoplugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
@State(name = "GoModuleSettings", storages = [Storage("go-module-settings.xml")])
class GoModuleSettingsState : PersistentStateComponent<GoModuleSettingsState.State> {
    class State {
        var defaultSource: String = "OFFICIAL"
        var goPath: String = "go"
        var librariesApiKey: String = "e2a7a629051f43c44cd839f7033bd245"
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(project: com.intellij.openapi.project.Project): GoModuleSettingsState =
            project.getService(GoModuleSettingsState::class.java)
    }
}
