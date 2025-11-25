package snapmeal.snapmeal.service;

import snapmeal.snapmeal.web.dto.UserRequestDto;
import snapmeal.snapmeal.web.dto.UserResponseDto;

public interface UserCommandService {
    UserResponseDto.UserDto joinUser(UserRequestDto.JoinDto request);
    UserResponseDto.LoginDto saveNewUser(UserRequestDto.JoinDto request);


    UserResponseDto.LoginDto isnewUser(String email);
    UserResponseDto.LoginDto signIn(UserRequestDto.SignInRequestDto request);

    void logout(String accessToken, String refreshToken);
    void deleteUser(String accessToken, String refreshToken);
}

