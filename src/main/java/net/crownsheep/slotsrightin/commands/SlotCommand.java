package net.crownsheep.slotsrightin.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class SlotCommand {
    private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType((p_180344_, p_180345_) -> {
        return Component.translatable("commands.item.target.no_changed.known_item", p_180344_, p_180345_);
    });

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("slot").requires((p_180256_) -> p_180256_.hasPermission(2)).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.literal("clear").then(Commands.argument("slot", SlotArgument.slot()).executes((p_180371_) -> {
            return clearSlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), 0, true);
        }).then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                .executes((p_180369_) -> clearSlot(p_180369_.getSource(), EntityArgument.getEntities(p_180369_, "targets"), SlotArgument.getSlot(p_180369_, "slot"), IntegerArgumentType.getInteger(p_180369_, "count"), false)
                )))).then(Commands.literal("get").then(Commands.argument("slot", SlotArgument.slot())
                .executes((p_180371_) -> getSlot(p_180371_.getSource(), EntityArgument.getEntity(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot")
                )))).then(Commands.literal("math").then(Commands.literal("add").then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("amount", IntegerArgumentType.integer())
                .executes((p_180371_) -> addSlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), IntegerArgumentType.getInteger(p_180371_, "amount"))
                )))).then(Commands.literal("subtract").then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("amount", IntegerArgumentType.integer())
                .executes((p_180371_) -> subtractSlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), IntegerArgumentType.getInteger(p_180371_, "amount"))
                )))).then(Commands.literal("multiply").then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("amount", IntegerArgumentType.integer())
                .executes((p_180371_) -> multiplySlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), IntegerArgumentType.getInteger(p_180371_, "amount"))
                )))).then(Commands.literal("divide").then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("amount", IntegerArgumentType.integer())
                .executes((p_180371_) -> divideSlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), IntegerArgumentType.getInteger(p_180371_, "amount"))
                )))).then(Commands.literal("random").then(Commands.argument("slot", SlotArgument.slot())
                .executes((p_180371_) -> setCountRandom(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"))
                )))).then(Commands.literal("set").then(Commands.literal("random").then(Commands.argument("slot", SlotArgument.slot())
                .executes((p_180371_) -> insertRandom(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"))
                ))).then(Commands.literal("count").then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("count", IntegerArgumentType.integer())
                .executes((p_180371_) -> setCount(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), IntegerArgumentType.getInteger(p_180371_, "count"))
                )))).then(Commands.literal("item").then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("item", ItemArgument.item(event.getBuildContext())).then(Commands.argument("count", IntegerArgumentType.integer())
                .executes((p_180371_) -> setSlotItem(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), ItemArgument.getItem(p_180371_, "item").createItemStack(1, false), IntegerArgumentType.getInteger(p_180371_, "count"))
                )))))).then(Commands.literal("copy").then(Commands.argument("slot1", SlotArgument.slot()).then(Commands.argument("slot2", SlotArgument.slot())
                .executes((p_180371_) -> copySlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot1"), SlotArgument.getSlot(p_180371_, "slot2"))
                )))).then(Commands.literal("rename").then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("name", StringArgumentType.string()).then(Commands.argument("italic", BoolArgumentType.bool())
                .executes((p_180371_) -> renameSlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), StringArgumentType.getString(p_180371_, "name"), BoolArgumentType.getBool(p_180371_, "italic"))
                ))))).then(Commands.literal("swap").then(Commands.argument("slot1", SlotArgument.slot()).then(Commands.argument("slot2", SlotArgument.slot())
                .executes((p_180371_) -> swapSlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot1"), SlotArgument.getSlot(p_180371_, "slot2"))
                )))).then(Commands.literal("compare").then(Commands.argument("slot1", SlotArgument.slot()).then(Commands.argument("slot2", SlotArgument.slot())
                .executes((p_180371_) -> compareSlot(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot1"), SlotArgument.getSlot(p_180371_, "slot2")
                ))))))));
    }

    private static int compareSlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot1, int slot2) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        SlotAccess slotAccess = SlotAccess.NULL;
        SlotAccess slotAccess2 = SlotAccess.NULL;
        for (Entity entity : entities) {
            slotAccess = entity.getSlot(slot1);
            slotAccess2 = entity.getSlot(slot2);
            if (slotAccess != SlotAccess.NULL && slotAccess2 != SlotAccess.NULL) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).containerMenu.broadcastChanges();
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot1);
        } else {
            if (slotAccess.get().is(slotAccess2.get().getItem()) || slotAccess2.get().is(slotAccess.get().getItem())) {
                commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.compare.match"), true);
            } else {
                commandSourceStack.sendFailure(Component.translatable("commands.slot.entity.compare.not_match"));
            }
            return list.size();
        }
    }

    private static int copySlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot1, int slot2) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot1);
            SlotAccess slotaccess2 = entity.getSlot(slot2);
            if (slotaccess != SlotAccess.NULL && slotaccess2.set(slotaccess.get())) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).containerMenu.broadcastChanges();
                }
            } else {
                if (slotaccess != SlotAccess.NULL) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot1);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.item.entity.copy"), true);
            return list.size();
        }
    }

    private static int swapSlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot1, int slot2) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot1);
            SlotAccess slotaccess2 = entity.getSlot(slot2);
            ItemStack itemStack = slotaccess2.get();
            if (slotaccess != SlotAccess.NULL && slotaccess2.set(slotaccess.get()) && slotaccess.set(itemStack)) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).containerMenu.broadcastChanges();
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot1);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.swap"), true);
            return list.size();
        }
    }

    private static int setCountRandom(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        int random = 0;
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            random = RandomSource.create().nextInt(1, slotaccess.get().getMaxStackSize() + 1);
            if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(random))) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).containerMenu.broadcastChanges();
                }
            } else {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.math.random").append(String.valueOf(random)), true);
            return list.size();
        }
    }

    private static int setCount(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot, int amount) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (amount <= slotaccess.get().getMaxStackSize()) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(amount))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            } else if (amount > slotaccess.get().getMaxStackSize()) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getMaxStackSize()))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.set.count", list.size(), ItemStack.EMPTY.getDisplayName()), true);
            return list.size();
        }
    }

    private static int renameSlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot, String name, boolean italic) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (italic) {
                slotaccess.set(slotaccess.get().copy().setHoverName(Component.literal(name)));
            } else {
                slotaccess.set(setHoverNameWithoutItalic(slotaccess.get().copy(), Component.literal(name)));
            }
            list.add(entity);
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer) entity).containerMenu.broadcastChanges();
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            if(italic) {
                commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.rename").append(Component.literal(name).withStyle(ChatFormatting.ITALIC)), true);
            } else {
                commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.rename").append(name), true);
            }
            return list.size();
        }
    }

    public static ItemStack setHoverNameWithoutItalic(ItemStack itemStack, @Nullable Component name) {
        CompoundTag compoundtag = itemStack.getOrCreateTagElement("display");
        if (name != null) {
            compoundtag.put("Name", StringTag.valueOf("{\"text\":\"" + name.getString() + "\",\"italic\":false}"));
        } else {
            compoundtag.remove("Name");
        }

        return itemStack;
    }

    //private static int dropSlot(CommandSourceStack commandSourceStack, Entity entity, int slot) throws CommandSyntaxException {
    //    SlotAccess slotaccess = entity.getSlot(slot);
    //    Player player = (Player) entity;
    //    player.drop(slotaccess.get(), true);
    //    slotaccess.set(ItemStack.EMPTY);
    //    if (entity instanceof ServerPlayer) {
    //        ((ServerPlayer) entity).containerMenu.broadcastChanges();
    //    }
    //
    //    if (entity == null) {
    //        throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
    //    } else {
    //        if(slotaccess.get() == ItemStack.EMPTY) {
    //            commandSourceStack.sendFailure(Component.translatable("commands.slot.entity.slot_is_empty"));
    //        } else {
    //            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.drop.success"), true);
    //        }
    //    }
    //    return 1;
    //}

    private static int insertRandom(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        List<Item> itemList = new ArrayList<>(ForgeRegistries.ITEMS.getValues());

        Item randomItem = itemList.get(new Random().nextInt(itemList.size()));
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (slotaccess != SlotAccess.NULL && slotaccess.set(randomItem.getDefaultInstance())) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).containerMenu.broadcastChanges();
                }
            } else {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.set.random").append(randomItem.getDefaultInstance().getDisplayName().copy().withStyle(randomItem.getDefaultInstance().getRarity().color)), true);
            return list.size();
        }
    }

    private static int setSlotItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot, ItemStack itemStack, int amount) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if(amount > itemStack.getMaxStackSize()) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(itemStack.copyWithCount(itemStack.getMaxStackSize()))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                }
            } else {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(itemStack.copyWithCount(amount))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.set.item"), true);
            return list.size();
        }
    }

    private static int addSlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot, int amount) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (slotaccess.get().getCount() + amount <= slotaccess.get().getMaxStackSize()) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getCount() + amount))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            } else if(slotaccess.get().getCount() + amount > slotaccess.get().getMaxStackSize()) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getMaxStackSize()))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.math.add").append(String.valueOf(amount)), true);
            return list.size();
        }
    }

    private static int subtractSlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities,int slot ,int amount) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (slotaccess.get().getCount() - amount >= 0) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getCount() - amount))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            } else if(slotaccess.get().getCount() - amount <= 0) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.math.subtract").append(String.valueOf(amount)), true);
            return list.size();
        }
    }

    private static int multiplySlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot, int amount) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (slotaccess.get().getCount() * amount <= slotaccess.get().getMaxStackSize()) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getCount() * amount))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            } else if (slotaccess.get().getCount() * amount > slotaccess.get().getMaxStackSize()) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getMaxStackSize()))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.math.multiply", amount), true);
            return list.size();
        }
    }

    private static int divideSlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot, int amount) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (slotaccess.get().getCount() / amount > 0) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getCount() / amount))) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            } else if (slotaccess.get().getCount() / amount <= 0) {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                } else {
                    if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                        list.add(entity);
                        if (entity instanceof ServerPlayer) {
                            ((ServerPlayer) entity).containerMenu.broadcastChanges();
                        }
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.math.divide").append(String.valueOf(amount)), true);
            return list.size();
        }
    }

    private static int clearSlot(CommandSourceStack commandSourceStack, Collection<? extends Entity> entities, int slot, int count, boolean all) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(entities.size());
        ItemStack itemStack = ItemStack.EMPTY;
        for (Entity entity : entities) {
            SlotAccess slotaccess = entity.getSlot(slot);
            itemStack = slotaccess.get();
            if (slotaccess != SlotAccess.NULL && !all && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getCount() - count))) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).containerMenu.broadcastChanges();
                }
            } else if (slotaccess != SlotAccess.NULL && all && slotaccess.set(slotaccess.get().copyWithCount(slotaccess.get().getCount() - slotaccess.get().getCount()))) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).containerMenu.broadcastChanges();
                }
            } else {
                if (slotaccess != SlotAccess.NULL && slotaccess.set(ItemStack.EMPTY)) {
                    list.add(entity);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer) entity).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            if (list.size() == 1 && !itemStack.is(Items.AIR)) {
                commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.clear.success.single", list.iterator().next().getDisplayName()), true);
            } else if (list.size() == 1) {
                commandSourceStack.sendFailure(Component.translatable("commands.slot.entity.clear.failure.single", list.iterator().next().getDisplayName()));
            } else {
                commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.clear.success.multiple", list.size()), true);
            }
            return list.size();
        }
    }

    private static int getSlot(CommandSourceStack commandSourceStack, Entity entity, int slot) throws CommandSyntaxException{
        SlotAccess slotaccess = entity.getSlot(slot);
        if (slotaccess != SlotAccess.NULL) {
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer) entity).containerMenu.broadcastChanges();
            }
        }

        if (entity == null) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(Component.empty(), slot);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.slot.entity.get").append(slotaccess.get().getDisplayName().copy().withStyle(slotaccess.get().getRarity().color)), true);
        }
        return 1;
    }
}
