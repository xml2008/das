package com.ppdai.das.console.cloud.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class DasGroupView {

	@Column(name = "id")
	private Long id;

	@Column(name = "group_name")
	private String group_name;

}