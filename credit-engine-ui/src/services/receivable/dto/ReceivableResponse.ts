import type { ReceivableType } from '../ReceivableType.ts';

export type ReceivableStatus = 'UNSETTLED' | 'SETTLED' | 'CANCELLED';

export default interface ReceivableResponse {
    id: string;
    assignorId: string;
    type: ReceivableType;
    documentNumber: string;
    faceValue: number;
    currencyCode: string;
    dueDate: Date;
    status: ReceivableStatus;
    createdAt: Date;
    updatedAt: Date;
}
