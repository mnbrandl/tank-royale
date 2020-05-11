package dev.robocode.tankroyale.ui.desktop.ui.arena

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.fx.Animation
import dev.robocode.tankroyale.ui.desktop.fx.CircleBurst
import dev.robocode.tankroyale.ui.desktop.fx.Explosion
import dev.robocode.tankroyale.ui.desktop.model.*
import dev.robocode.tankroyale.ui.desktop.ui.ResultsWindow
import dev.robocode.tankroyale.ui.desktop.ui.arena.ArenaPanel.State.arenaHeight
import dev.robocode.tankroyale.ui.desktop.ui.arena.ArenaPanel.State.arenaWidth
import dev.robocode.tankroyale.ui.desktop.ui.arena.ArenaPanel.State.bots
import dev.robocode.tankroyale.ui.desktop.ui.arena.ArenaPanel.addMouseWheelListener
import dev.robocode.tankroyale.ui.desktop.util.Graphics2DState
import java.awt.*
import java.awt.event.MouseWheelEvent
import java.awt.geom.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JPanel
import kotlin.collections.HashSet


object ArenaPanel : JPanel() {

    private var scale = 1.0

    private val circleShape = Area(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))

    private val explosions = Collections.synchronizedList(ArrayList<Animation>())

    private object State {
        var arenaWidth: Int = Client.currentGameSetup?.arenaWidth ?: 800
        var arenaHeight: Int = Client.currentGameSetup?.arenaHeight ?: 600

        var round: Int = 0
        var time: Int = 0
        var bots: Set<BotState> = HashSet()
        var bullets: Set<BulletState> = HashSet()
    }

    private val state = State

    init {
        addMouseWheelListener { e -> if (e != null) onMouseWheel(
            e
        )
        }

        Client.onGameStarted.subscribe {
            onGameStarted(
                it
            )
        }
        Client.onGameEnded.subscribe {
            onGameEnded(
                it
            )
        }
        Client.onTickEvent.subscribe {
            onTick(
                it
            )
        }
    }

    private fun onGameStarted(gameStartedEvent: GameStartedEvent) {
        val setup = gameStartedEvent.gameSetup
        state.arenaWidth = setup.arenaWidth
        state.arenaHeight = setup.arenaHeight
    }

    private fun onGameEnded(gameEndedEvent: GameEndedEvent) {
        ResultsWindow(gameEndedEvent.results).isVisible = true
    }

    private val tick = AtomicBoolean(false)

    private fun onTick(tickEvent: TickEvent) {
        if (tick.get()) return
        tick.set(true)

        if (tickEvent.turnNumber == 1) {
            // Make sure to remove any explosion left from earlier battle
            explosions.clear()
        }

        State.round = tickEvent.roundNumber
        State.time = tickEvent.turnNumber
        state.bots = tickEvent.botStates
        State.bullets = tickEvent.bulletStates

        tickEvent.events.forEach {
            when (it) {
                is BotDeathEvent -> onBotDeath(
                    it
                )
                is BulletHitBotEvent -> onBulletHitBot(
                    it
                )
                is BulletHitWallEvent -> onBulletHitWall(
                    it
                )
                is BulletHitBulletEvent -> onBulletHitBullet(
                    it
                )
            }
        }

        repaint()

        tick.set(false)
    }

    private fun onBotDeath(botDeathEvent: BotDeathEvent) {
        val bot = bots.first { bot -> bot.id == botDeathEvent.victimId }
        val explosion = Explosion(bot.x, bot.y, 80, 50, 15,
            State.time
        )
        explosions.add(explosion)
    }

    private fun onBulletHitBot(bulletHitBotEvent: BulletHitBotEvent) {
        val bullet = bulletHitBotEvent.bullet
        val bot = bots.first { bot -> bot.id == bulletHitBotEvent.victimId }

        val xOffset = bullet.x - bot.x
        val yOffset = bullet.y - bot.y

        val explosion = BotHitExplosion(
            bot.x,
            bot.y,
            xOffset,
            yOffset,
            bot.id,
            4.0,
            40.0,
            25,
            State.time
        )
        explosions.add(explosion)
    }

    private fun onBulletHitWall(bulletHitWallEvent: BulletHitWallEvent) {
        val bullet = bulletHitWallEvent.bullet
        val explosion = CircleBurst(bullet.x, bullet.y, 4.0, 40.0, 25,
            State.time
        )
        explosions.add(explosion)
    }

    private fun onBulletHitBullet(bulletHitBulletEvent: BulletHitBulletEvent) {
        val bullet1 = bulletHitBulletEvent.bullet
        val bullet2 = bulletHitBulletEvent.hitBullet

        val x = (bullet1.x + bullet2.x) / 2
        val y = (bullet1.y + bullet2.y) / 2

        val explosion = CircleBurst(x, y, 4.0, 40.0, 25,
            State.time
        )
        explosions.add(explosion)
    }

    private fun onMouseWheel(e: MouseWheelEvent) {
        var newScale = scale
        if (e.unitsToScroll > 0) {
            newScale *= 1.2
        } else if (e.unitsToScroll < 0) {
            newScale /= 1.2
        }
        if (newScale != scale && newScale >= 0.25 && newScale <= 10) {
            scale = newScale
            repaint()
        }
    }

    override fun paintComponent(g: Graphics) {
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        try {
            drawArena(g)
        } finally {
            g.dispose()
        }
    }

    private fun drawArena(g: Graphics2D) {
        clearCanvas(g)

        val marginX = (size.width - scale * arenaWidth) / 2
        val marginY = (size.height - scale * arenaHeight) / 2

        g.translate(marginX, marginY)
        g.scale(scale, -scale)
        g.translate(0, -arenaHeight)

        drawGround(g)
        drawBots(g)
        drawExplosions(g)
        drawBullets(g)
        drawRoundInfo(g)
    }

    private fun drawBots(g: Graphics2D) {
        state.bots.forEach {
            val x = it.x
            val y = it.y

            Tank(
                x,
                y,
                it.direction,
                it.gunDirection,
                it.radarDirection
            ).paint(g)

            drawScanArc(
                g,
                x,
                y,
                it.radarDirection,
                it.radarSweep,
                Color.WHITE
            )
            drawEnergy(g, x, y, it.energy)
        }
    }

    private fun drawBullets(g: Graphics2D) {
        State.bullets.forEach {
            drawBullet(
                g,
                it.x,
                it.y,
                it.power
            )
        }
    }

    private fun clearCanvas(g: Graphics) {
        g.color = Color.DARK_GRAY
        g.fillRect(0, 0, size.width, size.height)
    }

    private fun drawGround(g: Graphics) {
        g.color = Color.BLACK
        g.fillRect(0, 0, state.arenaWidth, state.arenaHeight)
    }

    private fun drawExplosions(g: Graphics2D) {
        with(explosions.iterator()) {
            forEach { explosion ->
                explosion.paint(g, State.time)
                if (explosion.isFinished()) remove()
            }
        }
    }

    private fun drawBullet(g: Graphics2D, x: Double, y: Double, power: Double) {
        val size = 2 * Math.sqrt(2.5 * power)
        g.color = Color.WHITE
        g.fillCircle(x, y, size)
    }

    private fun drawScanArc(g: Graphics2D, x: Double, y: Double, direction: Double, spreadAngle: Double, color: Color) {
        val oldState = Graphics2DState(g)

        g.color = color
        g.stroke = BasicStroke(1f)
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)

        val arc = Arc2D.Double()

        var startAngle = 360 - direction
        var angleEx = spreadAngle

        if (angleEx < 0) {
            startAngle += angleEx
            angleEx *= -1
        }
        startAngle %= 360

        arc.setArcByCenter(x, y, 1200.0, startAngle, angleEx, Arc2D.PIE)

        if (angleEx >= .5) {
            g.fill(arc)
        } else {
            g.draw(arc)
        }

        oldState.restore(g)
    }

    private fun drawRoundInfo(g: Graphics2D) {
        val oldState = Graphics2DState(g)

        g.scale(1.0, -1.0)
        g.color = Color.YELLOW
        g.drawString("Round ${State.round}, Turn: ${State.time}", 10, 20 - arenaHeight)

        oldState.restore(g)
    }

    private fun drawEnergy(g: Graphics2D, x: Double, y: Double, energy: Double) {
        val oldState = Graphics2DState(g)

        g.color = Color.WHITE
        val text = "%.1f".format(energy)
        val width = g.fontMetrics.stringWidth(text)

        g.scale(1.0, -1.0)
        g.drawString(text, x.toFloat() - width / 2,  (-30 - y).toFloat())

        oldState.restore(g)
    }

    private fun Graphics2D.fillCircle(x: Double, y: Double, size: Double) {
        this.color = color
        val transform = AffineTransform.getTranslateInstance(x, y)
        transform.scale(size, size)
        fill(circleShape.createTransformedArea(transform))
    }

    class BotHitExplosion(
        x: Double,
        y: Double,
        private val xOffset: Double,
        private val yOffset: Double,
        val victimId: Int,
        startRadius: Double,
        endRadius: Double,
        period: Int,
        startTime: Int
    ) : CircleBurst(x, y, startRadius, endRadius, period, startTime) {

        override fun paint(g: Graphics2D, time: Int) {

            val origX = x
            val origY = y

            x += xOffset
            y += yOffset

            super.paint(g, time)

            x = origX
            y = origY
        }
    }
}