<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <!-- DONT DELETE 4d0bafeb-5b4c-4cbb-865e-d6211de5174e-login -->
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>Cloud Manager</title>
        
        <link rel="shortcut icon" href="assets/images/favicon.ico" />

        <link href="assets/libs/bootstrap/3.2.0/css/bootstrap.min.css" rel="stylesheet">
        <link href="assets/libs/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet" type="text/css">


        <!--[if lt IE 9]>
            <script src="assets/libs/html5shiv/3.7.2/html5shiv.min.js"></script>
            <script src="assets/libs/respond.js/1.4.2/respond.min.js"></script>
        <![endif]-->
    </head>

    <body>

        <div class="container">
            <div class="row">
                <div class="col-md-6 col-md-offset-3">
                    <form class="form-signin" role="form" action="doLogin" method="post">
                        <h2 class="form-signin-heading text-center text-primary">
                            <span class="fa-stack">
                                <i class="fa fa-cloud fa-stack-2x"></i>
                                <i class="fa fa-cog fa-stack-1x fa-inverse"></i>
                            </span> 
                            Cloud Manager
                        </h2>
                        <input type="text" class="form-control" placeholder="=yourname" id="cloudName" name="cloudName" required autofocus>
                        <input type="password" class="form-control" placeholder="Password" id="password" name="password" required>
                        <br />
                        <c:if test="${'1' eq param.login_error}">
                            <div class="alert alert-danger" role="alert">
                                ${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}
                            </div>
                        </c:if>

                        <button class="btn btn-lg btn-primary btn-block" type="submit">Log In</button>
                    </form>
                </div>

            </div>
        </div>

        <script src="assets/libs/jquery/1.11.1/jquery-1.11.1.min.js"></script>
        <script src="assets/libs/bootstrap/3.2.0/js/bootstrap.min.js"></script>
    </body>

    </html>