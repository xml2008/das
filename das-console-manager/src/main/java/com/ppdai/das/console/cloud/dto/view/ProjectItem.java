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
public class ProjectItem {

	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String project_name;

}