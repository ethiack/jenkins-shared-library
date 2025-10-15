<!-- MARKDOWN LINKS & IMAGES -->

[version-shield]: https://img.shields.io/github/v/release/ethiack/jenkins-shared-library?style=for-the-badge
[version-url]: https://github.com/ethiack/jenkins-shared-library/releases/latest

[license-shield]: https://img.shields.io/github/license/ethiack/jenkins-shared-library?style=for-the-badge
[license-url]: LICENSE

[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/company/ethiack


<!-- README -->
<a name="readme-top"></a>
<div align="center">

<h1>
  <br>
    <img src="assets/logo.png" alt="logo" width="400">
    <br><br>
    Ethiack Jenkins Shared Library
    <br><br>
</h1>

<h4>Integration of Ethiack's Public API with Jenkins CI/CD</h4>

[![GitHub Release][version-shield]][version-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<hr />


[Introduction](#introduction) •
[Installation](#installation) •
[Credentials Setup](#credentials-setup) •
[Usage](#usage) •
[License](#license)

</div>


## Introduction

This Jenkins shared library facilitates the integration of [Ethiack's Public API](https://api.ethiack.com) ([API docs](https://portal.ethiack.com/docs/api/)) for launching scans through [Jenkins](https://www.jenkins.io/) pipelines. By using this library, you can seamlessly incorporate Ethiack's security scanning capabilities into your Jenkins workflows, enhancing your CI/CD pipeline with automated security testing.

<p align="right"><small>(<a href="#readme-top">back to top</a>)</small></p>


## Installation

> [!NOTE]
> For more information on these and other installation methods, please refer to the Jenkins' extensive [documentation on shared libraries](https://www.jenkins.io/doc/book/pipeline/shared-libraries/).


###  __Method #1__: Using [*Global Shared Libraries*](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#global-shared-libraries) *(Recommended)*

1. Navigate to `Dashboard » Manage Jenkins » System » Global Pipeline Libraries` on the Jenkins dashboard.
2. Under the *__Global Pipeline Libraries__* section, provide the following configuration:

    > __Name__: `ethiack-library`  
    > __Default Version__: `main`  
    > __Load implicitly__: &#9744;   
    > __Allow default version to be overridden__: &#9745;  
    > __Include @Library changes in job recent changes__: &#9745;  
    > __Cache fetched versions on controller for quick retrieval__: &#9744;
    > 
    >  
    > __Retrieval Method__: `Modern SCM`
    > > __Source Code Management__: `Git`
    > > > __Project Repository__: `https://github.com/ethiack/jenkins-library`
    > > > __Credentials__: `- none -`
    > >
    > >__Fresh clone per build__:  &#9745;  
    > > 
    > 
    > # 
  
The shared library can then be imported in a pipeline with: 
```groovy
@Library("ethiack-library") _
```


### __Method #2__: Using the [*Github Groovy Libraries*](https://plugins.jenkins.io/pipeline-github-lib/) plugin 

The [*Github Groovy Libraries*](https://plugins.jenkins.io/pipeline-github-lib/) plugin allows pipelines to be loaded on the fly from GitHub repositories. If the plugin is installed, simply import the library repository in the working pipeline:

```groovy filename="Jenkins Pipeline"
@Library('github.com/ethiack/jenkins-library@main') _
```

<p align="right"><small>(<a href="#readme-top">back to top</a>)</small></p>


## Credentials Setup

Using Ethiack's API - and, therefore, this shared library - requires authentication using an *API Key* and *API Secret*, which can be retrieved in [Ethiack's Portal settings page](https://portal.ethiack.com/settings/api). These credentials must be available as environment variables `ETHIACK_API_KEY` and `ETHIACK_API_SECRET`, repectively, whenever the shared library is used.

To setup these credentials in Jenkins:

1. Navigate to `Dashboard » Manage Jenkins » Credentials` on the Jenkins homepage.
2. Select the desired store/domain
3. Add *API Key*:
   > __New Credential__
   > 
   > __Kind__: `Secret Text`  
   > > __Secret__: [YOUR API KEY]  
   > > __ID__:   `ethiack-api-key`  
   > > __Description__: *Optional* 
4. Add *API Secret*:
   > __New Credential__
   > 
   > __Kind__: `Secret Text`  
   > > __Secret__: [YOUR API SECRET]  
   > > __ID__:   `ethiack-api-secret`  
   > > __Description__: *Optional* 

> [!CAUTION]
> Ensure that the store/domain used for storing the *API Key* and the *API Secret* has an adequate scope and  access rules.

> [!NOTE]
> For more information, see https://www.jenkins.io/doc/book/using/using-credentials/.

<p align="right"><small>(<a href="#readme-top">back to top</a>)</small></p>


## Usage

> [!NOTE]
> This shared library is fundamentally a wrapper around [Ethiack's Public API](https://api.ethiack.com/). For more information, see the [API docs](https://portal.ethiack.com/docs/api/).


### Example: *Launching a job and waiting for its conclusion*

This pipeline launches a scan for the domain  `https://example.ethiack.com` and waits until it finishes.

```groovy
@Library("ethiack-library")_
import groovy.json.JsonSlurper

pipeline {
    agent any
    
    environment {
        ETHIACK_API_KEY = credentials('ethiack-api-key')
        ETHIACK_API_SECRET = credentials('ethiack-api-secret')
    }

    stages {
        stage('Ethiack Scan') {
            steps {
                script {                 
                    def job = ethiack.launchJob("https://example.ethiack.com:443")
                    def resp = ethiack.awaitJob(job.uuid, Severity.MEDIUM, false)
                    def job_info = ethiack.getJobInfo(job.uuid)
                    println job_info
                    if(resp.success == false) {
                        error(resp.message)
                    }
                }
            }
        }
    }
}
```

### Example: *Using beacon_id and event_slug parameters*

This example demonstrates how to use the optional parameters for tracking and integration purposes.

```groovy
@Library("ethiack-library")_
import groovy.json.JsonSlurper

pipeline {
    agent any
    
    environment {
        ETHIACK_API_KEY = credentials('ethiack-api-key')
        ETHIACK_API_SECRET = credentials('ethiack-api-secret')
    }

    stages {
        stage('Ethiack Scan') {
            steps {
                script {                 
                    def buildNumber = env.BUILD_NUMBER.toInteger()
                    def releaseVersion = "v1.2.3"
                    
                    // Launch a scan with tracking information
                    def job = ethiack.launchJob(
                        "https://example.ethiack.com:443",
                        buildNumber,  // beacon_id for build tracking
                        "release-${releaseVersion}" // event_slug for release tracking
                    )
                    
                    def resp = ethiack.awaitJob(job.uuid, Severity.MEDIUM, false)
                    if(resp.success == false) {
                        error(resp.message)
                    }
                }
            }
        }
    }
}
```


### Available commands

A complete list of the available commands is provided below.


<details open>
<summary>

#### `check`

</summary>

> __Description:__ Check if the provided URL is valid and the scan is authorized for the organization.
>
> __Signature:__ `Boolean check(String url, Integer beacon_id = null, String event_slug = null, Boolean failOnBadStatus = false) {...}`
> 
> __Parameters:__ 
> - `url`: URL to check
> - `beacon_id`: Optional beacon ID to associate with the check
> - `event_slug`: Optional event slug to associate with the check
> - `failOnBadStatus`: If true, an error will be raised if the operation fails
>
> __Returns:__
> - `true` true if URL is valid and if the organization has scan minutes available, `false` otherwise

</details>



<details open>
<summary>

#### `launchJob`

</summary>

> __Description:__ Launch a new job for the provided URL if the provided URL is valid and the organization has scan minutes available.
>
> __Signature:__ `Map launchJob(String url, Integer beacon_id = null, String event_slug = null, Boolean failOnBadStatus = true) {...}`
> 
> __Parameters:__ 
> - `url`: URL to scan.
> - `beacon_id`: Optional beacon ID to associate with the job
> - `event_slug`: Optional event slug to associate with the job
> - `failOnBadStatus`: If `true`, an error will be raised if the operation fails
> 
> __Returns:__
> 
> - Map object with the information about the launched job

</details>



<details open>
<summary>

#### `cancelJob`

</summary>

> __Description:__ Cancel a job. The status of the job will be changed to `CANCELED`.
>
> __Signature:__ `void cancelJob(String jobUuid, Boolean failOnBadStatus = true) {...}`
> 
> __Parameters:__ 
> - `jobUuid`: UUID of job to cancel
> - `failOnBadStatus`: If `true`, an error will be raised if the operation fails

</details>


<details open>
<summary>

#### `listJobs`

</summary>

> __Description:__ Get a list of jobs for the current organization.
>
> __Signature:__ `Map listJobs(Boolean failOnBadStatus = true) {...}`
> 
> __Parameters:__
> 
> - `failOnBadStatus`: If `true`, an error will be raised if the operation fails
> 
> __Returns:__
>
> - List of jobs

</details>


<details open>
<summary>

#### `getJobInfo`

</summary>

> __Description:__ Get job information, containing the status and a list of findings for the requested job UUID.
>
> __Signature:__ `Map getJobInfo(String jobUuid, Boolean failOnBadStatus = true) {...}`
> 
> __Parameters:__
>
> - `jobUuid`: UUID of the job
> - `failOnBadStatus`: If `true`, an error will be raised if the operation fails
> 
> __Returns:__
>
> - Map object with info about the job's information and its findings

</details>


<details open>
<summary>

#### `getJobStatus`

</summary>

> __Description:__ Get job status of the requested job UUID.
>
> __Signature:__ `String getJobStatus(String jobUuid, Boolean failOnBadStatus = true) {...}`
> 
> __Parameters:__
>
> - `jobUuid`: UUID of the job
> - `failOnBadStatus`: If `true`, an error will be raised if the operation fails
> 
> __Returns:__
>
> - Job status. One of: `PENDING`, `IN_PROGRESS`, `FINISHED`, `ERROR`, `CANCELED`

</details>


<details open>
<summary>

#### `getJobSuccess`

</summary>

> __Description:__ Get job success status. A job is considered unsucessful if it contains findings with a greater or equal severity to the one provided.
>
> __Signature:__ `Map getJobSuccess(String jobUuid, String severity = null, Boolean failOnBadStatus = true) {...}`
> 
> __Parameters:__
>
> - `jobUuid`: UUID of the job
> - `severity`: Minimum severity of findings for which an error should be raised. Defaults to medium.
> - `failOnBadStatus`: If `true`, an error will be raised if the operation fails
> 
> __Returns:__
>
> - Map object with job success status and message

</details>



<details open>
<summary>

#### `awaitJob`

</summary>

> __Description:__ Wait for the job to complete. The success of the job will be queried until it finishes or fails, or the job timeout is reached.
>
> __Signature:__ `Map awaitJob(String jobUuid, Integer timeout = 3600, String severity = null, Boolean quiet = true, Boolean failOnBadStatus = true) {...}`
> 
> __Parameters:__
>
> - `jobUuid`: UUID of the job
> - `timeout`: Timeout in seconds to wait for job to complete
> - `severity`: Minimum severity of findings for which an error should be raised
> - `quiet`: If `true`, retry information won't be echoed
> - `failOnBadStatus`: Ff `true`, an error will be raised if the operation fails
> 
> __Returns:__
>
> - Map object with job success status and message

</details>

<p align="right"><small>(<a href="#readme-top">back to top</a>)</small></p>


## License

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.

<p align="right"><small>(<a href="#readme-top">back to top</a>)</small></p>
