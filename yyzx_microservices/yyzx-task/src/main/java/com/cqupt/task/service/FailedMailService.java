package com.cqupt.task.service;

import com.cqupt.pojo.FailedMailRecord;
import java.util.List;

public interface FailedMailService {
    
    List<FailedMailRecord> findDueRecords();
    
    void retryMail(FailedMailRecord record) throws Exception;
    
    void markAsSuccess(Long id);
    
    void markAsFailed(Long id, String reason);
}
