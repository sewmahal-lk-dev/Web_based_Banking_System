import com.banking.dao.*;
import com.banking.util.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/** Isolated JDBC doubles: no network or database writes. Exercises the existing DAO transaction paths. */
class FinancialSafetyTest {
    static State state;
    static int checks;
    static class State {
        boolean sender=true,receiver=true,installment=true,failInsert=false;
        BigDecimal balance=new BigDecimal("100.00");
        int commits,rollbacks,updates,customerId;
    }
    interface Operation { void run() throws Exception; }
    static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;System.out.println("PASS "+label);}
    static void rejects(Operation op,String label)throws Exception {try{op.run();throw new AssertionError(label);}catch(IllegalArgumentException|SQLException expected){check(state.commits==0,label);}}
    public static void main(String[] args)throws Exception {
        Class.forName("com.banking.util.DBConnection");
        for(Driver driver:Collections.list(DriverManager.getDrivers()))DriverManager.deregisterDriver(driver);
        DriverManager.registerDriver(new Driver(){
            public Connection connect(String u,Properties p){return connection();}
            public boolean acceptsURL(String u){return true;}
            public DriverPropertyInfo[] getPropertyInfo(String u,Properties p){return new DriverPropertyInfo[0];}
            public int getMajorVersion(){return 1;}public int getMinorVersion(){return 0;}public boolean jdbcCompliant(){return false;}public Logger getParentLogger(){return Logger.getGlobal();}
        });
        TransferDAO transfer=new TransferDAO();PaymentDAO payment=new PaymentDAO();
        for(String amount:List.of("0","-1","0.001","10000000000000")) {
            state=new State();rejects(()->transfer.transferMoney(7,200,new BigDecimal(amount)),"transfer rejects "+amount);
            state=new State();rejects(()->payment.makeBillPayment(7,"Provider","CEB/123-456",new BigDecimal(amount)),"payment rejects "+amount);
        }
        state=new State();String ref=transfer.transferMoney(7,200,new BigDecimal("10.00"));check(state.commits==1 && state.updates==3 && state.customerId==7 && ref.startsWith("TRF-"),"transfer records both balance changes and payment for session customer");
        state=new State();state.failInsert=true;rejects(()->transfer.transferMoney(7,200,new BigDecimal("10")),"transfer insert failure does not commit");check(state.rollbacks==1,"transfer rolls back after balance changes");
        state=new State();rejects(()->transfer.transferMoney(7,100,new BigDecimal("10")),"same account rejected");check(state.updates==0,"same account never updates balance");
        state=new State();state.receiver=false;rejects(()->transfer.transferMoney(7,200,new BigDecimal("10")),"missing receiver rejected");check(state.updates==0,"missing receiver never updates balance");
        state=new State();rejects(()->transfer.transferMoney(7,200,new BigDecimal("101")),"insufficient transfer balance rejected");
        state=new State();state.sender=false;rejects(()->transfer.transferMoney(7,200,new BigDecimal("10")),"missing active sender rejected");
        state=new State();payment.makeBillPayment(7,"Provider","CEB/123-456",new BigDecimal("10"));check(state.commits==1 && state.updates==2 && state.customerId==7,"payment debits and records together");
        state=new State();state.failInsert=true;rejects(()->payment.makeBillPayment(7,"Provider","CEB/123-456",new BigDecimal("10")),"payment insert failure does not commit");check(state.rollbacks==1,"payment rolls back debit on insert failure");
        state=new State();rejects(()->payment.makeBillPayment(7," ","CEB/123-456",new BigDecimal("10")),"blank provider rejected");
        state=new State();rejects(()->payment.makeBillPayment(7,"x".repeat(151),"CEB/123-456",new BigDecimal("10")),"oversized provider rejected");
        state=new State();rejects(()->payment.makeBillPayment(7,"Provider","CEB/123-456",new BigDecimal("101")),"insufficient payment balance rejected");
        state=new State();new LoanRepaymentDAO().pay(7,1);check(state.commits==1 && state.updates==4 && state.customerId==7,"loan installment debit, status and payment commit together");
        state=new State();state.installment=false;rejects(()->new LoanRepaymentDAO().pay(7,1),"paid, inactive or unowned installment rejected");check(state.updates==0,"ineligible installment never debits");
        state=new State();state.failInsert=true;rejects(()->new LoanRepaymentDAO().pay(7,1),"loan payment insert failure does not commit");check(state.rollbacks==1,"loan installment and debit roll back together");
        check(Input.html("<script>\"&'").equals("&lt;script&gt;&quot;&amp;&#39;"),"HTML escaping");
        var installments=DemoRules.installments(new BigDecimal("1000"),new BigDecimal("12"),7);
        check(installments.stream().reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(new BigDecimal("1070"))==0,"loan installments sum to exact total");
        check(installments.get(6).compareTo(new BigDecimal("152.90"))==0,"final installment absorbs rounding remainder");
        check(DemoRules.payout(new BigDecimal("1000"),new BigDecimal("6"),java.time.LocalDate.of(2025,1,1),java.time.LocalDate.of(2026,1,1),java.time.LocalDate.of(2026,1,1)).compareTo(new BigDecimal("1060"))==0,"maturity uses annual actual-days interest");
        check(DemoRules.payout(new BigDecimal("1000"),new BigDecimal("6"),java.time.LocalDate.of(2025,1,1),java.time.LocalDate.of(2026,1,1),java.time.LocalDate.of(2025,12,31)).compareTo(new BigDecimal("1000"))==0,"early withdrawal pays principal only");
        check(Input.longId("3000000000")==3000000000L,"payment IDs support SQL BIGINT");
        System.out.println("Completed "+checks+" isolated financial/security checks. No database accessed.");
    }
    static Connection connection(){return proxy(Connection.class,(p,m,a)->switch(m.getName()) {
        case "prepareStatement" -> statement((String)a[0]);case "commit" -> {state.commits++;yield null;}case "rollback" -> {state.rollbacks++;yield null;}case "getAutoCommit" -> false;default -> defaultValue(m.getReturnType());
    });}
    static PreparedStatement statement(String sql){Map<Integer,Object> params=new HashMap<>();return proxy(PreparedStatement.class,(p,m,a)->{
        if(m.getName().startsWith("set") && a!=null && a.length>=2){params.put((Integer)a[0],a[1]);return null;}
        if(m.getName().equals("executeQuery")){
            if(sql.contains("loan_repayment")){state.customerId=(Integer)params.get(2);return result(state.installment,true);}
            boolean sender=sql.contains("customer_id");if(sender)state.customerId=(Integer)params.get(1);
            return result(sender?state.sender:state.receiver,sender);
        }
        if(m.getName().equals("executeUpdate")){if(sql.startsWith("INSERT")&&state.failInsert)throw new SQLException("Simulated insert failure");state.updates++;return 1;}
        return defaultValue(m.getReturnType());
    });}
    static ResultSet result(boolean exists,boolean sender){boolean[] first={true};return proxy(ResultSet.class,(p,m,a)->switch(m.getName()){
        case "next" -> {boolean value=first[0]&&exists;first[0]=false;yield value;}case "getLong" -> sender?100L:200L;case "getBigDecimal" -> state.balance;default -> defaultValue(m.getReturnType());
    });}
    static <T>T proxy(Class<T> type,InvocationHandler handler){return type.cast(Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},handler));}
    static Object defaultValue(Class<?> type){if(type==boolean.class)return false;if(type==int.class)return 0;if(type==long.class)return 0L;return null;}
}
