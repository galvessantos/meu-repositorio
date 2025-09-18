package com.montreal.oauth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.montreal.oauth.domain.dto.request.UserRequest;
import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.UserInfo;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    IUserMapper INSTANCE = Mappers.getMapper(IUserMapper.class);

    UserInfo toEntity(UserRequest userRequest);

    UserResponse toResponse(UserInfo userInfo);

}
