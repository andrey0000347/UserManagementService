package com.app.mapper;

import com.app.dto.request.UserCreateRequest;
import com.app.dto.response.UserResponse;
import com.app.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-12T22:22:19+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toEntity(UserCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.email( request.email() );
        userEntity.password( request.password() );
        userEntity.firstName( request.firstName() );
        userEntity.lastName( request.lastName() );
        userEntity.age( request.age() );

        return userEntity.build();
    }

    @Override
    public UserResponse toResponse(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;
        Long id = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        Integer age = null;

        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();
        id = entity.getId();
        email = entity.getEmail();
        firstName = entity.getFirstName();
        lastName = entity.getLastName();
        age = entity.getAge();

        String fullName = entity.getFirstName() + " " + entity.getLastName();

        UserResponse userResponse = new UserResponse( id, email, firstName, lastName, age, fullName, createdAt, updatedAt );

        return userResponse;
    }

    @Override
    public List<UserResponse> toResponseList(List<UserEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<UserResponse> list = new ArrayList<UserResponse>( entities.size() );
        for ( UserEntity userEntity : entities ) {
            list.add( toResponse( userEntity ) );
        }

        return list;
    }

    @Override
    public void updateEntity(UserEntity entity, UserCreateRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.email() != null ) {
            entity.setEmail( request.email() );
        }
        if ( request.password() != null ) {
            entity.setPassword( request.password() );
        }
        if ( request.firstName() != null ) {
            entity.setFirstName( request.firstName() );
        }
        if ( request.lastName() != null ) {
            entity.setLastName( request.lastName() );
        }
        if ( request.age() != null ) {
            entity.setAge( request.age() );
        }
    }
}
