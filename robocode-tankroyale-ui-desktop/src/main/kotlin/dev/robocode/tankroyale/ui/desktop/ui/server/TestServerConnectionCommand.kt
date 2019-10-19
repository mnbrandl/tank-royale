package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import dev.robocode.tankroyale.ui.desktop.util.ICommand
import kotlinx.serialization.ImplicitReflectionSerializer
import java.io.Closeable
import javax.swing.JOptionPane

class TestServerConnectionCommand(private val serverUrl: String) : ICommand {

    val onCompleted = Event<Unit>()

    private val disposables = ArrayList<Closeable>()

    @ImplicitReflectionSerializer
    override fun execute() {
        dispose()

        // Cleanup when connected, disconnected or error occurs
        disposables += Client.onConnected.subscribe { complete() }
        disposables += Client.onDisconnected.subscribe { complete() }
        disposables += Client.onError.subscribe { complete() }

        ConnectToServerCommand(serverUrl).execute()
    }

    private fun complete() {
        onCompleted.publish(Unit)
        dispose()
    }

    private fun dispose() {
        disposables.forEach { it.close() }
    }
}