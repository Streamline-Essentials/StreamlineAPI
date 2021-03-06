package net.streamline.api.savables.events;

import net.streamline.api.savables.users.SavableUser;

public class SavableUserEvent<T extends SavableUser> extends SavableEvent<T> {
    public SavableUserEvent(T user) {
        super(user);
    }
}
