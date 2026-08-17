/** Mirrors backend com.luc.qa.common.exception.ErrorResponse */
export type ApiErrorBody = {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  details?: string[] | null;
};

export const SIGN_IN_REQUIRED = 'SIGN_IN_REQUIRED';

export class ApiError extends Error {
  readonly status: number;
  readonly errorLabel?: string;
  readonly details?: string[];
  readonly path?: string;

  constructor(
    status: number,
    message: string,
    options?: { errorLabel?: string; details?: string[]; path?: string }
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorLabel = options?.errorLabel;
    this.details = options?.details;
    this.path = options?.path;
  }

  static async fromResponse(res: Response): Promise<ApiError> {
    const text = await res.text();
    if (!text) {
      return new ApiError(res.status, `Request failed (${res.status})`);
    }

    try {
      const body = JSON.parse(text) as ApiErrorBody;
      const message =
        body.message ??
        body.error ??
        (body.details?.length ? body.details.join('; ') : undefined) ??
        text;

      return new ApiError(res.status, message, {
        errorLabel: body.error,
        details: body.details ?? undefined,
        path: body.path,
      });
    } catch {
      return new ApiError(res.status, text);
    }
  }

  get displayMessage(): string {
    if (this.details?.length) {
      return `${this.message}: ${this.details.join('; ')}`;
    }
    return this.message;
  }
}

export function isApiError(err: unknown, status?: number): err is ApiError {
  return err instanceof ApiError && (status === undefined || err.status === status);
}

export function getErrorMessage(err: unknown, fallback = 'Request failed'): string {
  if (err instanceof ApiError) {
    return err.displayMessage;
  }
  if (err instanceof Error) {
    return err.message;
  }
  return fallback;
}
