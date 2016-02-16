package com.alcatel.mobilevoicemail.opentouch;

import android.os.AsyncTask;
import android.util.Log;

public class ExAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, ExAsyncTaskResult<Result>> {

    ExceptionHandler mExceptionHandler;

    private ExAsyncTask() {}

    public ExAsyncTask(ExceptionHandler handler) {
        super();
        mExceptionHandler = handler;
    }

    @Override
    protected ExAsyncTaskResult<Result> doInBackground(Params... params) {
        try {
            Result result = main(params);
            return new ExAsyncTaskResult<Result>(result);
        } catch ( Exception anyError) {
            return new ExAsyncTaskResult<Result>(anyError);
        }
    }

    @Override
    protected void onPostExecute(ExAsyncTaskResult<Result> result) {
        if ( result.getError() != null ) {
            // error handling here
            mExceptionHandler.handle(result.getError());
        }  else if ( isCancelled() ) {
            // cancel handling here
        } else {
            onSuccess(result.getResult());
        }
    }

    protected Result main(Object... params) throws Exception {
        Log.e(getClass().getSimpleName(), "Please reimplement ExAsyncTask.main()");
        return null;
    }

    protected void onSuccess(Result result) {
        Log.d(getClass().getSimpleName(), "onSuccess");
    }
}
