package snapmeal.snapmeal.service;

import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.web.dto.UserResponseDto;
import snapmeal.snapmeal.web.dto.UserRequestDto;

public interface UserCommandService {
    UserResponseDto.UserDto joinUser(UserRequestDto.JoinDto request);
    UserResponseDto.LoginDto saveNewUser(UserRequestDto.JoinDto request);


    UserResponseDto.LoginDto isnewUser(String email);
    UserResponseDto.LoginDto signIn(UserRequestDto.SignInRequestDto request);

//    UserResponseDto.userDto getMyUsers();
//    UserResponseDto.userDto getUsers(Long UserId);

   // UserMyPageResponseDto getMypages(String dataType);

    //UserMyPageResponseDto getUserMypage(Long userId, String dataType);

   // UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto);


    String logout(String aToken, String refreshToken);
}

