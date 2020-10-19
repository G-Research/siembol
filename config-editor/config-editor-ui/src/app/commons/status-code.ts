export enum StatusCode {
    OK = 'OK',
    CREATED = 'CREATED',
    BAD_REQUEST = 'BAD_REQUEST',
    ERROR = 'INTERNAL_SERVER_ERROR',
}

export function getHttpErrorType(status: string): StatusCode {
    switch (status) {
        case StatusCode.OK || StatusCode.CREATED: {
            return StatusCode.OK;
        }
        case StatusCode.BAD_REQUEST: {
            return StatusCode.BAD_REQUEST;
        }
        default: {
            return StatusCode.ERROR;
        }
    }
}
