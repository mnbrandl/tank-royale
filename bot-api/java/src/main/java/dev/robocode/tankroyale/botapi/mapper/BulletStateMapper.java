package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BulletState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Utility class for mapping a bot state. */
public final class BulletStateMapper {

  public static BulletState map(final dev.robocode.tankroyale.schema.BulletState source) {
    return new BulletState(
        source.getBulletId(),
        source.getOwnerId(),
        source.getPower(),
        source.getX(),
        source.getY(),
        source.getDirection(),
        source.getSpeed(),
        source.getColor());
  }

  public static Set<BulletState> map(final Collection<dev.robocode.tankroyale.schema.BulletState> source) {
    Set<BulletState> bulletStates = new HashSet<>();
    source.forEach(bulletState -> bulletStates.add(map(bulletState)));
    return bulletStates;
  }
}