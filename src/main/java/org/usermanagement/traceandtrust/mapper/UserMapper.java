package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.Mapper;
import org.usermanagement.traceandtrust.dto.CreateUserRequest;
import org.usermanagement.traceandtrust.dto.UserDto;
import org.usermanagement.traceandtrust.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
    User toEntity(CreateUserRequest request);

}
