<%--
  Created by IntelliJ IDEA.
  User: ACER
  Date: 10/27/2021
  Time: 5:30 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resource/style.css">
</head>
<body>
<c:import url="/WEB-INF/jsp/header.jspf"/>
<form action="getEventsByOrganizer" method="get">
    <input type="text" placeholder="organizerId" name="organizerId" value="${param.organizerId}">
    <input type="submit" value="seachByOrganizer" />
</form>
<c:url context="/support/admin" value="/events" var="getAllURL"/>
<a href="${getAllURL}">Get all events</a>
<br>
<c:if test="${empty eventPage.content}">
    There's no event
</c:if>
<c:if test="${not empty eventPage.content}">
    <table>
        <thead>
        <th>Event Id</th>
        <th>Owner Id</th>
        <th>Title</th>
        <th>Start Date</th>
        <th>End date</th>
        <th>Status</th>
        <th></th>
        </thead>
        <tbody>
        <c:forEach items="${eventPage.content}" var="ev">
            <c:url context="/support/admin" value="/deleteEvent?eventId=${ev.id}" var="deleteURL"/>
            <tr>
                <td>${ev.id}</td>
                <td>${ev.userProfileId}</td>
                <td>${ev.title}</td>
                <td>${ev.startDate}</td>
                <td>${ev.endDate}</td>
                <td>${ev.status.name}</td>
                <td><a href="${deleteURL}">Delete</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <p>
        Number of records: ${eventPage.totalElements}
        Current page: ${eventPage.number}
    </p>


    <c:if test="${param.organizerId != null}">
        <c:if test="${eventPage.hasPreviousPage()}">
            <c:url context="/support/admin" value="/getEventsByOrganizer?organizerId=${param.organizerId}&page=${eventPage.number-1}" var="prePageURL" />
            <a href="${prePageURL}">Prev</a>
        </c:if>
        <c:if test="${eventPage.hasNextPage()}">
            <c:url context="/support/admin" value="/getEventsByOrganizer?organizerId=${param.organizerId}&page=${eventPage.number+1}" var="nextPageURL" />
            <a href="${nextPageURL}">Next</a>
        </c:if>
        <c:url context="/support/admin" value="/getEventsByOrganizer?organizerId=${param.organizerId}&page=0" var="firstPage" />
        <c:url context="/support/admin" value="/getEventsByOrganizer?organizerId=${param.organizerId}&page=${eventPage.totalPages-1}" var="lastPage" />
    </c:if>
<c:if test="${param.title == null && param.organizerId == null}">
    <c:if test="${eventPage.hasPreviousPage()}">
        <c:url context="/support/admin" value="/events?page=${eventPage.number-1}" var="prePageURL" />
        <a href="${prePageURL}">Prev</a>
    </c:if>
    <c:if test="${eventPage.hasNextPage()}">
        <c:url context="/support/admin" value="/events?page=${eventPage.number+1}" var="nextPageURL" />
        <a href="${nextPageURL}">Next</a>
    </c:if>
    <c:url context="/support/admin" value="/events?page=0" var="firstPage" />
    <c:url context="/support/admin" value="/events?page=${eventPage.totalPages-1}" var="lastPage" />
</c:if>
<c:if test="${param.title!=null || param.startDate!=null || param.endDate!=null}">
    <c:if test="${eventPage.hasPreviousPage()}">
        <c:url context="/support/admin" value="/searchEvent?title=${param.title}&startDate=${param.startDate}&endDate=${param.endDate}&page=${eventPage.number-1}" var="prePageURL" />
        <a href="${prePageURL}">Prev</a>
    </c:if>
    <c:if test="${eventPage.hasNextPage()}">
        <c:url context="/support/admin" value="/searchEvent?title=${param.title}&startDate=${param.startDate}&endDate=${param.endDate}&page=${eventPage.number+1}" var="nextPageURL" />
        <a href="${nextPageURL}">Next</a>
    </c:if>
    <c:url context="/support/admin" value="/searchEvent?title=${param.title}&startDate=${param.startDate}&endDate=${param.endDate}&page=0" var="firstPage" />
    <c:url context="/support/admin" value="/searchEvent?title=${param.title}&startDate=${param.startDate}&endDate=${param.endDate}&page=${eventPage.totalPages-1}" var="lastPage" />
</c:if>

<br>
<a href="${firstPage}">First page</a>
<a href="${lastPage}">Last page</a>
</c:if>

<%--<hr>--%>
<%--<h3>Search event form</h3>--%>
<%--<form action="searchEvent" method="get">--%>
<%--    <input type="text" placeholder="TITLE" name="title" value="${param.title}">--%>
<%--    <input type="date" placeholder="START DATE" name="startDate" value="${param.startDate}">--%>
<%--    <input type="date" placeholder="END DATE" name="endDate" value="${param.endDate}">--%>
<%--    <input type="submit" value="SEARCH">--%>
<%--</form>--%>
</body>
</html>
