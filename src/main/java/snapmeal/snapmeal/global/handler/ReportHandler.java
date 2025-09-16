package snapmeal.snapmeal.global.handler;

import snapmeal.snapmeal.global.GeneralException;
import snapmeal.snapmeal.global.code.BaseErrorCode;

public class ReportHandler extends GeneralException {
    public ReportHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
