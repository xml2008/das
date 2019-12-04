package com.ppdai.das.console.cloud.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserView {

    @Column(name="user_no")
    private String userNo;

    @Column(name="user_real_name")
    private String userRealName;


}


