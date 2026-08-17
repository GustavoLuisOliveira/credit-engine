export default interface SettlementExtractResponse {
    id: string;
    assignorId: string;
    assignorName: string;
    assignorDocumentNumber: string;
    settlementDateTime: Date;
    valuationDate: Date;
    targetCurrencyCode: string;
    totalFaceValue: number;
    totalDiscountAmount: number;
    totalNetAmount: number;
}
