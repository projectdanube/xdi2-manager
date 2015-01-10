<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

${successMsg}


<p style="color:red">${errorMsg}</p>
<p>${errorDetails}</p>
    
<c:if test="${not empty successMsg}">
	<script>
		window.close();
	</script>
</c:if>