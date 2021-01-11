package com.ppdai.das.console.cloud.dto.view;

import lombok.*;

import javax.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserGroupCloudView {

    @Column(name = "id")
    private Long id;

    @Column(name = "group_name")
    private String group_name;

    @Column(name = "role")
    private Integer role;

    @Column(name = "group_comment")
    private String group_comment;

}
