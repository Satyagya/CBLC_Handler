package com.example.CBLC_Handler.services;

import java.io.IOException;

public interface EGSchedulerService {
    void hitECService();
    void updateTableAfterEG(String fileName) throws IOException;
}
