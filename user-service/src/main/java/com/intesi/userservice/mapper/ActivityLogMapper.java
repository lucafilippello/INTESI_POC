package com.intesi.userservice.mapper;

import com.intesi.userservice.dto.ActivityLogDto;
import com.intesi.userservice.model.ActivityLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    ActivityLogDto toDto(ActivityLog activityLog);
}
