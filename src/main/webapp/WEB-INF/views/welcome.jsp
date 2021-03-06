<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=utf-8" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/views/header.jsp"/>
</head>
<body>
<div class="container">
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-6 col-sm-4 col-xs-4">
                <h2>Книги</h2>
            </div>
            <div class="col-md-6 col-sm-8 col-xs-8">
                <form action="" class="search-form" method="GET" onsubmit="this.booksSearch.value = encodeURI(this.tempField.value)">
                    <div class="cell search-field-cell">
                        <input name="booksSearch" type="hidden">
                        <input name="tempField" class="form-control" id="search" type="text"
                               placeholder="Введите название книги" value="${param.booksSearch}">
                    </div>
                    <div class="cell">
                        <button class="btn btn-primary" type="submit">
                            <span class="glyphicon glyphicon-search"></span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="container-fluid" style="margin-top: 10px">
        <div class="row">
            <c:forEach var="book" items="${books}" varStatus="loop">
                <div class="col-xs-4 col-sm-4 col-md-2 col-lg-2">
                    <div class="thumbnail ${book.owner == null ? '' : 'book-out'}">
                        <div class="thumbnail-book-picture" style="background-image: url(${contextPath}/resources/images/${book.picture})"></div>
                        <div class="caption ">
                            <h4>${book.name}</h4>
                            <div class="book-thumbnail-description" >${book.description}</div>
                            <p><a href="/bookpage?bookId=${book.id}" class="btn btn-primary" role="button">Перейти</a></p>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
    <div class="container-fluid" style="text-align: center">
        <ul class="pagination" style="display: inline-block">
            <c:if test="${param.booksPage != 1}">
                <li><a href="?booksPage=${param.booksPage - 1}&booksSearch=${param.booksSearch}">&lt; Назад</a></li>
            </c:if>
            <c:if test="${param.booksPage == 1}">
                <li li class="disabled"><a>&lt; Назад</a></li>
            </c:if>
            <c:forEach var="curr" items="${booksPageContr.pages}">
                <c:if test="${param.booksPage != curr}">
                    <li><a href="?booksPage=${curr}&booksSearch=${param.booksSearch}">${curr}</a></li>
                </c:if>
                <c:if test="${param.booksPage == curr}">
                    <li li class="disabled"><a>${curr}</a></li>
                </c:if>
            </c:forEach>
            <c:if test="${param.booksPage != booksPageContr.countPages}">
                <li><a href="?booksPage=${param.booksPage + 1}&booksSearch=${param.booksSearch}">Вперед &gt;</a></li>
            </c:if>
            <c:if test="${param.booksPage == booksPageContr.countPages}">
                <li li class="disabled"><a>Вперед &gt;</a></li>
            </c:if>
        </ul>
    </div>
</div>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="${contextPath}/resources/js/bootstrap.min.js"></script>
</body>
</html>