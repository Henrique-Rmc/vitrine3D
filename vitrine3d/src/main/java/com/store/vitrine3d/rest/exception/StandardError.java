package com.store.vitrine3d.rest.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class StandardError {
    private Instant timestamp;
    private Integer status;
    private String error;
    private String message;
}
