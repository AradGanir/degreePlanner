package com.example.degreePlanner.dto.response;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;

    public ErrorResponse(int status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    // getters
    public LocalDateTime getTimestamp() { return this.timestamp;}
    public int getStatus() { return this.status;}
    public String getError() { return this.error;}
    public String getMessage() { return this.message;}
}
