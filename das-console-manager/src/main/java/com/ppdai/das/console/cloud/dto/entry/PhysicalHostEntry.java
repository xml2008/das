package com.ppdai.das.console.cloud.dto.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalHostEntry {

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "catalog")
    private String catalog;
}
