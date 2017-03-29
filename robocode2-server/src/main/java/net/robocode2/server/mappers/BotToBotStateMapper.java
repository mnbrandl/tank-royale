package net.robocode2.server.mappers;

import net.robocode2.game.MathUtil;
import net.robocode2.json_schema.states.BotState;
import net.robocode2.model.IBot;

public final class BotToBotStateMapper {

	public static BotState map(IBot bot) {
		BotState botState = new BotState();
		botState.setEnergy(bot.getEnergy());
		botState.setPosition(PointMapper.map(bot.getPosition()));
		botState.setSpeed(bot.getSpeed());
		botState.setDirection(MathUtil.normalAbsoluteAngleDegrees(bot.getDirection()));
		botState.setRadarDirection(MathUtil.normalAbsoluteAngleDegrees(bot.getRadarDirection()));
		botState.setGunDirection(MathUtil.normalAbsoluteAngleDegrees(bot.getGunDirection()));
		botState.setScanArc(ArcMapper.map(bot.getScanArc()));
		return botState;
	}
}