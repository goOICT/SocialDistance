# OpenTrace - Social Distance Alarm
<img src="https://github.com/kunai-consulting/OpenTrace/blob/master/play_store.png" width="100"> 
Inspired by TraceTogether and COVID Symptom Tracker, and written in response to COVID19.  OpenTrace is (now) a Social Distancing Application that detects the presence of other BLE handsets and devices and alarms the user to thier proximity.  The goal of this application is to create a completely transparent application that can be used to save both lives and livelihoods.

We are a team of product developers. We want to help and are doing what we can. 
Currently looking for teams and executives to partner with to build and deploy this solution. Currently actively working to create partnerships with people at MIT, UCSF, State of California, and inviting executives from UCSF, Blue Shield California, FedEx, Amazon, Kroger, Aldi, Target, Walmart and others to create partnerships to help build a public use version for governments and healthcare institutes as well as versions for Companies who employ essential and other types of workers. Eventually, we will all need to get back to work. This could help people do it more safely.

![Image of Social Distancing App Design](https://github.com/kunai-consulting/OpenTrace/blob/master/Social-distancing-app-flow.png)

# Product Description and Vision
## Problem Statement
People need to get back to work eventually, and also many are essential workers continue to work through this global pandemic dealing with Covid-19 that we now find ourselves in. We need them not to get sick so our supply chain stays running and peoples risk is minimized, and we can flatten the curve. We started building a contact tracing app for the Workforce Protection, and pivoted recently to building a Social Distancing app to help protect people and workforces. Contact tracing could easily be added on top of the social distancing features. (We would add and pair a symptom checker, and contact tracing notification capabilities to protect workers and the public.)

## Hypothesis
Companies can deploy social distancing and contact tracing apps to help protect their employees during Covid-19. Using BLE, a Social Distancing app coupled with testing, Contact Tracing, and other worker protections, coverage for sick-leave, temperature checks for employees and other techniques (which companies are starting to implement), will all help companies to protect their essential workers and help to flatten the curve.

We think there is a use case for employers and individuals to use a social distancing apps (and eventually contact tracing apps) to help protect employees in addition to the public health organization apps that will be delivered through the App Stores on the Google/Apple API. 
Contacty tracing only works to mitigate unknown transmisison after people are infected it is reactive. 
Social Distancing is proactive, and may help with prevention. We hypothesize that companies will want to have their own app so they are not asking employees to give up their personal health and tracing data to their employers. We think employer based contact tracing app deployments will help with this because when the pandemic is over, because employees can delete their app.

This may help companies add workforce protection for workers while not promoting workers to sacrifice their personal cell data.

We are building an MVP as well as prototyping improvements to BLE implementation, to lengthen battery life and optimize performance, and can quickly include contact tracing on top of that to implement for organizations and companies.

Apple and [Google are delivering interoperable API's](https://blog.google/inside-google/company-announcements/apple-and-google-partner-covid-19-contact-tracing-technology) that should well protect the privacy and mitigate concerns, creating a standard and a path for official organizations to create and publish apps through Apple and Google Play stores. Data Rights best practices have emerged for contact tracing that developers can comport to.


### Users Personas: Essential personnel and eventually all personnel.
Grocery Stockers checkers and workers

Caltrans road and municipal workers

Amazon warehouse workers

Security Guards

Hospital Staff

Healthcare workers, dentists, Physical therapists.


### Who is our target organization to deliver this for?
Amazon, Walmart, Kaiser, Kroger, Aldi, Safeway, One Medical, Restaurants Chains, Blue Shield Stanford Medical Center, Salesforce(s customers), etc.. Hospitals, Counties, States, Countries.

## Social Distancing
The app uses BLE to tell you when other app users are too close to help you maintain social distance.
The app can be deployed by an organization made available to be downloaded by individual workers. Individuals may opt in to application use and have granular and transparent data and privacy control’s.

### Features
* App notifies users for when another app user is close. 4 levels of granularity. "Good Distance"= green, "Warning"= yellow, "Danger" = Orange, "Too Close"= Red
* Alerts show signal strength and date and time when other devices detected.
* Coming next...History - App stores a local history of the alerts from coming close to other app user devices. Date and time.

### Roadmap
* Set your Social Distance. High risk users can set thier social distance 'radious' to be more sensitive that the CDC recommended 6 feet, so that they are notified for 'Too Close' app users sooner.
* History - Allow users to see the location and map of where a Social Distant alert/contact happened. This data only stored locally (on their handset). 
* Safe Team - Add users to your 'Safe Team' using secure QR codes for each handset. This allows families who are safely socially distancing together to not recieve notifications of eachothers hansets when they are together, but out in public. E.G. A Couple shopping together in a store where all shoppers have the app downloaded and turned on.
* Employers who deploy this could have option for users to opt in to sending location data for sessions that occured at the workplace if users wanted to contribute to mapping areas in the workplace where social distancing is not possible. Per session.


# Future Versions 
## Add Contact Tracing
App can use contact tracing to notify users when they have had contact with someone who has been tested and who's status is verified as positive for Covid-19. User can then get tested.

This app can be deployed by an organization made available to be downloaded by individual workers. Individuals may opt in to application use and have granular and transparent data and privacy control’s.

## Symptom Checker features possible if helpful.
Users go through and answer the symptom checker every time they open the app.
App recommends user self distance/isolate or quarantine if they have any symptoms or issues.
This is mostly built, but we are leaving it out of our v1 to focus on Social Distance features first.

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


## Self Isolation/Distancing  Clock
Start day,
Current day
End day
Restart clock if newly exposure.


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
