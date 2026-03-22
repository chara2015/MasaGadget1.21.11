package com.plusls.carpet.util;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.util.Optional;

public class CarpetHelper
{
	private static final String FABRIC_CARPET_MOD_ID = "carpet";

	public static Optional<Object> getCarpetRule(String ruleName)
	{
		if (!FabricLoader.getInstance().isModLoaded(FABRIC_CARPET_MOD_ID))
		{
			return Optional.empty();
		}
		try
		{
			Class<?> clazz = Class.forName("carpet.CarpetServer");
			Object settingsManager = clazz.getDeclaredField("settingsManager").get(null);
			Method getRule = settingsManager.getClass().getMethod("getRule", String.class);
			Object rule = getRule.invoke(settingsManager, ruleName);
			return Optional.ofNullable(rule);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static boolean getBoolRuleValue(String ruleName)
	{
		return getCarpetRule(ruleName).
				map(rule -> {
					try
					{
						Method getBoolValue = rule.getClass().getMethod("getBoolValue");
						return (boolean)getBoolValue.invoke(rule);
					}
					catch (Exception e)
					{
						return false;
					}
				}).
				orElse(false);
	}
}
