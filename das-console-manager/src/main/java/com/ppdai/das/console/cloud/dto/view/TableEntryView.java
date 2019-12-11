package com.ppdai.das.console.cloud.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class TableEntryView {

    private Integer date_type;

    private String custom_table_name;

    private String custom_entry_name;

}
