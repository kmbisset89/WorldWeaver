package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.GoveeDevice
import io.github.kmbisset89.worldweaver.domain.GoveeLanMessageFactory
import io.github.kmbisset89.worldweaver.domain.GoveeLightCommand
import io.github.kmbisset89.worldweaver.domain.GoveeLightingClient
import io.github.kmbisset89.worldweaver.domain.GoveeScanResponseTransformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException

/**
 * Govee LAN-control translator using UDP scan and per-device commands.
 */
internal class GoveeLanClient(
    private val messageFactory: GoveeLanMessageFactory = GoveeLanMessageFactory(),
    private val scanTransformer: GoveeScanResponseTransformer = GoveeScanResponseTransformer(),
    private val scanMillis: Int = 2_500,
) : GoveeLightingClient {
    override suspend fun scan(): GoveeLightingClient.ScanResult {
        return withContext(Dispatchers.IO) {
            try {
                DatagramSocket(null).use { listen ->
                    listen.reuseAddress = true
                    listen.bind(InetSocketAddress(LISTEN_PORT))
                    listen.soTimeout = 400
                    DatagramSocket().use { sender ->
                        sender.broadcast = true
                        val payload = messageFactory.scan().toByteArray()
                        sender.send(
                            DatagramPacket(
                                payload,
                                payload.size,
                                InetAddress.getByName(MULTICAST_ADDRESS),
                                SCAN_PORT,
                            ),
                        )
                    }
                    val found = linkedMapOf<String, GoveeDevice>()
                    val deadline = System.currentTimeMillis() + scanMillis
                    val buffer = ByteArray(4096)
                    while (System.currentTimeMillis() < deadline) {
                        try {
                            val packet = DatagramPacket(buffer, buffer.size)
                            listen.receive(packet)
                            val text = String(packet.data, 0, packet.length)
                            val device = scanTransformer.transform(text) ?: continue
                            found[device.deviceId] = device
                        } catch (_: SocketTimeoutException) {
                            continue
                        }
                    }
                    if (found.isEmpty()) {
                        GoveeLightingClient.ScanResult.Failed(
                            "No Govee lights answered. Turn on LAN Control in the Govee app.",
                        )
                    } else {
                        GoveeLightingClient.ScanResult.Devices(found.values.toList())
                    }
                }
            } catch (error: Exception) {
                GoveeLightingClient.ScanResult.Failed(
                    error.message ?: "Could not scan for Govee lights",
                )
            }
        }
    }

    override suspend fun apply(
        devices: List<GoveeDevice>,
        command: GoveeLightCommand,
    ): GoveeLightingClient.ApplyResult {
        if (devices.isEmpty()) {
            return GoveeLightingClient.ApplyResult.Failed("No Govee lights are selected")
        }
        return withContext(Dispatchers.IO) {
            try {
                DatagramSocket().use { socket ->
                    devices.forEach { device ->
                        val address = InetAddress.getByName(device.ipAddress)
                        send(socket, address, messageFactory.turn(command.powerOn))
                        if (command.powerOn) {
                            send(socket, address, messageFactory.brightness(command.brightness.coerceIn(1, 100)))
                            send(
                                socket,
                                address,
                                messageFactory.color(command.red, command.green, command.blue),
                            )
                        }
                    }
                }
                GoveeLightingClient.ApplyResult.Applied
            } catch (error: Exception) {
                GoveeLightingClient.ApplyResult.Failed(
                    error.message ?: "Could not reach Govee lights",
                )
            }
        }
    }

    private fun send(socket: DatagramSocket, address: InetAddress, json: String) {
        val bytes = json.toByteArray()
        socket.send(DatagramPacket(bytes, bytes.size, address, CONTROL_PORT))
    }

    private companion object {
        const val MULTICAST_ADDRESS = "239.255.255.250"
        const val SCAN_PORT = 4001
        const val LISTEN_PORT = 4002
        const val CONTROL_PORT = 4003
    }
}
