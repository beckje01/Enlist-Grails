package enlist.grails

class Activity {

	String title
	String description
	Integer numPeopleNeeded = 1
	Date startDate
	Date endDate
	String location
	Address locationAddress
	Event event
	String pointsType
	Integer points = 0
	Boolean featured = false

	static searchable = {
		startDate index: "analyzed", format: 'MM/dd/yyyy'
		event component: true
	}

	static hasMany = [coordinators: User]

	static embedded = ['locationAddress']

	static transients = ['countOfVolunteers','remainingVolunteerSlots']

	static mapping = {
		endDate index: 'batchActivityIdx'
	}

	static constraints = {
		title(blank: false
			, size: 3..100
		)
		description(maxLength: 500)
		numPeopleNeeded(size: 1..256)

		location(blank: false)
		event(nullable: false)
		locationAddress(nullable: true)
		endDate(validator: {val, obj, errors ->
			if (obj.endDate && obj.startDate) {
				if (obj.endDate.time <= obj.startDate.time) {
					obj.errors.reject("enddate.shouldbe.after.startdate", "End date should be after the start date.");
				}
			}
		})
		points min: 0
	}

	def getVolunteers() {
		return ActivitySignUp.findAllByActivityId(this.id)

	}

	Integer getCountOfVolunteers() {
		return ActivitySignUp.countByActivity(this)
	}

	Integer getRemainingVolunteerSlots() {
		return numPeopleNeeded - countOfVolunteers
	}
}
