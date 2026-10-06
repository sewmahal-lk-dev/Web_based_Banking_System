package com.banking.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Synchronous subject with immutable registration, safe to share across servlet requests. */
public final class BankingEventPublisher {
    private static final BankingEventPublisher NOTIFICATIONS=new BankingEventPublisher(
            List.of(new CustomerNotificationObserver(),new StaffNotificationObserver()));
    private final List<BankingEventObserver> observers;

    public BankingEventPublisher(List<? extends BankingEventObserver> observers){
        this.observers=List.copyOf(observers);
    }
    public void publish(Connection connection,BankingEvent event)throws SQLException {
        Objects.requireNonNull(connection);Objects.requireNonNull(event);
        for(BankingEventObserver observer:observers)observer.onEvent(connection,event);
    }
    public static void publishNotifications(Connection connection,BankingEvent event)throws SQLException {
        NOTIFICATIONS.publish(connection,event);
    }
}
