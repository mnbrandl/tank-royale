package dev.robocode.tankroyale.bot;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;

@SuppressWarnings("UnusedDeclaration")
public class TestBot2 extends Bot {

  public static void main(String[] args) {
    new TestBot2().start();
  }

  private Double targetX;
  private Double targetY;
  private double move = 200;

  private TestBot2() {
    super();
  }

  @Override
  public void onConnected(ConnectedEvent event) {
    System.out.println("onConnected");
  }

  @Override
  public void onDisconnected(DisconnectedEvent event) {
    System.out.println("onDisconnected");
  }

  @Override
  public void onGameStarted(GameStartedEvent event) {
    System.out.println("onGameStarted: " + event);

    setMaxGunTurnRate(4);
    setMaxRadarTurnRate(4);
    setMaxSpeed(4);

    setTurnRadarLeft(Double.POSITIVE_INFINITY);

    setForward(move);
    go();
  }

  public void run() {
    while (isRunning()) {
      forward(100);
      turnGunLeft(360);
      back(100);
      turnGunRight(360);
    }
  }

  @Override
  public void onScannedBot(ScannedBotEvent event) {
    fire(1);
  }

  @Override
  public void onHitWall(BotHitWallEvent event) {
    move = -move;
    setForward(move);
  }

  @Override
  public void onHitBot(BotHitBotEvent event) {
    move = -move;
    setForward(move);
  }
}
