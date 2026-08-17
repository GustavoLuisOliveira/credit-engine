export default interface SettlementExtractFilter {
    assignorId: string;
    currencyCode: string;
    valuationDateFrom: Date | null;
    valuationDateTo: Date | null;
}

export const emptySettlementExtractFilter: SettlementExtractFilter = {
    assignorId: '',
    currencyCode: '',
    valuationDateFrom: null,
    valuationDateTo: null,
};
