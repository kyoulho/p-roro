package io.playce.roro.svr.asmt.debian.impl;

public class DebianAsmtCommand {
    public static String PACKAGES = "dpkg -l | grep -E '^ii' | awk '{print $2, $3}'";
}
