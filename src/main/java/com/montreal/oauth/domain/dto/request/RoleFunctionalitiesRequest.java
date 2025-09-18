package com.montreal.oauth.domain.dto.request;

import lombok.Data;


@Data
public class RoleFunctionalitiesRequest {


    private Integer roleId;
    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

}
