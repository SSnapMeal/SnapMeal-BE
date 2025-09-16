package snapmeal.snapmeal.global.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode implements BaseErrorCode {

    // 가장 일반적인 응답
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON001", "잘못된 입력값입니다."),

    //인증 관련 에러
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4010", "인증에 실패했습니다."),

    //유저 관련 응답
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST,"AUTH001" ,"비밀번호가 일치하지 않습니다." ),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH002", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH003", "이미 가입된 이메일입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "AUTH004", "접근 권한이 없습니다."),
    //친구 관련 응답
    ALREADY_SENT_REQUEST(HttpStatus.CONFLICT,"MATE001","이미 보낸 요청입니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "MATE002", "친구 요청을 찾을 수 없습니다."),
    NOT_AUTHORIZED_TO_CHANGE_REQUEST(HttpStatus.FORBIDDEN, "MATE003", "요청을 변경할 권한이 없습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "MATE002", "해당 요청에 대한 권한이 없습니다."),
    ALREADY_PROCESSED_REQUEST(HttpStatus.BAD_REQUEST, "MATE003", "이미 처리된 친구 요청입니다."),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "MATE003", "해당 친구 요청이 존재하지 않습니다."),

    //weekly report 관련 응답
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT001", "주간 리포트를 찾을 수 없습니다."),
    AI_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI001", "AI 응답 처리 중 오류가 발생했습니다.");
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;


    @Override
    public ErrorResponseDto getErrorResponse() {
        return ErrorResponseDto.builder()
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorResponseDto getErrorResponseHttpStatus() {
        return ErrorResponseDto.builder()
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
