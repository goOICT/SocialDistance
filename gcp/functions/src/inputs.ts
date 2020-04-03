export interface VisitInput {
  firstIdentity: string;
  secondIdentity: string;
  location: {
    latitude: number;
    longitude: number;
  };
  signalStrength?: number;
}
