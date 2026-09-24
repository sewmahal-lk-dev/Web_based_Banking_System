import com.banking.dao.CashTransactionDAO;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
import java.lang.reflect.*;

/** Isolated transactional JDBC fixtures; never opens a real database connection. */
public class CashTransactionSafetyTest {
 static BigDecimal saved, working;
 static int payments,audits,wp,wa,commits,rollbacks,updates,checks;
 static String failure,role,lastType,paymentReference,auditDetails;
 static boolean exists,locked;
 static void reset(){saved=new BigDecimal("94666.67");payments=audits=commits=rollbacks=updates=0;failure="";role="CUSTOMER_SERVICE_OFFICER";exists=true;}
 static void check(boolean b,String name){if(!b)throw new AssertionError(name);checks++;System.out.println("PASS "+name);}
 interface Action{void run()throws Exception;}
 static void reject(Action action,Class<?> type,String message)throws Exception{
  BigDecimal before=saved;int p=payments,a=audits,c=commits,r=rollbacks;
  try{action.run();throw new AssertionError("Expected failure");}catch(Exception e){check(type.isInstance(e)&&e.getMessage().contains(message),"preserves "+type.getSimpleName()+": "+message);}
  check(saved.equals(before)&&payments==p&&audits==a&&commits==c&&rollbacks==r+1,"failure rolls back all changes");
 }
 public static void main(String[] args)throws Exception{
  Class.forName("com.banking.util.DBConnection");for(Driver d:Collections.list(DriverManager.getDrivers()))DriverManager.deregisterDriver(d);
  DriverManager.registerDriver(new Driver(){
   public Connection connect(String u,Properties p){return connection();}public boolean acceptsURL(String u){return true;}
   public DriverPropertyInfo[] getPropertyInfo(String u,Properties p){return new DriverPropertyInfo[0];}
   public int getMajorVersion(){return 1;}public int getMinorVersion(){return 0;}public boolean jdbcCompliant(){return false;}
   public java.util.logging.Logger getParentLogger(){return java.util.logging.Logger.getGlobal();}
  });
  CashTransactionDAO dao=new CashTransactionDAO();BigDecimal amount=new BigDecimal("30000.00");
  reset();String reference=dao.deposit(4,4975567220610L,amount,"Investment funding test");
  check(saved.equals(new BigDecimal("124666.67"))&&updates==1&&commits==1,"deposit increases balance exactly once");
  check(payments==1&&audits==1&&lastType.equals("CASH_DEPOSIT")&&reference.equals(paymentReference)&&auditDetails.contains(reference),"deposit creates linked payment and audit");
  check(reference.matches("DEP-[A-F0-9]{12}")&&reference.length()<=50,"reference fits unique VARCHAR(50)");
  dao.withdraw(4,4975567220610L,new BigDecimal("1000.00"),"Withdrawal");
  check(saved.equals(new BigDecimal("123666.67"))&&payments==2&&audits==2&&lastType.equals("CASH_WITHDRAWAL"),"withdrawal debits and records payment and audit");
  for(boolean deposit:new boolean[]{true,false}){
   reset();exists=false;reject(()->{if(deposit)dao.deposit(4,999L,amount,"");else dao.withdraw(4,999L,amount,"");},IllegalArgumentException.class,"Account number was not found.");
   check(updates==0,"missing account never updates balance");
   reset();role="INVESTMENT_OFFICER";reject(()->{if(deposit)dao.deposit(3,4975567220610L,amount,"");else dao.withdraw(3,4975567220610L,amount,"");},SecurityException.class,"not authorized");
   for(String f:List.of("payment","audit","duplicate")){
    reset();failure=f;reject(()->{if(deposit)dao.deposit(4,4975567220610L,amount,"");else dao.withdraw(4,4975567220610L,amount,"");},SQLException.class,f.equals("duplicate")?"Duplicate entry":"Simulated "+f);
    check(updates==1,"no retry or partial balance change after "+f);
   }
  }
  reset();reject(()->dao.withdraw(4,4975567220610L,new BigDecimal("120000.00"),""),IllegalArgumentException.class,"Insufficient account balance");
  System.out.println("Completed "+checks+" cash transaction checks. No database accessed.");
 }
 static Connection connection(){working=saved;wp=payments;wa=audits;locked=false;return proxy(Connection.class,(p,m,a)->switch(m.getName()){
  case "prepareStatement"->statement((String)a[0]);case "commit"->{saved=working;payments=wp;audits=wa;commits++;yield null;}
  case "rollback"->{working=saved;wp=payments;wa=audits;rollbacks++;yield null;}default->zero(m.getReturnType());});}
 static PreparedStatement statement(String sql){Map<Integer,Object> params=new HashMap<>();return proxy(PreparedStatement.class,(p,m,a)->{
  if(m.getName().startsWith("set")&&a!=null&&a.length>=2){params.put((Integer)a[0],a[1]);return null;}
  if(m.getName().equals("executeQuery")){
   boolean employee=sql.contains("FROM employee");if(!employee){if(!sql.contains("FOR UPDATE"))throw new AssertionError("Missing lock");locked=true;}
   boolean[] first={true};return proxy(ResultSet.class,(q,n,b)->switch(n.getName()){
    case "next"->{boolean found=first[0]&&(employee||exists);first[0]=false;yield found;}
    case "getString"->b[0].equals("role")?role:"ACTIVE";case "getBigDecimal"->working;case "getInt"->1;default->zero(n.getReturnType());});
  }
  if(m.getName().equals("executeUpdate")){
   if(sql.startsWith("UPDATE account")){if(!locked||!Objects.equals(params.get(2),4975567220610L))throw new AssertionError("Wrong account or lock");updates++;BigDecimal amount=(BigDecimal)params.get(1);working=sql.contains("balance +")?working.add(amount):working.subtract(amount);return 1;}
   if(sql.startsWith("INSERT INTO payment")){
    if(failure.equals("duplicate"))throw new SQLIntegrityConstraintViolationException("Duplicate entry for key reference_number","23000",1062);
    if(failure.equals("payment"))throw new SQLException("Simulated payment");wp++;lastType=(String)params.get(2);paymentReference=(String)params.get(5);return 1;}
   if(sql.startsWith("INSERT INTO audit_log")){if(failure.equals("audit"))throw new SQLException("Simulated audit");wa++;auditDetails=(String)params.get(3);return 1;}
   throw new AssertionError("Unexpected SQL: "+sql);
  }return zero(m.getReturnType());});}
 static <T>T proxy(Class<T> t,InvocationHandler h){return t.cast(Proxy.newProxyInstance(t.getClassLoader(),new Class[]{t},h));}
 static Object zero(Class<?> t){if(t==boolean.class)return false;if(t==int.class)return 0;if(t==long.class)return 0L;return null;}
}
