package com.raxdiam.lockables.text;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class PrefixedText {
    public static String MOD_ID;

    public static MutableText createLiteral(String prefix, String value, Formatting formatting ) {
        return createPrefix(prefix).append(new LiteralText(value).formatted(formatting));
    }

    public static MutableText createLiteral(String prefix, String value) {
        return createLiteral(prefix, value, Formatting.WHITE);
    }

    public static MutableText createLiteral(String value, Formatting formatting) {
        return createLiteral(getModName(), value, formatting);
    }

    public static MutableText createLiteral(String value) {
        return createLiteral(value, Formatting.WHITE);
    }

    public static MutableText createPrefix(String prefix) {
        return new LiteralText("[").formatted(Formatting.GRAY).append(
                new LiteralText(prefix).formatted(Formatting.GOLD)).append(
                new LiteralText("] ").formatted(Formatting.GRAY));
    }

    private static String getModName() {
        return FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getName();
    }
}
