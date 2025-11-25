package snapmeal.snapmeal.global.handler;

import snapmeal.snapmeal.global.code.BaseErrorCode;

public class RefreshTokenHandler extends GeneralException {
    public RefreshTokenHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
