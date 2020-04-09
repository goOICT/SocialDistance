export interface VisitInput {
  firstIdentity: string;
  secondIdentity: string;
  location: {
    latitude: number;
    longitude: number;
  };
  signalStrength?: number;
}

export interface SymptomsInput {
  identity: string;
  symptoms: string[];
}
