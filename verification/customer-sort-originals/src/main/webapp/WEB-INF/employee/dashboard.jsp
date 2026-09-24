<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ page import="java.util.*,com.banking.util.Input" %>

        <% String role=(String) session.getAttribute("role"); Map<String, List<Map<String, Object>>> groups =
            (Map<String, List<Map<String, Object>>>)
                request.getAttribute("groups");

                Object csrfObject = session.getAttribute("csrf");
                String csrf = csrfObject == null ? "" : csrfObject.toString();

                Object userNameObject = session.getAttribute("userName");
                String userName =
                userNameObject == null
                ? "Staff Member"
                : userNameObject.toString();

                Object userIdObject = session.getAttribute("userId");
                String currentUserId =
                userIdObject == null
                ? ""
                : userIdObject.toString();
                %>

                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="UTF-8">

                    <meta name="viewport" content="width=device-width,initial-scale=1">

                    <meta name="csrf-token" content="<%= Input.html(csrf) %>">

                    <title>
                        Staff Dashboard | LankaTrust Bank
                    </title>

                    <script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>

                    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/dashboard.css">

                    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/services.css">

                    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/lankatrust.css">

                    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/banking-system.css">

                    <script defer src="<%= request.getContextPath() %>/assets/js/banking-ui.js"></script>
                <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/notifications.css">
