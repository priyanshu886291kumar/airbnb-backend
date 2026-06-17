package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.user;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.UserDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.UserProfileUpdateReqDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import org.jspecify.annotations.Nullable;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(UserProfileUpdateReqDTO updateReqDTO);

    @Nullable UserDTO getMyProfile();
}
