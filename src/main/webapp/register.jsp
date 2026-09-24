<%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <!DOCTYPE html>
    <html lang="en">

    <head>
        <script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>Create Account | LankaTrust Digital Banking</title>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/register.css">
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/lankatrust.css">
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/banking-system.css">
        <script defer src="<%= request.getContextPath() %>/assets/js/banking-ui.js"></script>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/design-system.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/workspaces.css">
<script defer src="<%= request.getContextPath() %>/assets/js/workspaces.js"></script>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/public.css">
</head>

    <body class="auth-page">
        <a class="auth-home" href="<%= request.getContextPath() %>/">&larr; LankaTrust home</a>

        <div class="register-page">

            <!-- ================= LEFT SIDE ================= -->

            <section class="brand-panel">

                <div class="brand">

                    <div class="logo-box">LT</div>

                    <div class="brand-name">
                        <h2>LankaTrust</h2>
                        <span>DIGITAL BANKING</span>
                    </div>

                </div>


                <div class="brand-content">

                    <p class="small-title">
                        PERSONAL BANKING
                    </p>

                    <h1>
                        Your financial future
                        <span>starts here.</span>
                    </h1>

                    <p class="description">
                        Open your LankaTrust banking account securely and
                        access modern digital banking services from anywhere.
                    </p>


                    <div class="features">

                        <div class="feature">
                            <strong>Protected</strong>
                            <span>Customer operations</span>
                        </div>

                        <div class="feature">
                            <strong>Personal</strong>
                            <span>Digital Banking</span>
                        </div>

                        <div class="feature">
                            <strong>Role-based</strong>
                            <span>Access control</span>
                        </div>

                    </div>

                </div>


                <div class="brand-footer">
                    Secure • Reliable • Intelligent Banking
                </div>

            </section>



            <!-- ================= RIGHT SIDE ================= -->

            <section class="form-panel">

                <div class="form-container">

                    <p class="eyebrow">
                        JOIN LankaTrust
                    </p>

                    <h2>
                        Create your account
                    </h2>

                    <p class="subtitle">
                        Enter your details to start your digital banking journey.
                    </p>


                    <!-- ERROR FROM SERVLET -->

                    <% Object error=request.getAttribute("error"); if (error !=null) { %>

                        <div class="alert error">
                            <%= error %>
                        </div>

                        <% } %>


                            <!-- ================= REGISTER FORM ================= -->

                            <form action="<%= request.getContextPath() %>/register" method="post" id="registerForm">


                                <div class="form-grid">


                                    <!-- FULL NAME -->

                                    <div class="form-group full">

                                        <label for="name">
                                            Full Name *
                                        </label>

                                        <input id="name" type="text" name="name" placeholder="Enter your full name"
                                            autocomplete="name" required>

                                    </div>



                                    <!-- EMAIL -->

                                    <div class="form-group">

                                        <label for="email">
                                            Email Address *
                                        </label>

                                        <input id="email" type="email" name="email" placeholder="name@example.com"
                                            autocomplete="email" required>

                                    </div>



                                    <!-- PHONE -->

                                    <div class="form-group">

                                        <label for="phone">
                                            Phone Number *
                                        </label>

                                        <input id="phone" type="tel" name="phone" placeholder="+94 77 123 4567"
                                            autocomplete="tel" required>

                                    </div>



                                    <!-- DATE OF BIRTH -->

                                    <div class="form-group">

                                        <label for="dateOfBirth">
                                            Date of Birth
                                        </label>

                                        <input id="dateOfBirth" type="date" name="dateOfBirth">

                                    </div>



                                    <!-- ACCOUNT TYPE -->

                                    <div class="form-group">

                                        <label for="accountType">
                                            Account Type *
                                        </label>

                                        <select id="accountType" name="accountType" required>

                                            <option value="">
                                                Select account type
                                            </option>

                                            <option value="SAVINGS">
                                                Savings Account
                                            </option>

                                            <option value="CURRENT">
                                                Current Account
                                            </option>

                                        </select>

                                    </div>



                                    <!-- ADDRESS -->

                                    <div class="form-group full">

                                        <label for="address">
                                            Address
                                        </label>

                                        <input id="address" type="text" name="address"
                                            placeholder="Enter your residential address" autocomplete="street-address">

                                    </div>



                                    <!-- CITY -->

                                    <div class="form-group">

                                        <label for="city">
                                            City
                                        </label>

                                        <input id="city" type="text" name="city" placeholder="Colombo"
                                            autocomplete="address-level2">

                                    </div>



                                    <!-- POSTAL CODE -->

                                    <div class="form-group">

                                        <label for="postalCode">
                                            Postal Code
                                        </label>

                                        <input id="postalCode" type="text" name="postalCode" placeholder="00100"
                                            autocomplete="postal-code">

                                    </div>



                                    <!-- PASSWORD -->

                                    <div class="form-group">

                                        <label for="password">
                                            Password *
                                        </label>

                                        <div class="password-box">

                                            <input id="password" type="password" name="password"
                                                placeholder="Minimum 8 characters" minlength="8"
                                                autocomplete="new-password" required>

                                            <button type="button" class="show-password"
                                                onclick="togglePassword('password', this)">
                                                Show
                                            </button>

                                        </div>

                                    </div>



                                    <!-- CONFIRM PASSWORD -->

                                    <div class="form-group">

                                        <label for="confirmPassword">
                                            Confirm Password *
                                        </label>

                                        <div class="password-box">

                                            <input id="confirmPassword" type="password" name="confirmPassword"
                                                placeholder="Repeat your password" minlength="8"
                                                autocomplete="new-password" required>

                                            <button type="button" class="show-password"
                                                onclick="togglePassword('confirmPassword', this)">
                                                Show
                                            </button>

                                        </div>

                                    </div>


                                </div>



                                <!-- TERMS -->

                                <label class="terms">

                                    <input type="checkbox" id="terms" required>

                                    <span>
                                        I agree to the Terms & Conditions and Privacy Policy.
                                    </span>

                                </label>



                                <!-- REGISTER BUTTON -->

                                <button type="submit" class="create-btn">

                                    <span>
                                        Create Secure Account
                                    </span>

                                    <span class="arrow">
                                        →
                                    </span>

                                </button>



                                <div class="security-message">
                                    ◆ Your personal information is securely protected.
                                </div>

                            </form>



                            <!-- LOGIN LINK -->

                            <div class="login-link">

                                Already have an account?

                                <a href="<%= request.getContextPath() %>/login.jsp">
                                    Sign in →
                                </a>

                            </div>

                </div>

            </section>

        </div>


        <script src="<%= request.getContextPath() %>/assets/js/register.js"></script>

    </body>

    </html>