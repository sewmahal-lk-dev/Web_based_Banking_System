package com.banking.dao;

import java.sql.Connection;
import java.sql.SQLException;

/** Callback must use the caller's connection and must not commit, close or retain it. */
@FunctionalInterface
public interface BankingEventObserver {
    void onEvent(Connection connection,BankingEvent event)throws SQLException;
}
