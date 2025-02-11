package com.teamresourceful.resourcefullib.common.recipe.ingredient.forge;

import com.teamresourceful.resourcefullib.common.recipe.ingredient.CodecIngredient;
import com.teamresourceful.resourcefullib.common.recipe.ingredient.CodecIngredientSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.HashMap;
import java.util.Map;

public final class ForgeIngredientHelper {

    private static final Map<ResourceLocation, ForgeIngredientSerializer<?>> SERIALIZERS = new HashMap<>();

    public static ForgeIngredientSerializer<?> get(ResourceLocation id) {
        return SERIALIZERS.get(id);
    }

    public static <T extends CodecIngredient<T>> void register(CodecIngredientSerializer<T> serializer) {
        ForgeIngredientSerializer<T> forgeSerializer = new ForgeIngredientSerializer<>(serializer);
        SERIALIZERS.put(serializer.id(), forgeSerializer);
        CraftingHelper.register(serializer.id(), forgeSerializer);
    }
}
