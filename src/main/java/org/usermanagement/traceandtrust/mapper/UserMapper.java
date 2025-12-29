package org.usermanagement.traceandtrust.mapper;

import jdk.dynalink.beans.StaticClass;
import org.mapstruct.Mapper;
import org.usermanagement.traceandtrust.dto.CreateUserRequest;
import org.usermanagement.traceandtrust.dto.UserDto;
import org.usermanagement.traceandtrust.entity.User;

@Mapper(componentModel = "spring")
public  interface UserMapper {

    @org.mapstruct.Mapping(target = "active", source = "enabled")
    UserDto  toDto(User user);
    User   toEntity(CreateUserRequest request);

}
