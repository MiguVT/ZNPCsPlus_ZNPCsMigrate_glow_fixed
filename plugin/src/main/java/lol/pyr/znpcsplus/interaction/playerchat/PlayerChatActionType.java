package lol.pyr.znpcsplus.interaction.playerchat;

import lol.pyr.director.adventure.command.CommandContext;
import lol.pyr.director.common.command.CommandExecutionException;
import lol.pyr.znpcsplus.api.interaction.InteractionAction;
import lol.pyr.znpcsplus.api.interaction.InteractionActionType;
import lol.pyr.znpcsplus.api.interaction.InteractionType;
import lol.pyr.znpcsplus.interaction.InteractionCommandHandler;
import lol.pyr.znpcsplus.scheduling.TaskScheduler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class PlayerChatActionType implements InteractionActionType<PlayerChatAction>, InteractionCommandHandler {
    private final TaskScheduler scheduler;

    public PlayerChatActionType(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public String serialize(PlayerChatAction obj) {
        return Base64.getEncoder().encodeToString(obj.getMessage().getBytes(StandardCharsets.UTF_8)) + ";" + obj.getCooldown() + ";" + obj.getInteractionType().name() + ";" + obj.getDelay();
    }

    @Override
    public PlayerChatAction deserialize(String str) {
        String[] split = str.split(";");
        return new PlayerChatAction(scheduler, new String(Base64.getDecoder().decode(split[0]), StandardCharsets.UTF_8), InteractionType.valueOf(split[2]), Long.parseLong(split[1]), Long.parseLong(split.length > 3 ? split[3] : "0"));
    }

    @Override
    public Class<PlayerChatAction> getActionClass() {
        return PlayerChatAction.class;
    }

    @Override
    public String getSubcommandName() {
        return "playerchat";
    }

    @Override
    public void appendUsage(CommandContext context) {
        context.setUsage(context.getUsage() + " " + getSubcommandName() + " <id> <click type> <cooldown seconds> <delay ticks> <message>");
    }

    @Override
    public InteractionAction parse(CommandContext context) throws CommandExecutionException {
        InteractionType type = context.parse(InteractionType.class);
        long cooldown = (long) (context.parse(Double.class) * 1000D);
        long delay = (long) (context.parse(Integer.class) * 1D);
        String message = context.dumpAllArgs();
        return new PlayerChatAction(scheduler, message, type, cooldown, delay);
    }

    @Override
    public List<String> suggest(CommandContext context) throws CommandExecutionException {
        if (context.argSize() == 1) return context.suggestEnum(InteractionType.values());
        if (context.argSize() == 2) return context.suggestLiteral("1");
        if (context.argSize() == 3) return context.suggestLiteral("0");
        return Collections.emptyList();
    }
}
