package snapmeal.snapmeal.global.handler;

import snapmeal.snapmeal.global.code.ErrorCode;

public class RecommendationHandler extends GeneralException {
    public RecommendationHandler(ErrorCode errorCode) {
      super(errorCode);
    }
}
