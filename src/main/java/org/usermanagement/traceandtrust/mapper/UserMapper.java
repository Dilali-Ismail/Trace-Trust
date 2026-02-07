package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.usermanagement.traceandtrust.dto.CreateUserRequest;
import org.usermanagement.traceandtrust.dto.UserDto;
import org.usermanagement.traceandtrust.entity.User;

@Mapper(componentModel = "spring")
public  interface UserMapper {

    UserDto  toDto(User user);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    User   toEntity(CreateUserRequest request);

}
