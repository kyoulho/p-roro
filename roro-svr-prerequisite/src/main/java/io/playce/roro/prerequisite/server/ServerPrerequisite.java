package io.playce.roro.prerequisite.server;

public interface ServerPrerequisite {
    void checkAdminPermission() throws InterruptedException;

    void checkSoftwares() throws InterruptedException;
}
