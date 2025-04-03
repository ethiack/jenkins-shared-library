import groovy.json.JsonOutput
import com.ethiack.API
import com.ethiack.HttpResponse
import com.ethiack.Requests


/**
 * Check if provided URL is valid and authorized for the organization.
 *
 * @param url URL to check
 * @param beacon_id Optional beacon ID to associate with the check
 * @param event_slug Optional event slug to associate with the check
 * @param failOnBadStatus if true, an error will be raised if the operation fails
 * @return true if URL is valid and if the organization has scan minutes available, false otherwise
 */
Boolean check(String url, Integer beacon_id = null, String event_slug = null, Boolean failOnBadStatus = false) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);

    def payload = [url: url]
    if (beacon_id != null) {
        payload.beacon_id = beacon_id
    }
    if (event_slug != null) {
        payload.event_slug = event_slug
    }

    String json = JsonOutput.toJson(payload);
    String requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/check";
    HttpResponse response = r.doPostHttpRequestWithJson(json, requestUrl, failOnBadStatus);
    return response.statusCode >= 200 && response.statusCode < 400;
}


/**
 * Launch a new job if the provided URL is valid and the organization has scan minutes available.
 *
 * @param url URL to scan
 * @param beacon_id Optional beacon ID to associate with the job
 * @param event_slug Optional event slug to associate with the job
 * @param failOnBadStatus if true, an error will be raised if the operation fails
 * @return Map object with the information about the launched job
 */
Map launchJob(String url, Integer beacon_id = null, String event_slug = null, Boolean failOnBadStatus = true) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);

    def payload = [url: url]
    if (beacon_id != null) {
        payload.beacon_id = beacon_id
    }
    if (event_slug != null) {
        payload.event_slug = event_slug
    }

    String json = JsonOutput.toJson(payload);
    String requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/launch";
    HttpResponse response = r.doPostHttpRequestWithJson(json, requestUrl, failOnBadStatus);
    return response.body;
}


/**
 * Cancel a job. The status of the job will be changed to CANCELED
 *
 * @param jobUuid UUID of the job to cancel
 * @param failOnBadStatus if true, an error will be raised if the operation fails
 */
void cancelJob(String jobUuid, Boolean failOnBadStatus = true) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);
    String requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/$jobUuid/cancel";
    HttpResponse response = r.doPostHttpRequestWithJson("{}", requestUrl, failOnBadStatus);
}


/**
 * Get list of jobs.
 *
 * @param failOnBadStatus if true,  an error will be raised if the operation fails
 * @return list of jobs in json.
 */
Map listJobs(Boolean failOnBadStatus = true) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);
    String requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/";
    HttpResponse response = r.doGetHttpRequest(requestUrl, failOnBadStatus);
    return response.body;
}


/**
 * Get job information. Returns status and a list of findings for the requested job uuid.
 *
 * @param jobUuid UUID of the job
 * @param failOnBadStatus if true, an error will be raised if the operation fails
 * @return Map object with info about the job's information and its findings
 */
Map getJobInfo(String jobUuid, Boolean failOnBadStatus = true) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);
    String requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/$jobUuid";
    HttpResponse response = r.doGetHttpRequest(requestUrl, failOnBadStatus);
    return response.body.job;
}


/**
 * Get job status.
 *
 * @param jobUuid UUID of the job
 * @param failOnBadStatus if true, an error will be raised if the operation fails
 * @return job status. One of: PENDING, IN_PROGRESS, FINISHED, ERROR, CANCELED
 */
String getJobStatus(String jobUuid, Boolean failOnBadStatus = true) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);
    String requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/$jobUuid/status";
    HttpResponse response = r.doGetHttpRequest(requestUrl, failOnBadStatus);
    return response.body.status;
}


/**
 * Get job success status. A job is considered unsuccessful if it contains findings with a greater or equal severity to the one provided.
 *
 * @param jobUuid UUID of the job
 * @param severity severity of findings to check for. Defaults to medium.
 * @param failOnBadStatus if true, an error will be raised if the operation fails
 * @return Map object with success status and message
 */
Map getJobSuccess(String jobUuid, String severity = null, Boolean failOnBadStatus = true) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);
    String requestUrl;
    if (severity == null) {
        requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/$jobUuid/success";
    } else {
        requestUrl = "${API.BASE_URL}/${API.ENDPOINT}/$jobUuid/success?severity=$severity";
    }
    HttpResponse response = r.doGetHttpRequest(requestUrl, failOnBadStatus);
    return response.body;
}


/**
 * Wait for job to complete.
 *
 * @param jobUuid UUID of the job
 * @param timeout timeout in seconds to wait for job to complete
 * @param severity Minimum severity of findings for which errors should be raised
 * @param failOnBadStatus if true, an error will be raised if the operation fails
 * @return Map object with job success status and message
 */
Map awaitJob(String jobUuid, String severity = null, Boolean failOnBadStatus = true, Integer timeout = 3600,  Boolean quiet = true) {
    Requests r = new Requests(env.ETHIACK_API_KEY, env.ETHIACK_API_SECRET);
    steps.timeout(time: timeout, unit: "SECONDS") {
        Map response = getJobSuccess(jobUuid, severity);
        steps.waitUntil(initialRecurrencePeriod: 5000, quiet: quiet) {
            response = getJobSuccess(jobUuid, severity, failOnBadStatus);
            return response.success != null;
        }
        return response;
    }
}
