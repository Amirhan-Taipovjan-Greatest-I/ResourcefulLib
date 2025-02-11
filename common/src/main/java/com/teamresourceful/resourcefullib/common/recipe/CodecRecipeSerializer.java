package com.teamresourceful.resourcefullib.common.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.teamresourceful.resourcefullib.common.lib.Constants;
import com.teamresourceful.resourcefullib.common.networking.PacketHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class CodecRecipeSerializer<R extends Recipe<?>> implements RecipeSerializer<R> {

    private final RecipeType<R> recipeType;
    private final Function<ResourceLocation, Codec<R>> jsonCodec;
    private final Function<ResourceLocation, Codec<R>> networkCodec;

    public CodecRecipeSerializer(RecipeType<R> recipeType, Function<ResourceLocation, Codec<R>> codec) {
        this(recipeType, codec, codec);
    }

    public CodecRecipeSerializer(RecipeType<R> recipeType, Function<ResourceLocation, Codec<R>> jsonCodec, Function<ResourceLocation, Codec<R>> networkCodec) {
        this.recipeType = recipeType;
        this.jsonCodec = jsonCodec;
        this.networkCodec = networkCodec;
    }

    @Override
    public @NotNull R fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        return jsonCodec.apply(id).parse(JsonOps.INSTANCE, json)
                .getOrThrow(false, s -> Constants.LOGGER.error("Could not parse {}", id));
    }

    @Override
    public R fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buffer) {
        return PacketHelper.readWithYabn(buffer, networkCodec.apply(id), true)
                .getOrThrow(false, s -> Constants.LOGGER.error("Could not parse {}", id));
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull R recipe) {
        PacketHelper.writeWithYabn(buffer, networkCodec.apply(recipe.getId()), recipe, true)
                .getOrThrow(false, s -> Constants.LOGGER.error("Could not parse {}", recipe.getId()));
    }

    public RecipeType<R> type() {
        return recipeType;
    }
}
