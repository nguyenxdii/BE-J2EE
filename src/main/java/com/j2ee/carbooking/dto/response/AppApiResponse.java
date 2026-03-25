package com.j2ee.carbooking.dto.response;

public class AppApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public AppApiResponse() {}

    public AppApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public static <T> AppApiResponse<T> success(String message, T data) {
        return new AppApiResponse<>(true, message, data);
    }

    public static <T> AppApiResponse<T> success(String message) {
        return new AppApiResponse<>(true, message, null);
    }

    public static <T> AppApiResponse<T> error(String message) {
        return new AppApiResponse<>(false, message, null);
    }
}
