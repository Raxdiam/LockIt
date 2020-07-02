package com.raxdiam.lockit;

import com.raxdiam.lockit.text.PrefixedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class LockItLockResult {
    private final LockItAction action;
    private final boolean success;
    private final Text message;

    private LockItLockResult(LockItAction action, boolean success, Text message) {
        this.action = action;
        this.success = success;
        this.message = message;
    }

    public static LockItLockResult success(LockItAction action, String message) {
        return new LockItLockResult(action, true, PrefixedText.createLiteral(message, Formatting.GREEN));
    }

    public static LockItLockResult failSoft(LockItAction action, String message) {
        return new LockItLockResult(action, false, PrefixedText.createLiteral(message, Formatting.YELLOW));
    }

    public static LockItLockResult fail(LockItAction action, String message) {
        return new LockItLockResult(action, false, PrefixedText.createLiteral(message, Formatting.RED));
    }

    public LockItAction getAction() {
        return this.action;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public Text getMessage() {
        return this.message;
    }
}
