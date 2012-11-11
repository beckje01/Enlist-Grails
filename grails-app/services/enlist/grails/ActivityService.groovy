package enlist.grails

import org.springframework.transaction.annotation.Transactional
import enlist.grails.util.DateParser
import org.apache.commons.lang.StringUtils

class ActivityService {
    static transactional = false

    @Transactional
    Date batchCalculateActivityPoints(BatchJobParameter batchJobParameter ) {
        Date lastActEndDate = batchJobParameter ? DateParser.parseDateTimeDefault(batchJobParameter.value) : null
        def actList = Activity.createCriteria().list {
            isNotNull("endDate")
            lte("endDate", new Date())
            if(lastActEndDate) gt("endDate", lastActEndDate)
            order "endDate", "asc"
        }
        Date latestEndDate = lastActEndDate
        Map<Long, User> userMap = new HashMap<Long, User>()
        Map<Long, Integer> userAmtUpdateMap = new HashMap<Long, Integer>()
        for(Activity act : actList) {
            def signUpList = ActivitySignUp.findAllByActivity(act)
            for(ActivitySignUp signUp : signUpList) {
                Integer point = calculatePoint(signUp.activity)
                UserActivityHistory.create(signUp.user, signUp.activity, calculatePoint(signUp.activity))
                PointTransaction txn = new PointTransaction( acctOwner: signUp.user, txnDate: new Date(),
                        txnType: PointTransaction.VOLUNTEER, amount: point,
                        description: "${signUp.activity?.event} ${signUp.activity.title}")
                txn.save(failOnError: true, validate: false)
                ActivitySignUp.remove(signUp.user, signUp.activity)
                userMap.put(signUp.user.id, signUp.user)
                userAmtUpdateMap.put(signUp.user.id, (userAmtUpdateMap.get(signUp.user.id) ?: 0) + point)
            }
            if(latestEndDate == null || latestEndDate.time < act.endDate.time)
                if(!signUpList.isEmpty()) latestEndDate = act.endDate
        }
        userAmtUpdateMap.each{
            Long id, Integer points ->
            User usr = userMap.get(id)
            usr.currPoints = (usr.currPoints?:0) + points
            usr.save(failOnError: true, validate: false)
        }
        return latestEndDate
    }
    /**
     * TODO change the calculation
     * @param activity
     * @return
     */
    Integer calculatePoint(Activity activity) {
        if(StringUtils.equals(activity.pointsType, "Hourly")) {
            long time = activity.endDate.time - activity.startDate.time  // millis
            return (time / 3600000) * activity.points
        }
        return activity.points
    }

    def mailService
    @Transactional
    Date batchSendEmailReminder(BatchJobParameter batchJobParameter) {
        Date lastReminder = batchJobParameter ? DateParser.parseDateTimeDefault(batchJobParameter.value) : null
        def signUpList = ActivitySignUp.createCriteria().list {
            isNotNull("reminderAt")
            lte("reminderAt", new Date())
            if(lastReminder) gt("reminderAt", lastReminder)
            order "reminderAt", "asc"
        }
        def userSignUpMap = [:]
        Status active = Status.findWhere(status: 'Active')
        for(ActivitySignUp signUp : signUpList) {
            if(!StringUtils.isBlank(signUp.user?.email) && signUp.user?.status.id == active.id) {
                String email =signUp.user.email.toLowerCase()
                def activities = userSignUpMap.get(email) ?: []
                activities << signUp.activity
                userSignUpMap.put(email,activities)
            }
            if(lastReminder == null || lastReminder.time < signUp.reminderAt.time) lastReminder = signUp.reminderAt
        }
        userSignUpMap.each { String email, def activities ->
             sendEmailReminderForMultipleEvents(email, activities)
        }
        lastReminder
    }
    void sendEmailReminderForMultipleEvents(String userEmail, def activities) {
        log.debug("send email to ${userEmail}")
        String content = "Upcoming event's activities:\n"
        activities.each { Activity it ->
            content += " ** '${it.title}' @ ${it.location} will start at: ${it.startDate}"
        }
        try {
            mailService.sendMail {
                to userEmail
                subject "[Enlist Reminder] Get Ready!"
                body content
            }
        } catch(Exception e) {
            e.printStackTrace()
            while(e.cause != null) e = e.cause
            log.error("Failed to send email. ${e.message}")
        }
    }
}
