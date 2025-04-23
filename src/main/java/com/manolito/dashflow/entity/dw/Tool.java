package com.manolito.dashflow.entity.dw;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tools", schema = "dw_dashflow")
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tool_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "seq", nullable = false)
    private Integer seq;

    @Size(max = 255)
    @NotNull
    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

}