<script defer src="<%= request.getContextPath() %>/assets/js/notifications.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/design-system.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/workspaces.css">
<script defer src="<%= request.getContextPath() %>/assets/js/workspaces.js"></script>
<% if ("SYSTEM_ADMIN".equals(role)) { %><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/admin-overview.css"><% } %>
</head>

                <body class="staff-portal" data-role="<%= Input.html(role) %>">


                    <!-- ========================================= -->
                    <!-- BRAND BAR -->
                    <!-- ========================================= -->

                    <div class="staff-brandbar">

                        <a href="<%= request.getContextPath() %>/">
                            LankaTrust
                        </a>

                        <span>
                            Banking Operations
                        </span>

                    </div>


                    <!-- ========================================= -->
                    <!-- STAFF HEADER -->
                    <!-- ========================================= -->

                    <header class="topbar">

                        <div>

                            <span class="eyebrow">
                                SECURE STAFF PORTAL
                            </span>

                            <h1>
                                <%= Input.html( role==null ? "STAFF" : role.replace('_', ' ' ) ) %>
                            </h1>

                        </div>


                        <span>

                            <%= Input.html(userName) %>

                                |

                                <a href="<%= request.getContextPath() %>/logout">
                                    Sign Out
                                </a>

                        </span>

                    <jsp:include page="/WEB-INF/fragments/notification-bell.jsp" /></header>


                    <main class="dashboard-content services-content">
<% if ("SYSTEM_ADMIN".equals(role)) { %><jsp:include page="/WEB-INF/fragments/admin-overview.jsp" /><% } %>
<section class="staff-welcome"><p class="eyebrow">LANKATRUST / OPERATIONS</p><h2>Welcome, <%= Input.html(userName) %></h2><p>Your workspace for customer care and banking operations.</p></section>

                        <% if ("CARD_SERVICES_OFFICER".equals(role)) { %>
                            <jsp:include page="/WEB-INF/fragments/card-closure.jsp" />
                            <% } %>


                                <!-- ========================================= -->
                                <!-- ERROR / SUCCESS MESSAGES -->
                                <!-- ========================================= -->

                                <% if (request.getAttribute("error") !=null) { %>

                                    <p class="notice error" role="alert">

                                        <%= Input.html( request.getAttribute("error") ) %>

                                    </p>

                                    <% } %>


                                        <% if (request.getAttribute("success") !=null) { %>

                                            <p class="notice" role="status">

                                                <%= Input.html( request.getAttribute("success") ) %>

                                            </p>

                                            <% } %>


                                                <!-- ========================================= -->
                                                <!-- CUSTOMER SERVICE OFFICER -->
                                                <!-- CASH DEPOSIT / WITHDRAWAL -->
                                                <!-- ========================================= -->

                                                <% if ("CUSTOMER_SERVICE_OFFICER".equals(role)) { %>

                                                    <section class="panel" id="cash-transactions">

                                                        <div class="panel-heading">

                                                            <div>

                                                                <p class="eyebrow">
                                                                    TELLER OPERATIONS
                                                                </p>

                                                                <h2>
                                                                    Cash Transactions
                                                                </h2>

                                                                <p>
                                                                    Process secure over-the-counter cash deposits
                                                                    and withdrawals for customer bank accounts.
                                                                </p>

                                                            </div>

                                                        </div>


                                                        <div class="service-grid">


                                                            <!-- ================================= -->
                                                            <!-- CASH DEPOSIT -->
                                                            <!-- ================================= -->

                                                            <article class="record">

                                                                <div class="panel-heading">

                                                                    <div>

                                                                        <p class="eyebrow">
                                                                            CASH IN
                                                                        </p>

                                                                        <h3>
                                                                            Cash Deposit
                                                                        </h3>

                                                                    </div>

                                                                </div>


                                                                <p>
                                                                    Credit cash received at the bank counter
                                                                    to an active customer account.
                                                                </p>


                                                                <form method="post">

                                                                    <input type="hidden" name="csrf"
                                                                        value="<%= Input.html(csrf) %>">

                                                                    <input type="hidden" name="action"
                                                                        value="cash-deposit">


                                                                    <label>

                                                                        Customer Account Number

                                                                        <input type="text" name="accountNumber"
                                                                            inputmode="numeric" pattern="[0-9]+"
                                                                            maxlength="20" autocomplete="off"
                                                                            placeholder="Enter account number" required>

                                                                    </label>


                                                                    <label>

                                                                        Deposit Amount (LKR)

                                                                        <input type="number" name="amount" min="0.01"
                                                                            max="9999999999999.99" step="0.01"
                                                                            placeholder="0.00" required>

                                                                    </label>


                                                                    <label>

                                                                        Reference / Note

                                                                        <textarea name="note" maxlength="200" rows="3"
                                                                            placeholder="Optional teller reference or note"></textarea>

                                                                    </label>


                                                                    <button type="submit">
                                                                        Process Cash Deposit
                                                                    </button>

                                                                </form>

                                                            </article>


                                                            <!-- ================================= -->
                                                            <!-- CASH WITHDRAWAL -->
                                                            <!-- ================================= -->

                                                            <article class="record">

                                                                <div class="panel-heading">

                                                                    <div>

                                                                        <p class="eyebrow">
                                                                            CASH OUT
                                                                        </p>

                                                                        <h3>
                                                                            Cash Withdrawal
                                                                        </h3>

                                                                    </div>

                                                                </div>


                                                                <p>
                                                                    Debit cash from an active customer account
                                                                    after validating sufficient available balance.
                                                                </p>


                                                                <form method="post">

                                                                    <input type="hidden" name="csrf"
                                                                        value="<%= Input.html(csrf) %>">

                                                                    <input type="hidden" name="action"
                                                                        value="cash-withdrawal">


                                                                    <label>

                                                                        Customer Account Number

                                                                        <input type="text" name="accountNumber"
                                                                            inputmode="numeric" pattern="[0-9]+"
                                                                            maxlength="20" autocomplete="off"
                                                                            placeholder="Enter account number" required>

                                                                    </label>


                                                                    <label>

                                                                        Withdrawal Amount (LKR)

                                                                        <input type="number" name="amount" min="0.01"
                                                                            max="9999999999999.99" step="0.01"
                                                                            placeholder="0.00" required>

                                                                    </label>


                                                                    <label>

                                                                        Reference / Note

                                                                        <textarea name="note" maxlength="200" rows="3"
                                                                            placeholder="Optional teller reference or note"></textarea>

                                                                    </label>


                                                                    <button type="submit">
                                                                        Process Cash Withdrawal
                                                                    </button>

                                                                </form>

                                                            </article>

                                                        </div>

                                                    </section>

                                                    <% } %>


                                                        <!-- ========================================= -->
                                                        <!-- STAFF NAVIGATION -->
                                                        <!-- ========================================= -->

                                                        <nav class="staff-navigation" aria-label="Staff sections">
<% if ("SYSTEM_ADMIN".equals(role)) { %><a href="<%= request.getContextPath() %>/admin/reports">Reports</a><% } %>


                                                            <% if ("CUSTOMER_SERVICE_OFFICER".equals(role)) { %>

                                                                <a href="#cash-transactions">
                                                                    Cash Transactions
                                                                </a>

                                                                <% } %>


                                                                    <% if (groups !=null) { for (String groupName :
                                                                        groups.keySet()) { %>

                                                                        <a href="#<%= groupName.replace(' ', '-') %>">
                                                                            <%= Input.html(groupName) %>
                                                                        </a>

                                                                        <% } } %>

                                                        </nav>


                                                        <!-- ========================================= -->
                                                        <!-- AUDIT SEARCH -->
                                                        <!-- ADMIN + COMPLIANCE -->
                                                        <!-- ========================================= -->

                                                        <% if ( List.of( "SYSTEM_ADMIN" , "COMPLIANCE_RISK_OFFICER"
                                                            ).contains(role) ) { %>

                                                            <section class="panel" id="audit-search">

                                                                <div class="panel-heading">

                                                                    <div>

                                                                        <p class="eyebrow">
                                                                            AUDIT & COMPLIANCE
                                                                        </p>

                                                                        <h2>
                                                                            Search Audit Records
                                                                        </h2>

                                                                    </div>

                                                                </div>


                                                                <form method="get" action="<%= Input.html(request.getContextPath() + request.getAttribute("jakarta.servlet.forward.servlet_path")) %>#Audit-log">

                                                                    <label>

                                                                        Search action or details

                                                                        <input type="text" name="q" maxlength="100"
                                                                            value="<%= Input.html(request.getParameter("q")) %>"
                                                                        placeholder="Search audit records">

                                                                    </label>


                                                                    <button type="submit">
                                                                        Search Audit
                                                                    </button>

                                                                </form>

                                                            </section>

                                                            <% } %>


                                                                <!-- ========================================= -->
                                                                <!-- SYSTEM ADMIN -->
                                                                <!-- EMPLOYEE CREATE -->
                                                                <!-- ========================================= -->

                                                                <% if ("SYSTEM_ADMIN".equals(role)) { %>

                                                                    <section class="panel" id="employee-management">

                                                                        <div class="panel-heading">

                                                                            <div>

                                                                                <p class="eyebrow">
                                                                                    ADMINISTRATION MANAGEMENT
                                                                                </p>

                                                                                <h2>
                                                                                    Create Employee
                                                                                </h2>

                                                                                <p>
                                                                                    Create employee accounts and assign
                                                                                    banking roles and access
                                                                                    permissions.
                                                                                </p>

                                                                            </div>

                                                                        </div>


                                                                        <% request.setAttribute( "adminFormType"
                                                                            , "employee" );
                                                                            request.removeAttribute( "adminRecord" ); %>

                                                                            <jsp:include
                                                                                page="/WEB-INF/fragments/admin-form.jsp" />

                                                                    </section>


                                                                    <!-- ========================================= -->
                                                                    <!-- SYSTEM ADMIN -->
                                                                    <!-- BANKING PRODUCT CREATE -->
                                                                    <!-- ========================================= -->

                                                                    <section class="panel" id="product-management">

                                                                        <div class="panel-heading">

                                                                            <div>

                                                                                <p class="eyebrow">
                                                                                    BANKING PRODUCT MANAGEMENT
                                                                                </p>

                                                                                <h2>
                                                                                    Create Banking Product
                                                                                </h2>

                                                                                <p>
                                                                                    Create and maintain banking products
                                                                                    available through LankaTrust.
                                                                                </p>

                                                                            </div>

                                                                        </div>


                                                                        <% request.setAttribute( "adminFormType"
                                                                            , "product" );
                                                                            request.removeAttribute( "adminRecord" ); %>

                                                                            <jsp:include
                                                                                page="/WEB-INF/fragments/admin-form.jsp" />

                                                                    </section>

                                                                    <% } %>


                                                                        <!-- ========================================= -->
                                                                        <!-- INFORMATION -->
                                                                        <!-- ========================================= -->

                                                                        <p>
                                                                            Each section shows up to 200 records.
                                                                            Sensitive staff operations are validated by
                                                                            the
                                                                            server
                                                                            and recorded in the audit log.
                                                                        </p>


                                                                        <!-- ========================================= -->
                                                                        <!-- ROLE-SPECIFIC DATA GROUPS -->
                                                                        <!-- ========================================= -->

                                                                        <jsp:include page="/WEB-INF/fragments/staff-support-tickets.jsp" />

                                                                        <% if (groups !=null) { for ( Map.Entry< String,
                                                                            List<Map<String, Object>>
                                                                            > group : groups.entrySet()
                                                                            ) {
                                                                            %>


                                                                            <section class="panel"
                                                                                data-record-group="<%= Input.html(group.getKey()) %>" id="<%= group.getKey().replace(' ', '-') %>">


                                                                                <div class="panel-heading">

                                                                                    <div>

                                                                                        <p class="eyebrow">
                                                                                            BANK OPERATIONS
                                                                                        </p>

                                                                                        <h2>
                                                                                            <%= Input.html(
                                                                                                group.getKey() ) %>
                                                                                        </h2>

                                                                                    </div>


                                                                                    <span>

                                                                                        <%= group.getValue().size() %>
                                                                                            record(s)

                                                                                    </span>

                                                                                </div>


                                                                                <% if (group.getValue().isEmpty()) { %>

                                                                                    <p>
                                                                                        No records found.
                                                                                    </p>

                                                                                    <% } %>


                                                                                        <% for ( Map<String, Object>
                                                                                            record
                                                                                            : group.getValue()
                                                                                            ) {
                                                                                            %>


                                                                                            <article class="record">


                                                                                                <!-- ================================= -->
                                                                                                <!-- RECORD DETAILS -->
                                                                                                <!-- ================================= -->

                                                                                                <dl>

                                                                                                    <% for (
                                                                                                        Map.Entry<String,
                                                                                                        Object> field
                                                                                                        :
                                                                                                        record.entrySet()
                                                                                                        ) {
                                                                                                        %>


                                                                                                        <div>

                                                                                                            <dt>

                                                                                                                <%= Input.html(
                                                                                                                    field.getKey()
                                                                                                                    .replace('_', ' '
                                                                                                                    ) )
                                                                                                                    %>

                                                                                                            </dt>


                                                                                                            <dd>

                                                                                                                <span
                                                                                                                    class="<%= "status".equals(field.getKey())
                                                                                                                    ? "status-badge"
                                                                                                                    : ""
                                                                                                                    %>">

                                                                                                                    <%= Input.html(
                                                                                                                        field.getValue()
                                                                                                                        )
                                                                                                                        %>

                                                                                                                </span>

                                                                                                            </dd>

                                                                                                        </div>


                                                                                                        <% } %>

                                                                                                </dl>


                                                                                                <!-- ================================= -->
                                                                                                <!-- ADMIN EDIT EMPLOYEE / PRODUCT -->
                                                                                                <!-- ================================= -->

                                                                                                <% boolean
                                                                                                    editableProduct="Products"
                                                                                                    .equals(
                                                                                                    group.getKey() );
                                                                                                    boolean
                                                                                                    editableEmployee="Employees"
                                                                                                    .equals(
                                                                                                    group.getKey() ) &&
                                                                                                    record.get("employee_id")
                                                                                                    !=null &&
                                                                                                    !record.get("employee_id")
                                                                                                    .toString()
                                                                                                    .equals(currentUserId);
                                                                                                    if ( editableProduct
                                                                                                    || editableEmployee
                                                                                                    ) {
                                                                                                    request.setAttribute( "adminFormType"
                                                                                                    , editableProduct
                                                                                                    ? "product"
                                                                                                    : "employee" );
                                                                                                    request.setAttribute( "adminRecord"
                                                                                                    , record ); %>


                                                                                                    <details>

                                                                                                        <summary>
                                                                                                            Edit Record
                                                                                                            / Change
                                                                                                            Status
                                                                                                        </summary>

                                                                                                        <jsp:include
                                                                                                            page="/WEB-INF/fragments/admin-form.jsp" />

                                                                                                    </details>


                                                                                                    <% } %>


                                                                                                        <!-- ================================= -->
                                                                                                        <!-- APPROVAL / DECISION ACTIONS -->
                                                                                                        <!-- ================================= -->

                                                                                                        <%
                                                                                                            List<String[]>
                                                                                                            decisions =
                                                                                                            new
                                                                                                            ArrayList<>
                                                                                                                ();


                                                                                                                /*
                                                                                                                * LOAN
                                                                                                                MANAGEMENT
                                                                                                                */
                                                                                                                if (
                                                                                                                "Loans".equals(
                                                                                                                group.getKey()
                                                                                                                )
                                                                                                                &&
                                                                                                                "PENDING".equals(
                                                                                                                String.valueOf(
                                                                                                                record.get("status")
                                                                                                                )
                                                                                                                )
                                                                                                                ) {

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "loan-approve",
                                                                                                                "Approve Loan",
                                                                                                                record.get("loan_id")
                                                                                                                .toString()
                                                                                                                }
                                                                                                                );
                                                                                                                }


                                                                                                                /*
                                                                                                                * CARD
                                                                                                                MANAGEMENT
                                                                                                                */
                                                                                                                if (
                                                                                                                "Cards".equals(
                                                                                                                group.getKey()
                                                                                                                )
                                                                                                                ) {

                                                                                                                String
                                                                                                                cardId =
                                                                                                                record.get("card_id")
                                                                                                                .toString();

                                                                                                                String
                                                                                                                cardState
                                                                                                                =
                                                                                                                String.valueOf(
                                                                                                                record.get("status")
                                                                                                                );


                                                                                                                if (
                                                                                                                "PENDING".equals(
                                                                                                                cardState
                                                                                                                )
                                                                                                                ) {

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "card-approve",
                                                                                                                "Approve Card",
                                                                                                                cardId
                                                                                                                }
                                                                                                                );

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "card-reject",
                                                                                                                "Reject Card",
                                                                                                                cardId
                                                                                                                }
                                                                                                                );
                                                                                                                }


                                                                                                                if (
                                                                                                                "BLOCKED".equals(
                                                                                                                cardState
                                                                                                                )
                                                                                                                ) {

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "card-unblock",
                                                                                                                "Unblock Card",
                                                                                                                cardId
                                                                                                                }
                                                                                                                );
                                                                                                                }


                                                                                                                if (
                                                                                                                List.of(
                                                                                                                "ACTIVE",
                                                                                                                "BLOCKED",
                                                                                                                "EXPIRED"
                                                                                                                ).contains(cardState)
                                                                                                                ) {

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "card-close",
                                                                                                                "Close Card",
                                                                                                                cardId
                                                                                                                }
                                                                                                                );
                                                                                                                }
                                                                                                                }


                                                                                                                /*
                                                                                                                *
                                                                                                                INVESTMENT
                                                                                                                MANAGEMENT
                                                                                                                */
                                                                                                                if (
                                                                                                                "Investments".equals(
                                                                                                                group.getKey()
                                                                                                                )
                                                                                                                ) {

                                                                                                                String
                                                                                                                investmentId
                                                                                                                =
                                                                                                                record.get(
                                                                                                                "investment_id"
                                                                                                                ).toString();

                                                                                                                String
                                                                                                                investmentState
                                                                                                                =
                                                                                                                String.valueOf(
                                                                                                                record.get("status")
                                                                                                                );


                                                                                                                if (
                                                                                                                "PENDING".equals(
                                                                                                                investmentState
                                                                                                                )
                                                                                                                ) {

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "investment-approve",
                                                                                                                "Approve and Fund",
                                                                                                                investmentId
                                                                                                                }
                                                                                                                );

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "investment-reject",
                                                                                                                "Reject Investment",
                                                                                                                investmentId
                                                                                                                }
                                                                                                                );
                                                                                                                }


                                                                                                                if (
                                                                                                                List.of(
                                                                                                                "ACTIVE",
                                                                                                                "MATURED"
                                                                                                                ).contains(
                                                                                                                investmentState
                                                                                                                )
                                                                                                                ) {

                                                                                                                decisions.add(
                                                                                                                new
                                                                                                                String[]{
                                                                                                                "investment-mature",
                                                                                                                "Pay Out Matured Investment",
                                                                                                                investmentId
                                                                                                                }
                                                                                                                );
                                                                                                                }
                                                                                                                }


                                                                                                                /*
                                                                                                                * RENDER
                                                                                                                DECISION
                                                                                                                FORMS
                                                                                                                */
                                                                                                                for (
                                                                                                                String[]
                                                                                                                decision
                                                                                                                :
                                                                                                                decisions
                                                                                                                ) {
                                                                                                                %>


                                                                                                                <form
                                                                                                                    method="post">

                                                                                                                    <input
                                                                                                                        type="hidden"
                                                                                                                        name="csrf"
                                                                                                                        value="<%= Input.html(csrf) %>">

                                                                                                                    <input
                                                                                                                        type="hidden"
                                                                                                                        name="id"
                                                                                                                        value="<%= Input.html(decision[2]) %>">

                                                                                                                    <input
                                                                                                                        type="hidden"
                                                                                                                        name="action"
                                                                                                                        value="<%= Input.html(decision[0]) %>">


                                                                                                                    <label>

                                                                                                                        Reason

                                                                                                                        <textarea
                                                                                                                            name="reason"
                                                                                                                            maxlength="500"
                                                                                                                            required></textarea>

                                                                                                                    </label>


                                                                                                                    <button
                                                                                                                        type="submit">

                                                                                                                        <%= Input.html(
                                                                                                                            decision[1]
                                                                                                                            )
                                                                                                                            %>

                                                                                                                    </button>

                                                                                                                </form>


                                                                                                                <% } %>


                                                                                                                    <!-- ================================= -->
                                                                                                                    <!-- GENERAL ROLE ACTIONS -->
                                                                                                                    <!-- ================================= -->

                                                                                                                    <% String
                                                                                                                        action=null;
                                                                                                                        Object
                                                                                                                        recordId=null;
                                                                                                                        String[]
                                                                                                                        statuses=new
                                                                                                                        String[]{};
                                                                                                                        switch
                                                                                                                        (
                                                                                                                        group.getKey()
                                                                                                                        )
                                                                                                                        {
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        LOAN
                                                                                                                        REJECTION
                                                                                                                        */
                                                                                                                        case "Loans"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        ( "PENDING"
                                                                                                                        .equals(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="loan-reject"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "loan_id"
                                                                                                                        );
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        CARD
                                                                                                                        BLOCK
                                                                                                                        */
                                                                                                                        case "Cards"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        ( "ACTIVE"
                                                                                                                        .equals(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="card-block"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "card_id"
                                                                                                                        );
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        INVESTMENT
                                                                                                                        ASSIGN
                                                                                                                        */
                                                                                                                        case "Investments"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        ( "PENDING"
                                                                                                                        .equals(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        &&
                                                                                                                        record.get( "employee_id"
                                                                                                                        )==null
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="investment-assign"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "investment_id"
                                                                                                                        );
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        SERVICE
                                                                                                                        REQUEST
                                                                                                                        */
                                                                                                                        case "Service requests"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        (
                                                                                                                        List.of( "PENDING"
                                                                                                                        , "PROCESSING"
                                                                                                                        ).contains(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="request-review"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "request_id"
                                                                                                                        );
                                                                                                                        statuses=new
                                                                                                                        String[]{ "PROCESSING"
                                                                                                                        , "REJECTED"
                                                                                                                        , "COMPLETED"
                                                                                                                        };
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        SUPPORT
                                                                                                                        TICKET
                                                                                                                        */
                                                                                                                        case "Support tickets"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        (
                                                                                                                        !List.of( "CLOSED"
                                                                                                                        , "RESOLVED"
                                                                                                                        ).contains(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="ticket-response"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "ticket_id"
                                                                                                                        );
                                                                                                                        statuses=new
                                                                                                                        String[]{ "IN_PROGRESS"
                                                                                                                        , "ESCALATED"
                                                                                                                        , "RESOLVED"
                                                                                                                        };
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        COMPLIANCE
                                                                                                                        ACCOUNT
                                                                                                                        *
                                                                                                                        FREEZE
                                                                                                                        /
                                                                                                                        UNFREEZE
                                                                                                                        */
                                                                                                                        case "Accounts"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        (
                                                                                                                        List.of( "ACTIVE"
                                                                                                                        , "FROZEN"
                                                                                                                        ).contains(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="account-status"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "account_number"
                                                                                                                        );
                                                                                                                        statuses=new
                                                                                                                        String[]{ "ACTIVE"
                                                                                                                        .equals(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        ? "FROZEN"
                                                                                                                        : "ACTIVE"
                                                                                                                        };
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        ADMIN
                                                                                                                        CUSTOMER
                                                                                                                        *
                                                                                                                        ACTIVATE
                                                                                                                        /
                                                                                                                        DEACTIVATE
                                                                                                                        */
                                                                                                                        case "Customers"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        (
                                                                                                                        List.of( "ACTIVE"
                                                                                                                        , "INACTIVE"
                                                                                                                        ).contains(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="customer-status"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "customer_id"
                                                                                                                        );
                                                                                                                        statuses=new
                                                                                                                        String[]{ "ACTIVE"
                                                                                                                        .equals(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        ? "INACTIVE"
                                                                                                                        : "ACTIVE"
                                                                                                                        };
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        /*
                                                                                                                        *
                                                                                                                        ADMIN
                                                                                                                        EMPLOYEE
                                                                                                                        *
                                                                                                                        SOFT
                                                                                                                        DELETE
                                                                                                                        /
                                                                                                                        REACTIVATE
                                                                                                                        */
                                                                                                                        case "Employees"
                                                                                                                        :
                                                                                                                        if
                                                                                                                        (
                                                                                                                        record.get( "employee_id"
                                                                                                                        )
                                                                                                                        !=null
                                                                                                                        &&
                                                                                                                        !record.get( "employee_id"
                                                                                                                        ).toString().equals(
                                                                                                                        currentUserId
                                                                                                                        )
                                                                                                                        )
                                                                                                                        {
                                                                                                                        action="employee-status"
                                                                                                                        ;
                                                                                                                        recordId=record.get( "employee_id"
                                                                                                                        );
                                                                                                                        statuses=new
                                                                                                                        String[]{ "ACTIVE"
                                                                                                                        .equals(
                                                                                                                        String.valueOf(
                                                                                                                        record.get("status")
                                                                                                                        )
                                                                                                                        )
                                                                                                                        ? "INACTIVE"
                                                                                                                        : "ACTIVE"
                                                                                                                        };
                                                                                                                        }
                                                                                                                        break;
                                                                                                                        }
                                                                                                                        if
                                                                                                                        (
                                                                                                                        action
                                                                                                                        !=null
                                                                                                                        )
                                                                                                                        {
                                                                                                                        %>


                                                                                                                        <form
                                                                                                                            method="post">

                                                                                                                            <input
                                                                                                                                type="hidden"
                                                                                                                                name="csrf"
                                                                                                                                value="<%= Input.html(csrf) %>">

                                                                                                                            <input
                                                                                                                                type="hidden"
                                                                                                                                name="action"
                                                                                                                                value="<%= Input.html(action) %>">

                                                                                                                            <input
                                                                                                                                type="hidden"
                                                                                                                                name="id"
                                                                                                                                value="<%= Input.html(recordId) %>">


                                                                                                                            <% if
                                                                                                                                (
                                                                                                                                statuses.length>
                                                                                                                                0
                                                                                                                                )
                                                                                                                                {
                                                                                                                                %>


                                                                                                                                <label>

                                                                                                                                    New
                                                                                                                                    Status

                                                                                                                                    <select
                                                                                                                                        name="status">

                                                                                                                                        <% for
                                                                                                                                            (
                                                                                                                                            String
                                                                                                                                            status
                                                                                                                                            :
                                                                                                                                            statuses
                                                                                                                                            )
                                                                                                                                            {
                                                                                                                                            %>

                                                                                                                                            <option
                                                                                                                                                value="<%= Input.html(status) %>">

                                                                                                                                                <%= Input.html(
                                                                                                                                                    status.replace( '_'
                                                                                                                                                    , ' '
                                                                                                                                                    )
                                                                                                                                                    )
                                                                                                                                                    %>

                                                                                                                                            </option>

                                                                                                                                            <% }
                                                                                                                                                %>

                                                                                                                                    </select>

                                                                                                                                </label>


                                                                                                                                <% }
                                                                                                                                    %>


                                                                                                                                    <label>

                                                                                                                                        <%= "ticket-response"
                                                                                                                                            .equals(
                                                                                                                                            action
                                                                                                                                            )
                                                                                                                                            ? "Response to Customer"
                                                                                                                                            : "Reason"
                                                                                                                                            %>

                                                                                                                                            <textarea
                                                                                                                                                name="reason"
                                                                                                                                                maxlength="500"
                                                                                                                                                required></textarea>

                                                                                                                                    </label>


                                                                                                                                    <button
                                                                                                                                        type="submit">

                                                                                                                                        <%= Input.html(
                                                                                                                                            action.replace( '-'
                                                                                                                                            , ' '
                                                                                                                                            )
                                                                                                                                            )
                                                                                                                                            %>

                                                                                                                                    </button>

                                                                                                                        </form>


                                                                                                                        <% }
                                                                                                                            %>


                                                                                            </article>


                                                                                            <% } %>


                                                                            </section>


                                                                            <% } } %>


                                                                                <!-- ========================================= -->
                                                                                <!-- FOOTER -->
                                                                                <!-- ========================================= -->

                                                                                <footer class="dashboard-footer">

                                                                                    <span>
                                                                                        LankaTrust Banking System
                                                                                    </span>

                                                                                    <span>
                                                                                        Secure Banking Operations
                                                                                    </span>

                                                                                </footer>


                    </main>

                </body>

                </html>