# OpenTrace - Social Distance Alarm
<img src="https://github.com/kunai-consulting/OpenTrace/blob/master/play_store.png" width="100"> 
Inspired by TraceTogether and COVID Symptom Tracker, and written in response to COVID19.  OpenTrace project produced a Social Distancing Alarm Application that detects the presence of other BLE handsets and devices and alarms the user to their proximity.  The goal of this application is to create a completely transparent application that can be used to save both lives and livelihoods.

We are a team of product developers. We want to help and are doing what we can. 
Currently looking for teams and executives to partner with to build and deploy this solution. Currently actively working to create partnerships with people at MIT, UCSF, State of California, and inviting executives from UCSF, Blue Shield California, FedEx, Amazon, Kroger, Aldi, Target, Walmart and others to create partnerships to help build a public use version for governments and healthcare institutes as well as versions for Companies who employ essential and other types of workers. Eventually, we will all need to get back to work. This could help people do it more safely.

There is the free open source version "Social Disatnce Alarm. We are also partnering with teams to deploy custom solutions based on Social Distance Alarm for workforce protection for companies and organizations.

### [Download the App on Google Play Store](https://play.google.com/store/apps/details?id=ai.kun.socialdistancealarm)

### Social Distance Alarm App Images
![Image of Social Distancing App Design](https://github.com/kunai-consulting/OpenTrace/blob/master/Social-distancing-app-flow.png)

## Video Demo : ~8m
[![](http://img.youtube.com/vi/umMaZ4Q8uBM/0.jpg)](http://www.youtube.com/watch?v=umMaZ4Q8uBM "Video Demo")

# Product Description and Vision
## Problem Statement
People need to get back to work eventually. Many essential workers continue to work through this COVID-19 global pandemic that we now find ourselves in. We need them not to get sick so our supply chain stays running and peoples' risk is minimized, and we can flatten the curve. We started building a contact tracing app for the Workforce Protection, and pivoted recently to building a Social Distancing app to help protect people and workforces. Contact tracing could easily be added on top of the social distancing features. See Roadmap below.

## Hypothesis
Individuals can use Social Distance Alarm for free in groups to help them maintain social distance. 
Companies can deploy Social Distancing Alarm app along with other measures and help protect their employees during Covid-19 and beyond. Using BLE (Bluetooth Low Energy), the Social Distancing Alarm App coupled with virus testing, Contact Tracing, temperature checks, symptom tracking, sick-leave pay, and other workforce protection measures(which companies are starting to implement), will all help prevent the spread of the coronavirus, help flatten the curve, and protect people and workers.

We think there is a use case for employers, individuals, public health organizations, and governments to use Social Distancing coupled with Contact Tracing efforts put out by offical health orgs that may be delivered through App Stores on the Google/Apple API.

Contact tracing only works to mitigate unknown transmisison after people are infected it is reactive. It is effective.
Social Distancing is proactive, and may help with prevention. 

Companies can then work to protect thier teams while not saccrificing private personal data by deploying a custom version of the app with Social Distance features and others. This way they are not asking employees to give up their personal health and tracing data to their employers, fwill benefit from a custom deployment of contact tracing coupled with the Social Distance Alarm Features.

We have worked improveme BLE performance in our implementation, to lengthen battery life and optimize performance.
Contact tracing can qeasily be built by us or other teams on top of this code. The public version does not store any data centrally and keeps all data locally on the users handset.

Apple and [Google are delivering interoperable API's](https://blog.google/inside-google/company-announcements/apple-and-google-partner-covid-19-contact-tracing-technology) that should well protect the privacy and mitigate concerns for contact tracing apps, creating a standard and a path for official organizations to create and publish apps through Apple and Google Play stores. Data Rights best practices have emerged for contact tracing that developers can comport to by the [TCN Coalition](https://tcn-coalition.org/). Our app keeps both of these strategiues and best practices in mind.


### Users Personas: Essential personnel and eventually all personnel.
Grocery Stockers checkers and workers

Caltrans road and municipal workers

Construction teams and workers

Amazon and other warehouse workers

Security Guards

Hospital Staff

Healthcare workers, dentists, Physical therapists.


### Who is our target organization to deliver this for?
Amazon, Walmart, Kaiser, Kroger, Aldi, Safeway, One Medical, Restaurants Chains, Blue Shield Stanford Medical Center, Salesforce(s customers), etc.. Hospitals, Counties, States, Countries.

## Social Distancing
The app uses BLE to tell you when other app users are too close to help you maintain social distance.
The app can be deployed by an organization made available to be downloaded by individual workers. Individuals may opt in to application use and have granular and transparent data and privacy control’s.

### Features
* Detect Users - App notifies users when another app user is close. 4 levels of granularity. "Good Distance"= green, "Warning"= yellow, "Danger" = Orange, "Too Close"= Red
* Alerts - user and shows signal strength when other app user devices are detected.
* History - App stores a local history of the alerts from coming close to other app user devices. Displays Date and Time.
* Pause detection - user can turn off detection in app when not in use.

### Roadmap
* Set your Social Distance. High risk users can set thier social distance 'radious' to be more sensitive that the CDC recommended 6 feet, so that they are notified for 'Too Close' app users sooner. 
* Safe Teams - Add users to your 'Safe Team' using secure QR codes for each handset. This allows families who are safely socially distancing together to not recieve notifications of eachothers hansets when they are together, but out in public. E.G. A Couple shopping together in a store where all shoppers have the app downloaded and turned on.
* Employers who deploy this could have option for users to opt in to sending location data for sessions that occured at the workplace if users wanted to contribute to mapping areas in the workplace where social distancing is not possible, per session.

# Future Versions 
## Add Contact Tracing
App could use contact tracing to notify users when they have had contact with someone who has been tested and who's status is verified as positive for Covid-19. User can then get tested.

This app can be deployed by an organization made available to be downloaded by individual workers. Individuals may opt in to application use and have granular and transparent data and privacy control’s.

## Testing Status
Could incorporate testing status for yourself and last date of testing. This can be compared to a history of contacts that were not social distanced for users to gain visibility into thier personal risk.

## Self Isolation/Distancing  Clock
Start day,
Current day
End day
Restart clock if newly exposure.

## Symptom Checker features
We built this, but took it out when we pivoted away from Contact tracing to a Social Distance app. Users go through and answer the symptom checker every time they open the app.
App recommends user self distance/isolate or quarantine if they have any symptoms or issues.

## Symptom Checker: How do you feel?
Do you experience symptoms now? Did you start to have symptoms, or did you feel sick before today? (if so when <date picker>)

1. A runny nose
2. A Fever
3. A Dry Cough
4. Fatigue
5. Runny nose 
6. Nasal Congestion
7. Diarrhea
8. Body Aches
9. Sore Throat
10. Headache
11. Loss of Appetite
12. Shortness of breath
13. Respiratory issues

Results: Based on this chart:
CDC: recommends if you have these symptoms you get tested for covid-19.
https://www.cdc.gov/coronavirus/2019-ncov/symptoms-testing/symptoms.html 


## Stay Informed:
Resources section:

* Latest updates from your state on self isolation mandates (possibly)

* Links to resources made available by employer or org’s specific healthcare program or other employer specifics programs.

* Telehealth resources?

* Ways to volunteer or help

* With encryption - so nobody knows who anyone else is.

* Symptom list graphic

* Link to CDC: https://www.cdc.gov/coronavirus/2019-ncov/index.html 

* Link to WHO: https://www.who.int/emergencies/diseases/novel-coronavirus-2019 

* W.H.O. situation dashboard Graphic: https://experience.arcgis.com/experience/685d0ace521648f8a5beeeee1b9125cd
