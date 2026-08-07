package com.skillpath.ai;
public record ChatTurn(String role, String text) {
    public static ChatTurn user(String text) { return new ChatTurn("user", text); }
    public static ChatTurn model(String text) { return new ChatTurn("model", text); }
}