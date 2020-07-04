package com.raxdiam.lockit.text;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextBuilder {
    private MutableText text;

    private TextBuilder(MutableText text) {
        this.text = text;
    }

    public static TextBuilder literal(String value, Formatting formatting) {
        return new TextBuilder(new LiteralText(value).formatted(formatting));
    }

    public static TextBuilder literal(String value) {
        return new TextBuilder(new LiteralText(value));
    }

    public TextBuilder append(TextBuilder builder) {
        text.append(builder.text);
        return this;
    }

    public MutableText build() {
        return this.text;
    }
}
