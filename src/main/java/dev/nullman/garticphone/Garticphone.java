package dev.nullman.garticphone;

import dev.nullman.garticphone.commands.Commands;
import dev.nullman.garticphone.objects.GameObject;
import dev.nullman.garticphone.utils.WorldUtil;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

@Getter
public final class Garticphone extends JavaPlugin {

    @Getter
    private static Garticphone instance;

    private GameObject gameObject;
    private WorldUtil worldUtil;

    private Lamp<BukkitCommandActor> lamp;

    @Override
    public void onEnable() {
        instance = this;

        this.worldUtil = new WorldUtil("garticphone");
        this.gameObject = new GameObject();

        lamp = BukkitLamp.builder(this).build();

        lamp.register(new Commands(), this);

        this.getLogger().info("Garticphone plugin enabled!");
    }

    @Override
    public void onDisable() {

    }
}
