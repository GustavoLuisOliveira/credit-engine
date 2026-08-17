import type { SelectOption } from '../../components/shared/form/models/SelectOption.ts';

export type ReceivableType = 'COMMERCIAL_INVOICE' | 'POST_DATED_CHECK';

export const RECEIVABLE_TYPE_OPTIONS: SelectOption[] = [
    { label: 'Duplicata Mercantil', value: 'COMMERCIAL_INVOICE' },
    { label: 'Cheque Pré-datado', value: 'POST_DATED_CHECK' },
];
