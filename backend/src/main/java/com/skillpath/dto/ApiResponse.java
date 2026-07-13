package com.skillpath.dto;
import lombok.*;
import java.time.Instant;
@Getter @Builder
public class ApiResponse<T> {
 private final boolean success;
 private final T data;
 private final String message;
 
 @Builder.Default
 private final String timestamp = Instant.now().toString();

 public static <T> ApiResponse<T> ok(T data) {
    return ApiResponse.<T>builder().success(true).data(data).message("OK").build();
 }
 public static <T> ApiResponse<T> error(String msg) {
    return ApiResponse.<T>builder().success(false).message(msg).build();
 }
}
