package pyororin.cryptcat.service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface RequestIntervalService {
    boolean shouldNotProcessRequest(HttpServletRequest request) throws IOException;
}
