import { HttpErrorResponse } from "@angular/common/http";
import { ErrorDialogBuilder } from "./error-handler";
import { StatusCode } from "./model";
import { InputError } from "./model/config-model";

describe('ErrorDialogBuilder', () => {
    let builder: ErrorDialogBuilder;
    beforeEach(() => {
        builder = new ErrorDialogBuilder();
    });

    it('should return dialog data for any from backend', () => {
        const error = { message: "test"};
        const data = new HttpErrorResponse({error, status: StatusCode.ERROR});
        expect(builder.build(data)).toEqual({
            resolution: "Ask administrators for help",
            message: "test",
            icon_name: "report_problem",
            icon_color: "red",
            title: "Error Details",
        })
    });

    it('should return dialog data for BAD_REQUEST from backend', () => {
        const error = { message: "test", resolution: "do this", title: "bad request"};
        const data = new HttpErrorResponse({error, status: StatusCode.BAD_REQUEST});
        expect(builder.build(data)).toEqual({
            resolution: "do this",
            message: "test",
            icon_name: "feedback",
            icon_color: "orange",
            title: "bad request",
        })
    });

    it('should return dialog data for internal input error', () => {
        const data = new InputError("input error");
        expect(builder.build(data)).toEqual({
            resolution: "Inspect error message and try to fix your request. If error persists contact administrator",
            message: "Error: input error",
            icon_name: "feedback",
            icon_color: "orange",
            title: "Error Details",
        })
    });
});
