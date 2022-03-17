# JiraHelloWorld

## Introduction

Redpen Jenkins plugin is an open-sourced plugin that is used to integrate Jenkins with [Jira Software](https://www.atlassian.com/software/jira). This plugin is free to use and provides an easy, secure, and reliable way to way to send build and deployment information from your Jenkins server to your Jira Software. The pipeline reports the build and deployment status (failure or success) with other meaningful data into the Jira issues which helps your team to track and collaborate on the work.

## Benefits
 - This plugin integration gives visibility to your team of the CI/CD pipelines related to Jira issues.

 - The plugin reports the build and deployment-related data directly to the Jira issues. So, it is **important** that the branch that triggers the Jenkins pipeline **must have the Jira ticket number in the branch name.** (e.g. [TEST-1234] Deploy v1.2 to production)

 - The plugin eliminates the manual work required by **Software Engineers** to update the Jira issues with the pipeline details.
 
 - The plugin provides visibility to **Product Managers, Team Leads,** and **QA Engineers** into the pipeline like failing or passing builds, tests, and deployments.

## How to get started
There are four steps to get started with the integration:

1. Install Redpen Jenkins Plugin

2. Generate a new Jenkins Credentials 
3. Generate Jira API token of an Atlassian user account 
4. Configure Redpen Jenkins Plugin into a Jenkins Job

[Click here](https://support.redpen.ai/hc/en-us/articles/4727856045069) and follow the steps to integrate and configure the plugin.

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE)

Please [contact us](https://www.redpen.ai/contact-us) if you have any questions/queries. 