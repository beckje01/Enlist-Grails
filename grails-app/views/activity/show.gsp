
<%@ page import="enlist.grails.Activity" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="bootstrap">
		<g:set var="entityName" value="${message(code: 'activity.label', default: 'Activity')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="row-fluid">
			
			<div class="span3">
				<div class="well">
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li>
							<g:link class="list" action="list">
								<i class="icon-list"></i>
								<g:message code="default.list.label" args="[entityName]" />
							</g:link>
						</li>
						<li>
							<g:link class="create" action="create">
								<i class="icon-plus"></i>
								<g:message code="default.create.label" args="[entityName]" />
							</g:link>
						</li>
					</ul>
				</div>
			</div>
			
			<div class="span9">

				<div class="page-header">
					<h1><g:message code="default.show.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				    <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>
                <g:if test="${canVolunteer}">
                    <div class="sign-up">
                        <g:form class="form-search" action="${hasSignUp ? 'cancelSignUp' : 'signUp'}" id="${activityInstance?.id}">
                            <g:if test="${hasSignUp}">
                                <button type="submit" class="btn">Cancel Sign Up</button>
                            </g:if>
                            <g:else>
                                <button type="submit" class="btn btn-info">Sign Up</button>
                            </g:else>
                        </g:form>
                    </div>
                </g:if>


				<dl>
				
					<g:if test="${activityInstance?.coordinators}">
						<dt><g:message code="activity.coordinators.label" default="Coordinators" /></dt>
						
							<g:each in="${activityInstance.coordinators}" var="c">
							<dd><g:link controller="user" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></dd>
							</g:each>
						
					</g:if>
				
					<g:if test="${activityInstance?.description}">
						<dt><g:message code="activity.description.label" default="Description" /></dt>
						
							<dd><g:fieldValue bean="${activityInstance}" field="description"/></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.endDate}">
						<dt><g:message code="activity.endDate.label" default="End Date" /></dt>
						
							<dd><g:formatDate date="${activityInstance?.endDate}" /></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.event}">
						<dt><g:message code="activity.event.label" default="Event" /></dt>
						
							<dd><g:link controller="event" action="show" id="${activityInstance?.event?.id}">${activityInstance?.event?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.location}">
						<dt><g:message code="activity.location.label" default="Location" /></dt>
						
							<dd><g:fieldValue bean="${activityInstance}" field="location"/></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.numPeopleNeeded}">
						<dt><g:message code="activity.numPeopleNeeded.label" default="Num People Needed" /></dt>
						
							<dd><g:fieldValue bean="${activityInstance}" field="numPeopleNeeded"/></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.points}">
						<dt><g:message code="activity.points.label" default="Points" /></dt>
						
							<dd><g:fieldValue bean="${activityInstance}" field="points"/></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.pointsType}">
						<dt><g:message code="activity.pointsType.label" default="Points Type" /></dt>
						
							<dd><g:fieldValue bean="${activityInstance}" field="pointsType"/></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.startDate}">
						<dt><g:message code="activity.startDate.label" default="Start Date" /></dt>
						
							<dd><g:formatDate date="${activityInstance?.startDate}" /></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.title}">
						<dt><g:message code="activity.title.label" default="Title" /></dt>
						
							<dd><g:fieldValue bean="${activityInstance}" field="title"/></dd>
						
					</g:if>
				
					<g:if test="${activityInstance?.volunteers}">
						<dt><g:message code="activity.volunteers.label" default="Volunteers" /></dt>
						
							<g:each in="${activityInstance.volunteers}" var="v">
							<dd><g:link controller="user" action="show" id="${v.id}">${v?.encodeAsHTML()}</g:link></dd>
							</g:each>
						
					</g:if>
				
				</dl>

				<g:form>
					<g:hiddenField name="id" value="${activityInstance?.id}" />
					<div class="form-actions">
						<g:link class="btn" action="edit" id="${activityInstance?.id}">
							<i class="icon-pencil"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="btn btn-danger" type="submit" name="_action_delete">
							<i class="icon-trash icon-white"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>

			</div>

		</div>
	</body>
</html>
