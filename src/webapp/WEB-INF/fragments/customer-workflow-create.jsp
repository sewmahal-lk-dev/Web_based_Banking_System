<%@ page import="com.banking.util.*,com.banking.model.Account,java.util.*" %>
<% String workflow=(String)request.getAttribute("section"); %>
<% if(List.of("cards","loans","investments").contains(workflow)){ %>
<section class="panel <%= "cards".equals(workflow)?"card-application":"" %>"><h2><%= "cards".equals(workflow)?"Apply for a New Card":"loans".equals(workflow)?"Apply for a loan":"Start an investment" %></h2>
<% if("loans".equals(workflow)){ %><p>University demo: <%= DemoRules.number("loan.annualRate") %>% annual flat simple interest. Monthly installments; final installment adjusts rounding. Approval is followed by your acceptance before money is credited.</p><% } %>
<% if("investments".equals(workflow)){ %><p>University demo: <%= DemoRules.number("investment.annualRate") %>% annual simple interest, actual days/365. Your selected amount is debited when an officer approves. Withdrawal before maturity returns principal only; at maturity, principal plus interest. No automatic reinvestment.</p><% } %>
<% if("cards".equals(workflow)){
List<Account> cardAccounts=(List<Account>)request.getAttribute("accounts");
Account linkedAccount=cardAccounts==null?null:cardAccounts.stream().filter(a->"ACTIVE".equals(a.getStatus())).min(Comparator.comparingLong(Account::getAccountNumber)).orElse(null);
%>
<p class="card-application-intro">Choose your card type and review the details. Your application will be sent to Card Services for a decision.</p>
<section class="card-application-section card-account-summary" aria-labelledby="card-account-heading">
<h3 id="card-account-heading">01 / Linked account</h3>
<p class="card-applicant"><%= Input.html(session.getAttribute("userName")) %></p>
<% if(linkedAccount!=null){String linkedNumber=String.valueOf(linkedAccount.getAccountNumber()); %>
<strong><%= Input.html(linkedAccount.getAccountType()) %> &bull;&bull;&bull;&bull; <%= Input.html(linkedNumber.substring(Math.max(0,linkedNumber.length()-4))) %></strong><span class="status-badge" data-state="ACTIVE">ACTIVE</span>
<p>Your first active account is linked automatically. Account selection is not required.</p>
<% }else{ %><p class="card-account-unavailable" role="status">You need an active account to apply. Open My Accounts to review your accounts.</p><a href="<%= request.getContextPath() %>/customer/accounts">Open My Accounts</a><% } %>
</section>
<section class="card-application-section card-application-terms" aria-labelledby="card-limits-heading"><h3 id="card-limits-heading">03 / Limits &amp; review</h3>
<dl><div><dt>Daily limit</dt><dd>LKR <%= DemoRules.number("card.dailyLimit") %></dd></div><div data-card-kind="DEBIT"><dt>Debit withdrawal limit</dt><dd>LKR <%= DemoRules.number("card.withdrawalLimit") %></dd></div><div data-card-kind="CREDIT"><dt>Credit limit</dt><dd>LKR <%= DemoRules.number("card.creditLimit") %></dd></div><div><dt>Validity after approval</dt><dd><%= DemoRules.integer("card.expiryYears") %> years</dd></div></dl>
<p>These limits are assigned by the bank. Applications start as PENDING; Card Services reviews them before activation.</p><p class="card-demo-note">University demo: cards are internal identifiers, not payment-network cards.</p></section>
<% } %>
<form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="apply">
<label>Type<select name="type"><% String[] types="cards".equals(workflow)?new String[]{"DEBIT","CREDIT"}:"loans".equals(workflow)?new String[]{"STUDENT","PERSONAL","VEHICLE","HOME","BUSINESS"}:new String[]{"FIXED_DEPOSIT","SAVINGS_PLAN"};for(String type:types){ %><option value="<%= type %>"><%= type.replace('_',' ') %></option><% } %></select></label>
<% if(!"cards".equals(workflow)){String kind="loans".equals(workflow)?"loan":"investment"; %>
<label>Amount (LKR)<input type="number" name="amount" min="<%= DemoRules.number(kind+".minAmount") %>" max="<%= DemoRules.number(kind+".maxAmount") %>" step="0.01" required></label>
<label>Term in months<input type="number" name="months" min="1" max="<%= DemoRules.integer(kind+".maxMonths") %>" required></label>
<label><input class="inline-checkbox" type="checkbox" required> I accept the displayed demo rules and authorize this application.</label><% } %>
<p>The first active account shown on your dashboard will be linked to this request.</p><button>Submit application</button></form></section><% } %>
