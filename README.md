Firestore's ClientIsOffline Issue
=================================

Reproduction repository for https://stackoverflow.com/questions/48188476/

Setup
-----

This project needs a Firebase project setup with Firestore to be used.

Provide `app/google-services.json` to build the project.

In Firestore, create a document with path `test_collection/test_document` containing a String field `test_key: "test_value"`

![Firestore database](https://raw.githubusercontent.com/AoDevBlue/FirestoreClientIsOfflineIssue/master/screenshots/database.png)

