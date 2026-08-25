package com.luc.qa.module.alumni.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlumniDirectoryFilterDTO {

    private String q;
    private Integer gradYear;
    private String faculty;
    private String company;
    private int page = 0;
    private int size = 20;
}
