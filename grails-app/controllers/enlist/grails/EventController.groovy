package enlist.grails

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.context.annotation.Role
import grails.plugins.springsecurity.Secured

class EventController {

	def springSecurityService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
		log.debug "paraams: $params"
        params.max = Math.min(max ?: 10, 100)

        [eventInstanceList: Event.list(params), eventInstanceTotal: Event.count()]
    }

	@Secured(['ROLE_ADMIN', 'ROLE_CHAPTER_ADMIN', 'ROLE_ACTIVITY_COORDINATOR'])
    def create() {
        [eventInstance: new Event(params)]
    }

	@Secured(['ROLE_ADMIN', 'ROLE_CHAPTER_ADMIN', 'ROLE_ACTIVITY_COORDINATOR'])
    def save() {
//        def eventInstance = new Event(params)  // don't use this for Domain class that has Date fields.
        def eventInstance = new Event()
        bindData(eventInstance, params)
        if (!eventInstance.save(flush: true)) {
            render(view: "create", model: [eventInstance: eventInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Event'), eventInstance.id])
        redirect(action: "show", id: eventInstance.id)
    }

    def show(Long id) {
        def eventInstance = Event.get(id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), id])
            redirect(action: "list")
            return
        }

        [eventInstance: eventInstance]
    }

	@Secured(['ROLE_ADMIN', 'ROLE_CHAPTER_ADMIN', 'ROLE_ACTIVITY_COORDINATOR'])
    def edit(Long id) {
        def eventInstance = Event.get(id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), id])
            redirect(action: "list")
            return
        }

        [eventInstance: eventInstance]
    }

	@Secured(['ROLE_ADMIN', 'ROLE_CHAPTER_ADMIN', 'ROLE_ACTIVITY_COORDINATOR'])
    def update(Long id, Long version) {
        def eventInstance = Event.get(id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (eventInstance.version > version) {
                eventInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'event.label', default: 'Event')] as Object[],
                          "Another user has updated this Event while you were editing")
                render(view: "edit", model: [eventInstance: eventInstance])
                return
            }
        }

        eventInstance.properties = params

        if (!eventInstance.save(flush: true)) {
            render(view: "edit", model: [eventInstance: eventInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'event.label', default: 'Event'), eventInstance.id])
        redirect(action: "show", id: eventInstance.id)
    }

	@Secured(['ROLE_ADMIN', 'ROLE_CHAPTER_ADMIN', 'ROLE_ACTIVITY_COORDINATOR'])
    def delete(Long id) {
        def eventInstance = Event.get(id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), id])
            redirect(action: "list")
            return
        }

        try {
            eventInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'event.label', default: 'Event'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'event.label', default: 'Event'), id])
            redirect(action: "show", id: id)
        }
    }

	@Secured(['IS_AUTHENTICATED_FULLY'])
	def volunteer(Long id)
	{
		def event = Event.get(id)
		if(!event)
		{
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), id])
			return redirect(controller: 'user', action: "index")

		}

		def userInstance = springSecurityService.getCurrentUser()

		return [userInstance:userInstance,event:event]
	}
}
