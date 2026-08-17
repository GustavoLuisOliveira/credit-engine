import type SettlementItemResponse from './SettlementItemResponse';

export default interface SettlementResponse {
    id: string;
    assignorId: string;
    settlementDateTime: Date;
    valuationDate: Date;
    targetCurrencyCode: string;
    totalFaceValue: number;
    totalDiscountAmount: number;
    totalNetAmount: number;
    items: SettlementItemResponse[];
    createdAt: Date;
    updatedAt: Date;
}
