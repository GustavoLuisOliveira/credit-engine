export default interface PricingSimulationResponse {
    receivableId: string;
    valuationDate: Date;
    baseRate: number;
    spreadRate: number;
    termMonths: number;
    faceValue: number;
    discountAmount: number;
    presentValue: number;
    currencyCode: string;
    targetCurrencyCode: string;
    exchangeRateUsed: number;
    convertedAmount: number;
}
