package com.pietro.mcecapes;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * Client-side command (never sent to the server):
 *   /cape            -> list capes
 *   /cape <name>     -> equip a cape (e.g. /cape mce)
 *   /cape off        -> remove the custom cape
 *   /cape reload     -> reload textures from .minecraft/config/mcecapes
 *   /cape folder     -> show the textures folder path
 */
public class CapeCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "cape";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("capa", "mcecape");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cape [nome|off|reload|folder]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendList(sender);
            return;
        }
        String arg = args[0].toLowerCase();
        if (arg.equals("off") || arg.equals("none") || arg.equals("desligar")) {
            CapeManager.disable();
            msg(sender, EnumChatFormatting.YELLOW + "Capa removida.");
        } else if (arg.equals("reload")) {
            CapeManager.reload();
            msg(sender, EnumChatFormatting.GREEN + "Capas recarregadas: " + CapeManager.getNames());
        } else if (arg.equals("folder") || arg.equals("pasta")) {
            msg(sender, EnumChatFormatting.AQUA + "Pasta das capas: " + CapeManager.getDir());
        } else if (arg.equals("list") || arg.equals("lista")) {
            sendList(sender);
        } else if (CapeManager.select(arg)) {
            msg(sender, EnumChatFormatting.GREEN + "Capa equipada: " + EnumChatFormatting.WHITE + arg
                    + EnumChatFormatting.GRAY + " (so voce ve essa capa)");
        } else {
            msg(sender, EnumChatFormatting.RED + "Capa '" + arg + "' nao existe. Use /cape para ver a lista.");
        }
    }

    private void sendList(ICommandSender sender) {
        String active = CapeManager.getActiveName();
        msg(sender, EnumChatFormatting.GOLD + "=== MCE Capes ===");
        for (String name : CapeManager.getNames()) {
            boolean on = name.equals(active);
            msg(sender, (on ? EnumChatFormatting.GREEN + " > " : EnumChatFormatting.GRAY + "   ") + name
                    + (on ? " (equipada)" : ""));
        }
        msg(sender, EnumChatFormatting.GRAY + "Uso: /cape <nome> | /cape off | /cape reload | /cape folder");
    }

    private void msg(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            List<String> options = new java.util.ArrayList<String>(CapeManager.getNames());
            options.add("off");
            options.add("reload");
            options.add("folder");
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        return null;
    }
}
