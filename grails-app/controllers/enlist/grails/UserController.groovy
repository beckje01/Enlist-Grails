package enlist.grails

import java.awt.GraphicsConfiguration.DefaultBufferCapabilities;

import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.springsecurity.Secured
import org.apache.commons.lang.StringUtils
import org.compass.core.CompassQuery
import grails.converters.JSON


class UserController {

    def springSecurityService
    def eventService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        def userInstance = springSecurityService.getCurrentUser()
        def registeredEvents = eventService.getRegisteredEvents(userInstance)
        def upcomingEvents = eventService.getUpcomingEvents(userInstance.chapter)
        render(view: "index", model: [userInstance: userInstance, registeredEvents: registeredEvents, upcomingEvents: upcomingEvents])
    }

	@Secured(['ROLE_ADMIN'])
    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [userInstanceList: User.list(params), userInstanceTotal: User.count()]
    }

	@Secured(['ROLE_ADMIN'])
    def create() {
        [userInstance: new User(params)]
    }

	@Secured(['ROLE_ADMIN'])
    def save() {
        def userInstance = new User(params)
        if (!userInstance.save(flush: true)) {
            render(view: "create", model: [userInstance: userInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        redirect(action: "show", id: userInstance.id)
    }

	@Secured(['ROLE_ADMIN'])
    def show(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        [userInstance: userInstance]
    }

	@Secured(['ROLE_ADMIN'])
    def edit(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        [userInstance: userInstance]
    }

	@Secured(['ROLE_ADMIN'])
    def update(Long id, Long version) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (userInstance.version > version) {
                userInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'user.label', default: 'User')] as Object[],
                          "Another user has updated this User while you were editing")
                render(view: "edit", model: [userInstance: userInstance])
                return
            }
        }

        userInstance.properties = params

        if (!userInstance.save(flush: true)) {
            render(view: "edit", model: [userInstance: userInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        redirect(action: "show", id: userInstance.id)
    }

	@Secured(['ROLE_ADMIN'])
    def delete(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        try {
            userInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "show", id: id)
        }
    }

	@Secured(['ROLE_ADMIN'])
    def search() {
        def keywords = params["q"]
        if(StringUtils.isEmpty(keywords)) {
            render(view: "list", model: [userInstanceList: User.list(params), userInstanceTotal: User.count()])
            return
        }
        def searchClosure = {
            queryString(keywords)
            sort(CompassQuery.SortImplicitType.SCORE)
        }
        def searchResults = User.search(searchClosure, [offset : params.offset ?: 0, max : params.max ?: 10])
        render(view: "list", model: [userInstanceList: searchResults.results, userInstanceTotal: searchResults.total])
    }

	def isUsernameAvailable(final String username) {
		def user = User.findByUsername(username)
		def result = [available: true]
		
		if (user?.id) {
			result = [available: false]
		}
		
		render result as JSON
	}
}
