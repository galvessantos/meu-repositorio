package com.montreal.msiav_bh.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.domain.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserUtilsService {

    private final UserService userService;

    public UserResponse findByEmail(String email) {
        return userService.findByEmail(email);
    }

    public UserResponse getAuthenticatedUser() {
        return userService.getUser();
    }

    public UserInfo getAuthenticatedUserInfo() {
    	return userService.getUserInfo();
    }

    public UserInfo findById(Long userId) {
        return userService.findById(userId);
    }

    public UserInfo getLoggedInUser() {
    	return userService.getLoggedInUser();
    }

    public List<UserInfo> findUsersByCompanyId(Long companyId) {
    	return userService.findUsersByCompanyId(companyId);
    }
    
    public boolean checkUserIsAgenteOficial() {
    	var userInfo = getLoggedInUser();

        boolean hasPermission = userInfo.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.ROLE_AGENTE_OFICIAL);

        if (hasPermission) {
            return true;
        }
        
        return false;
    }

    public boolean checkUserIsLocalizador() {
    	var userInfo = getLoggedInUser();
    	
    	boolean hasPermission = userInfo.getRoles().stream()
    			.anyMatch(role -> role.getName() == RoleEnum.ROLE_LOCALIZADOR);
    	
    	if (hasPermission) {
    		return true;
    	}
    	
    	return false;
    }

    public boolean checkUserIsAdministrador() {
    	var userInfo = getLoggedInUser();
    	
    	boolean hasPermission = userInfo.getRoles().stream()
    			.anyMatch(role -> role.getName() == RoleEnum.ROLE_ADMIN);
    	
    	if (hasPermission) {
    		return true;
    	}
    	
    	return false;
    }
}
