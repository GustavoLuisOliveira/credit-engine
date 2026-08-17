import type { ReceivableType } from '../../receivable/ReceivableType.ts';

export default interface PricingParameterResponse {
    id: string;
    receivableType: ReceivableType;
    baseRate: number;
    spreadRate: number;
    effectiveDate: Date;
    createdAt: Date;
    updatedAt: Date;
}
