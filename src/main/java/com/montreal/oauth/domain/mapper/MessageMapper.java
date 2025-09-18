package com.montreal.oauth.domain.mapper;

import com.montreal.core.domain.dto.response.MessageResponse;
import com.montreal.core.domain.enumerations.MessageTypeEnum;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class MessageMapper {

    public MessageResponse.MessageResponseBuilder createMessageBuilder(MessageTypeEnum messageTypeEnum, String detail, Map<String, List<String>> list) {

        var objects = (HashMap<String, List<String>>) list;

        return MessageResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(messageTypeEnum.getStatus())
                .title(messageTypeEnum.getTitle())
                .type(messageTypeEnum.getType())
                .objects(objects)
                .detail(detail);
    }

}
