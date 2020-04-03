import * as functions from "firebase-functions";
import { firestore, initializeApp } from "firebase-admin";
import { COLLECTION_DEVICES, COLLECTION_VISITS } from "./consts";
import { VisitInput } from "./inputs";

initializeApp();

export const visitIdentity = functions.https.onCall(
  async (data: VisitInput, context) => {
    const batch = firestore().batch();

    var documentData: firestore.DocumentData = {};

    documentData.timestamp = firestore.FieldValue.serverTimestamp();

    if (data.signalStrength) {
      documentData.signalStrength = data.signalStrength;
    }

    if (data.location) {
      documentData.location = new firestore.GeoPoint(
        data.location.latitude,
        data.location.longitude
      );
    }

    const devicesCollection = firestore().collection(COLLECTION_DEVICES);

    const me = devicesCollection
      .doc(data.firstIdentity)
      .collection(COLLECTION_VISITS);
    const other = devicesCollection
      .doc(data.secondIdentity)
      .collection(COLLECTION_VISITS);

    me.doc(data.secondIdentity).set(documentData);
    other.doc(data.firstIdentity).set(documentData);

    try {
      await batch.commit();
      return {
        message: "Saved!"
      };
    } catch (e) {
      throw new functions.https.HttpsError("aborted", e.toString());
    }
  }
);

export const getVisitedDevices = functions.https.onCall(
  async (data: { identity: string }, _) => {
    const visits = await firestore()
      .collection(COLLECTION_DEVICES)
      .doc(data.identity)
      .collection(COLLECTION_VISITS)
      .get();

    return {
      visits: visits.docs.map(x => x.data())
    };
  }
);
