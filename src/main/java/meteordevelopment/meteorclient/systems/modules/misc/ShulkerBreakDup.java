/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.hit.BlockHitResult;

public class ShulkerBreakDup extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("disconnect-delay-ms")
        .description("Milliseconds between shulker break and client exit.")
        .defaultValue(950)
        .min(300)
        .sliderMax(2000)
        .build());

    private long triggerAt = 0;

    public ShulkerBreakDup() {
        super(Categories.Misc, "shulker-break-dup", "Disconnects at the exact tick shulker is broken to duplicate items.");
    }

    @Override
    public void onDeactivate() {
        triggerAt = 0;
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (triggerAt > 0 && System.currentTimeMillis() >= triggerAt) {
            mc.stop();
            triggerAt = 0;
            return;
        }

        ClientPlayerInteractionManager im = mc.interactionManager;
        if (im == null) return;

        float progress = ((ClientPlayerInteractionManagerAccessor) im).getBreakingProgress();
        if (Math.round(progress * 10f) != 10) return;

        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;
        if (!(mc.world.getBlockEntity(bhr.getBlockPos()) instanceof ShulkerBoxBlockEntity)) return;

        triggerAt = System.currentTimeMillis() + delay.get();
    }
}
