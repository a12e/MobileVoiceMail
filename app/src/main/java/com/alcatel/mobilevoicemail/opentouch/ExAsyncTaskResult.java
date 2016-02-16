package com.alcatel.mobilevoicemail.opentouch;

public class ExAsyncTaskResult<T> {
    private T result;
    private Exception error;

    public T getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public ExAsyncTaskResult(T result) {
        super();
        this.result = result;
    }

    public ExAsyncTaskResult(Exception error) {
        super();
        this.error = error;
    }
}