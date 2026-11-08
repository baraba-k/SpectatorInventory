package com.dodster.spectatorinv;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SpectatorInvCommands {

    private static final String TARGET = "player";
    private static final String ITEM = "item";
    private static final String AMOUNT = "amount";
    private static final String SOURCE = "source";

    private SpectatorInvCommands() {
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("spinvsee")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(TARGET, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> openInventory(ctx, false))));
        dispatcher.register(Commands.literal("spinventorysee")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(TARGET, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> openInventory(ctx, false))));

        dispatcher.register(Commands.literal("endersee")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(TARGET, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> openInventory(ctx, true))));
        dispatcher.register(Commands.literal("enderchestsee")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(TARGET, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> openInventory(ctx, true))));

        dispatcher.register(Commands.literal("spinvclear")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(TARGET, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> clearHandler(ctx, false))
                        .then(Commands.argument(ITEM, StringArgumentType.word())
                                .executes(ctx -> clearHandler(ctx, false))
                                .then(Commands.argument(AMOUNT, IntegerArgumentType.integer(1))
                                        .executes(ctx -> clearHandler(ctx, false))))));

        dispatcher.register(Commands.literal("enderclear")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(TARGET, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> clearHandler(ctx, true))
                        .then(Commands.argument(ITEM, StringArgumentType.word())
                                .executes(ctx -> clearHandler(ctx, true))
                                .then(Commands.argument(AMOUNT, IntegerArgumentType.integer(1))
                                        .executes(ctx -> clearHandler(ctx, true))))));

        dispatcher.register(Commands.literal("spinvclone")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(SOURCE, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> cloneHandler(ctx, false))
                        .then(Commands.argument(TARGET, StringArgumentType.word())
                                .suggests(SpectatorInvCommands::suggestPlayers)
                                .executes(ctx -> cloneHandler(ctx, false)))));

        dispatcher.register(Commands.literal("enderclone")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(SOURCE, StringArgumentType.word())
                        .suggests(SpectatorInvCommands::suggestPlayers)
                        .executes(ctx -> cloneHandler(ctx, true))
                        .then(Commands.argument(TARGET, StringArgumentType.word())
                                .suggests(SpectatorInvCommands::suggestPlayers)
                                .executes(ctx -> cloneHandler(ctx, true)))));

        dispatcher.register(Commands.literal("spinvreload")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Spectator Inventory configuration reloaded."), false);
                    return 1;
                }));
        dispatcher.register(Commands.literal("isppreload")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Spectator Inventory configuration reloaded."), false);
                    return 1;
                }));
    }

    private static int openInventory(CommandContext<CommandSourceStack> ctx, boolean ender) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer spectator = source.getPlayerOrException();
        String argument = StringArgumentType.getString(ctx, TARGET);
        Optional<SpectatorInvPlayers.Target> target = SpectatorInvPlayers.resolve(
                source.getServer(), argument, ModConfig.UNKNOWN_PLAYER_SUPPORT.get());
        if (target.isEmpty()) {
            source.sendFailure(Component.literal("Player " + argument + " does not exist or has never played here."));
            return 0;
        }
        SpectatorInvPlayers.Target resolved = target.get();
        if (resolved.online() == null && !ModConfig.OFFLINE_PLAYER_SUPPORT.get()) {
            source.sendFailure(Component.literal("Spectating offline players is disabled."));
            return 0;
        }
        if (ender) {
            SpectatorInvActions.openEnderChest(spectator, resolved);
        } else {
            SpectatorInvActions.openMainInventory(spectator, resolved);
        }
        return 1;
    }

    private static int clearHandler(CommandContext<CommandSourceStack> ctx, boolean ender) {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();
        SpectatorInvPlayers.Target target = resolveOrFail(source, server, StringArgumentType.getString(ctx, TARGET));
        if (target == null) return 0;

        Item item = null;
        try {
            item = SpectatorInvActions.parseItem(StringArgumentType.getString(ctx, ITEM));
            if (item == null) {
                source.sendFailure(Component.literal("Unknown item: " + StringArgumentType.getString(ctx, ITEM)));
                return 0;
            }
        } catch (IllegalArgumentException ignored) {
        }

        int amount = optionalAmount(ctx);

        SpectatorInvPlayers.InventoryHandle handle = SpectatorInvPlayers.handle(server, target);
        int removed = ender
                ? SpectatorInvActions.clearEnder(handle, item, amount)
                : SpectatorInvActions.clearMain(handle, item, amount);
        handle.save();

        source.sendSuccess(() -> Component.literal("Removed " + removed + " item(s) from " + target.name() + "."), false);
        return 1;
    }

    private static int cloneHandler(CommandContext<CommandSourceStack> ctx, boolean ender) {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();
        SpectatorInvPlayers.Target sourceTarget = resolveOrFail(source, server, StringArgumentType.getString(ctx, SOURCE));
        if (sourceTarget == null) return 0;

        SpectatorInvPlayers.Target target;
        try {
            target = resolveOrFail(source, server, StringArgumentType.getString(ctx, TARGET));
        } catch (IllegalArgumentException e) {
            if (source.getEntity() instanceof ServerPlayer player) {
                target = new SpectatorInvPlayers.Target(player.getUUID(), player.getGameProfile().getName(), player);
            } else {
                source.sendFailure(Component.literal("The command must be run by a player when no target is given."));
                return 0;
            }
        }
        if (target == null) return 0;
        final SpectatorInvPlayers.Target targetFinal = target;

        SpectatorInvPlayers.InventoryHandle from = SpectatorInvPlayers.handle(server, sourceTarget);
        SpectatorInvPlayers.InventoryHandle to = SpectatorInvPlayers.handle(server, targetFinal);
        if (ender) {
            SpectatorInvActions.cloneEnder(from, to);
        } else {
            SpectatorInvActions.cloneMain(from, to);
        }
        to.save();

        source.sendSuccess(() -> Component.literal("Copied " + sourceTarget.name() + "'s data to " + targetFinal.name() + "."), false);
        return 1;
    }

    private static SpectatorInvPlayers.Target resolveOrFail(CommandSourceStack source, MinecraftServer server, String argument) {
        Optional<SpectatorInvPlayers.Target> target = SpectatorInvPlayers.resolve(server, argument, ModConfig.UNKNOWN_PLAYER_SUPPORT.get());
        if (target.isEmpty()) {
            source.sendFailure(Component.literal("Player " + argument + " does not exist or has never played here."));
            return null;
        }
        SpectatorInvPlayers.Target resolved = target.get();
        if (resolved.online() == null && !ModConfig.OFFLINE_PLAYER_SUPPORT.get()) {
            source.sendFailure(Component.literal("Working with offline players is disabled."));
            return null;
        }
        return resolved;
    }

    private static int optionalAmount(CommandContext<CommandSourceStack> ctx) {
        try {
            return IntegerArgumentType.getInteger(ctx, AMOUNT);
        } catch (IllegalArgumentException e) {
            return Integer.MIN_VALUE;
        }
    }

    private static CompletableFuture<Suggestions> suggestPlayers(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    }
}