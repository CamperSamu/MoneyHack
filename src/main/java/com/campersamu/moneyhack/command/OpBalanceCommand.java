package com.campersamu.moneyhack.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static io.github.gunpowder.modelhandlers.BalanceHandler.INSTANCE;
import static java.math.BigDecimal.valueOf;
import static me.lucko.fabric.api.permissions.v0.Permissions.check;
import static me.lucko.fabric.api.permissions.v0.Permissions.require;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.util.Formatting.BOLD;
import static net.minecraft.util.Formatting.GREEN;

public class OpBalanceCommand {
    private static final Text addText = new LiteralText("$ got added to your balance!").formatted(GREEN);
    private static final MutableText setText = new LiteralText("Your balance got set to ").formatted(GREEN);
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.getRoot().addChild(
                    literal("opbalance")
                            .requires(require("moneyhack.command.opbalance", 4))
                            .then(
                                    literal("add")
                                            .requires(require("moneyhack.command.opbalance.add", 4))
                                            .then(
                                                    argument("player", player()).then(argument("value", doubleArg()).executes(OpBalanceCommand::addCommand))
                                            )
                            ).then(
                                    literal("set")
                                            .requires(require("moneyhack.command.opbalance.set", 4))
                                            .then(
                                                    argument("player", player()).then(argument("value", doubleArg()).executes(OpBalanceCommand::setCommand))
                                            )
                            )
                            .build()
            );
        });
    }

    private static int addCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = getPlayer(context, "player");
        var value = getDouble(context, "value");
        var playerUUID = player.getUuid();

        //updating balance
        var storedBalance = INSTANCE.getUser(playerUUID);
        storedBalance.setBalance(storedBalance.getBalance().add(valueOf(value)));
        INSTANCE.modifyUser(playerUUID, bal -> storedBalance);

        //feedback
        var message = new LiteralText(""+value).formatted(GREEN, BOLD).append(addText);
        player.sendMessage(message, false);
        context.getSource().sendFeedback(new LiteralText(player.getEntityName() + ": ").append(message), true);

        return 0;
    }

    private static int setCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = getPlayer(context, "player");
        var value = getDouble(context, "value");
        var playerUUID = player.getUuid();

        //setting balance
        INSTANCE.modifyUser(playerUUID, storedBalance -> {
            storedBalance.setBalance(valueOf(value));
            return storedBalance;
        });

        //feedback
        var message = setText.append(new LiteralText(""+value).formatted(GREEN, BOLD));
        player.sendMessage(message, false);
        context.getSource().sendFeedback(new LiteralText(player.getEntityName() + ": ").append(message), true);

        return 0;
    }
}
