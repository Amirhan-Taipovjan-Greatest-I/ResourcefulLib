package com.teamresourceful.resourcefullib.forge;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.teamresourceful.resourcefullib.common.lib.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ForgeResourcePackHandler {

    private static final List<ResourcePack> RESOURCE_PACKS = new ArrayList<>();
    private static final String RESOURCE_PACK_KEY = "resourcefullib:resourcepack";

    public static void load() {
        for (IModInfo mod : ModList.get().getMods()) {
            if (mod.getModProperties().containsKey(RESOURCE_PACK_KEY)) {
                try {
                    initMod(mod, mod.getModProperties());
                }catch (Exception e) {
                    Constants.LOGGER.error("Resourceful Lib failed to load resource pack for mod: " + mod.getDisplayName());
                    e.printStackTrace();
                }
            }
        }
    }

    private static void initMod(IModInfo mod, Map<String, Object> metadata) {
        for (Object pack : (List<?>) metadata.get(RESOURCE_PACK_KEY)) {
            loadPack(mod, pack);
        }
    }

    private static void loadPack(IModInfo mod, Object value) {
        if (value instanceof String string) {
            RESOURCE_PACKS.add(new ResourcePack(mod, string, "resourcefullib.resourcepack." + string));
        } else if (value instanceof UnmodifiableConfig config) {
            Map<String, Object> map = config.valueMap();
            String name = getOrThrow(map, "name");
            String description = getOrThrow(map, "description");
            RESOURCE_PACKS.add(new ResourcePack(mod, name, description));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getOrThrow(Map<?, ?> map, String id) {
        if (!map.containsKey(id)) throw new IllegalStateException("Missing key: " + id);
        return (T) map.get(id);
    }

    public static void onRegisterPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        for (ResourcePack resourcePack : RESOURCE_PACKS) {
            try {
                Path resourcePath = resourcePack.mod().getOwningFile()
                    .getFile().findResource("resourcepacks/" + resourcePack.name());

                final Pack pack = Pack.readMetaAndCreate(
                    "builtin/add_pack_finders_test", Component.translatable(resourcePack.description()),
                    false,
                    (path) -> new PathPackResources(path, false, resourcePath),
                    PackType.CLIENT_RESOURCES, Pack.Position.TOP, new PackSource() {
                        @Override
                        public @NotNull Component decorate(@NotNull Component arg) {
                            return PackSource.NO_DECORATION.apply(arg);
                        }

                        @Override
                        public boolean shouldAddAutomatically() {
                            return false;
                        }
                    }
                );
                event.addRepositorySource((source) -> source.accept(pack));
            } catch (Exception ignored) {
                Constants.LOGGER.error("Resourceful Lib failed to init resource pack for mod: " + resourcePack.mod().getDisplayName());
            }
        }
    }

    private record ResourcePack(IModInfo mod, String name, String description) {}
}
