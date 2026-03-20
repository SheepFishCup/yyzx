package com.cqupt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.dto.ForgotPasswordDTO;
import com.cqupt.dto.LoginWithCodeDTO;
import com.cqupt.dto.ResetPasswordDTO;
import com.cqupt.dto.UserDTO;
import com.cqupt.pojo.User;
import com.cqupt.utils.ResultVo;

public interface UserService extends IService<User> {
    ResultVo<User> login(String username, String password) throws Exception;

    ResultVo<Page<User>> findUserPage(UserDTO userDTO) throws Exception;

    ResultVo<Page<User>> findAllUserPage(UserDTO userDTO) throws Exception;

    ResultVo addUser(User user) throws Exception;

    ResultVo updateUser(User user) throws Exception;

    ResultVo deleteUser(Long id) throws Exception;

    ResultVo changePassword(Long userId, String oldPassword, String newPassword);

    ResultVo loginWithCaptcha(LoginWithCodeDTO loginDTO);

    ResultVo forgotPassword(ForgotPasswordDTO forgotDTO);

    ResultVo resetPassword(ResetPasswordDTO resetDTO);

    ResultVo verifyResetToken(String token);
}
