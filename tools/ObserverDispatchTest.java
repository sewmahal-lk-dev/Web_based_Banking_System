package com.banking.dao;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** No database needed: verifies the subject's callback, registration and failure contract. */
public class ObserverDispatchTest {
    private static int checks;
    private static void check(boolean ok,String label){
        if(!ok)throw new AssertionError(label);checks++;System.out.println("PASS "+label);
    }
    public static void main(String[] args)throws Exception {
        Connection connection=(Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},(proxy,method,values)->{
                    throw new AssertionError("Publisher must not manage JDBC: "+method.getName());
                });
        BankingEvent event=new BankingEvent.AccountChanged(910000000001L);
        List<Integer> called=new ArrayList<>();
        List<BankingEventObserver> registration=new ArrayList<>();
        registration.add((c,e)->{check(c==connection&&e==event,"First callback gets original connection and typed event");called.add(1);});
        registration.add((c,e)->{check(c==connection&&e==event,"Second callback gets original connection and typed event");called.add(2);});
        BankingEventPublisher publisher=new BankingEventPublisher(registration);
        registration.clear();publisher.publish(connection,event);
        check(called.equals(List.of(1,2)),"Registration is copied and callbacks run synchronously in order");
        SQLException failure=new SQLException("Synthetic observer failure");
        called.clear();
        BankingEventPublisher failing=new BankingEventPublisher(List.of(
                (c,e)->called.add(1),(c,e)->{throw failure;},(c,e)->called.add(3)));
        try{failing.publish(connection,event);throw new AssertionError("Failure swallowed");}
        catch(SQLException actual){check(actual==failure,"Observer failure propagates unchanged to transaction owner");}
        check(called.equals(List.of(1)),"Dispatch stops after failed observer; no later side effects");
        RuntimeException runtime=new IllegalArgumentException("Synthetic runtime failure");
        try{new BankingEventPublisher(List.of((c,e)->{throw runtime;})).publish(connection,event);throw new AssertionError("Runtime failure swallowed");}
        catch(RuntimeException actual){check(actual==runtime,"Runtime failure also propagates unchanged");}
        AtomicInteger total=new AtomicInteger();
        BankingEventPublisher concurrent=new BankingEventPublisher(List.of((c,e)->{
            if(c!=connection||e!=event)throw new AssertionError("Shared request state changed");total.incrementAndGet();
        },(c,e)->total.incrementAndGet()));
        ExecutorService pool=Executors.newFixedThreadPool(4);
        try{
            List<Future<?>> futures=new ArrayList<>();
            for(int i=0;i<100;i++)futures.add(pool.submit(()->{
                try{concurrent.publish(connection,event);}catch(SQLException e){throw new RuntimeException(e);}
            }));
            for(Future<?> future:futures)future.get(10,TimeUnit.SECONDS);
        }finally{pool.shutdownNow();}
        check(total.get()==200,"Concurrent publication uses fixed registration without lost callbacks");
        // Unrelated events must be ignored by each concrete observer, with no JDBC interaction.
        new CustomerNotificationObserver().onEvent(connection,new BankingEvent.CustomerRegistered(1));
        new StaffNotificationObserver().onEvent(connection,new BankingEvent.PaymentRecorded("TEST"));
        check(true,"Concrete observers ignore events outside their recipient responsibility");
        System.out.println("OBSERVER DISPATCH CHECKS PASSED: "+checks);
    }
}
