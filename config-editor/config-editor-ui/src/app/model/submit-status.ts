export interface ISubmitStatus {
    submitInFlight: boolean;
    submitSuccess: boolean;
    statusCode: string;
    message: string;
}

export class SubmitStatus implements ISubmitStatus {
    public submitInFlight;
    public submitSuccess;
    public statusCode;
    public message;

    constructor(
            submitInFlight: boolean = false,
            submitSuccess: boolean = false,
            message: string = undefined,
            statusCode: string = undefined) {
        this.submitInFlight = submitInFlight;
        this.submitSuccess = submitSuccess;
        this.statusCode = statusCode;
        this.message = message;
    }
}
