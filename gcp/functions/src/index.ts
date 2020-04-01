import * as functions from "firebase-functions";
import { firestore, initializeApp } from "firebase-admin";

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//

interface TrackingInput {
  myIdentity: string;
  metIdentity: string;
}

initializeApp();

export const visitIdentity = functions.https.onCall(
  async (data: TrackingInput, context) => {
    const batch = firestore().batch();
    const devicesCollection = firestore().collection("devices");
    const me = devicesCollection.doc(data.myIdentity);
    const other = devicesCollection.doc(data.metIdentity);

    me.update({
      visitedIds: firestore.FieldValue.arrayUnion(data.metIdentity)
    });

    other.update({
      visitedIds: firestore.FieldValue.arrayUnion(data.myIdentity)
    });

    await batch.commit();

    return {
      message: "Saved!"
    };
  }
);
