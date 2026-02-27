package com.labsupplysystem.labbackend.common;

import com.labsupplysystem.labbackend.entity.User;

public class UserContext {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }
}