package dev.quasemago.maidbot.helpers;

public enum LogTypes {
    EDIT_MESSAGE((1 << 1), "Edit Message"),
    DELETE_MESSAGE((1<<2), "Delete Message"),
    MEMBER_JOIN((1<<3), "Member Join Server"),
    MEMBER_LEAVE((1<<4), "Member Leave Server"),
    MEMBER_BAN((1<<5), "Member Ban"),
    MEMBER_UNBAN((1<<6), "Member Unban"),
    MEMBER_JOIN_VOICE((1<<7), "Member Join Voice Channel"),
    MEMBER_LEAVE_VOICE((1<<8), "Member Leave Voice Channel");

    // TODO: Add more log types.
    //  like: kick, mute, unmute, etc.

    private final long value;
    private final String name;

    LogTypes(long value, String name) {
        this.value = value;
        this.name = name;
    }

    public long getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }
}