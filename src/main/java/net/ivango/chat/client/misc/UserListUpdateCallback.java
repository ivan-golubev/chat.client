package net.ivango.chat.client.misc;

import net.ivango.chat.common.responses.User;

import java.util.List;

public interface UserListUpdateCallback {
    public void onUserListUpdated(List<User> users);
}
