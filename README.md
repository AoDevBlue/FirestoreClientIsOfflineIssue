Firestore's ClientIsOffline Issue
=================================

Reproduction repository for https://stackoverflow.com/questions/48188476/

On Android, Firestore responds with
`com.google.firebase.firestore.FirebaseFirestoreException: Failed to get document because the client is offline.`
when fetching a document after reconnecting to the Internet.

This issue was reproduced on the firestore-core version ranging from 11.6.2 to 17.0.4

Setup
-----

This project needs a Firebase project setup with Firestore to be used.

Provide `app/google-services.json` to build the project.

In Firestore, create a document with path `test_collection/test_document` containing a String field `test_key: "test_value"`

![Firestore database](https://raw.githubusercontent.com/AoDevBlue/FirestoreClientIsOfflineIssue/master/screenshots/database.png)



