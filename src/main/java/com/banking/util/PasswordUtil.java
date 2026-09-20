package com.banking.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        return BCrypt.hashpw(
                password,
                BCrypt.gensalt(12));
    }

    public static boolean checkPassword(
            String plainPassword,
            String hashedPassword) {

        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {

            return BCrypt.checkpw(
                    plainPassword,
                    hashedPassword);

        } catch (IllegalArgumentException e) {

            return false;
        }
    }
}