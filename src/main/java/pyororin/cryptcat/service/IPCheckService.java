package pyororin.cryptcat.service;

import jakarta.servlet.http.HttpServletRequest;

public interface IPCheckService {
    boolean isNotAllowedIPAddress(HttpServletRequest request);
}
