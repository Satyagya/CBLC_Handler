package com.example.CBLC_Handler.services;

import java.io.IOException;

public interface LCSchedulerService {
    void hitLCService();
    void updateTableAfterLC(String fileName) throws IOException;
}
