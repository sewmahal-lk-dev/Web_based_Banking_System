<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.banking.util.Input" %>
<% if ("CARD_SERVICES_OFFICER".equals(session.getAttribute("role"))) { %>
<section class="panel" id="card-closure">
    <h2>Card Management - Delete / Close Card</h2>
    <form method="get">
        <input type="hidden" name="findCard" value="1">
        <label>Card ID <input type="text" name="cardId" inputmode="numeric" pattern="[0-9]+" maxlength="10" value="<%= Input.html(request.getParameter("cardId")) %>"></label>
        <p>or</p>
        <label>Card Number <input type="text" name="cardNumber" maxlength="24" placeholder="**** 1568" value="<%= Input.html(request.getParameter("cardNumber")) %>"></label>
        <button type="submit">Find Card</button>
    </form>
    <% if (request.getAttribute("cardLookupError") != null) { %>
    <p class="notice error" role="alert"><%= Input.html(request.getAttribute("cardLookupError")) %></p>
    <% }
       List<Map<String,Object>> closureCards = (List<Map<String,Object>>) request.getAttribute("closureCards");
       if (closureCards != null) {
           if (closureCards.isEmpty()) { %>
    <p class="notice" role="status">No matching card found.</p>
    <%     } else if (closureCards.size() > 50) { %>
    <p class="notice" role="status">Too many matching cards. Enter the card ID or full number.</p>
    <%     } else {
               if (closureCards.size() > 1) { %>
    <p>Multiple cards match. Check the card ID, customer and account before choosing a card.</p>
    <%         }
               for (Map<String,Object> card : closureCards) { %>
    <article class="record">
        <h3><%= closureCards.size() == 1 ? "Selected Card" : "Matching Card" %></h3>
        <dl>
            <div><dt>Card ID</dt><dd><%= Input.html(card.get("card_id")) %></dd></div>
            <div><dt>Card Number</dt><dd><%= Input.html(card.get("masked_number")) %></dd></div>
            <div><dt>Customer</dt><dd><%= Input.html(card.get("customer_name")) %></dd></div>
            <div><dt>Account</dt><dd><%= Input.html(card.get("account_number")) %></dd></div>
            <div><dt>Status</dt><dd><%= Input.html(card.get("status")) %></dd></div>
        </dl>
        <% if (List.of("ACTIVE", "BLOCKED", "EXPIRED").contains(String.valueOf(card.get("status")))) { %>
        <form method="post">
            <input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>">
            <input type="hidden" name="action" value="card-close">
            <input type="hidden" name="id" value="<%= Input.html(card.get("card_id")) %>">
            <label>Reason <textarea name="reason" maxlength="500" required></textarea></label>
            <p>Closing marks this card as cancelled and retains its history.</p>
            <button type="submit">Close / Delete Card</button>
        </form>
        <% } else { %>
        <p>This card cannot be closed in its current status.</p>
        <% } %>
    </article>
    <%         }
           }
       } %>
</section>
<% } %>
