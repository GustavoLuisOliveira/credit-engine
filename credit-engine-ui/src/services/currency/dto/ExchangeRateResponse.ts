export default interface ExchangeRateResponse {
    id: string;
    originCurrencyCode: string;
    destinationCurrencyCode: string;
    rate: number;
    rateDateTime: Date;
    createdAt: Date;
    updatedAt: Date;
}
