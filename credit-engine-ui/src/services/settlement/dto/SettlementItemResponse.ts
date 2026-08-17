export default interface SettlementItemResponse {
    id: string;
    settlementId: string;
    receivableId: string;
    term: number;
    termMonths: number;
    baseRate: number;
    spreadRate: number;
    totalRate: number;
    originalCurrencyCode: string;
    faceValue: number;
    discountAmount: number;
    presentValue: number;
    settlementCurrencyCode: string;
    exchangeRateUsed: number;
    settlementAmount: number;
    createdAt: Date;
    updatedAt: Date;
}
