import * as functions from "firebase-functions";
import * as moment from "moment";
import { firestore, initializeApp } from "firebase-admin";
import {
  COLLECTION_DEVICES,
  COLLECTION_VISITS,
  COLLECTION_SYMPTOMS
} from "./consts";
import { VisitInput, SymptomsInput } from "./inputs";

initializeApp();

export const visitIdentity = functions.https.onCall(
  async (data: VisitInput, context) => {
    const batch = firestore().batch();

    const documentData: firestore.DocumentData = {};

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

    try {
      batch.set(me.doc(data.secondIdentity), documentData);
      batch.set(other.doc(data.firstIdentity), documentData);
      await batch.commit();
      return {
        message: "Saved!"
      };
    } catch (e) {
      throw new functions.https.HttpsError("aborted", e);
    }
  }
);

export const submitSymptoms = functions.https.onCall(
  async (data: SymptomsInput, _) => {
    const collection = firestore()
      .collection(COLLECTION_DEVICES)
      .doc(data.identity)
      .collection(COLLECTION_SYMPTOMS);

    const documentData: firestore.DocumentData = {};

    const timestamp = firestore.Timestamp.now();
    const formattedDate = moment(timestamp.toDate()).format("YYYY-MM-DD");

    // super complicated algo for now
    documentData.score = data.symptoms.length;

    try {
      await collection.doc(formattedDate).set(documentData);
      return {
        score: documentData.score
      };
    } catch (e) {
      throw new functions.https.HttpsError("aborted", e);
    }
  }
);
