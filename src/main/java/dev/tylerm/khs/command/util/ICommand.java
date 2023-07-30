package dev.tylerm.khs.command.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICommand {

	void execute(Player sender, String[] args);

	String getLabel();

	String getUsage();

	String getDescription();

	List<String> autoComplete(@NotNull String parameter, @NotNull String typed);

}
