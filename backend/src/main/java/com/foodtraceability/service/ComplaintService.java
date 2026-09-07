package com.foodtraceability.service;

import com.foodtraceability.dto.ComplaintDTO;

public interface ComplaintService {
    ComplaintDTO updateComplaintReason(Long id, String complaintReason );
    ComplaintDTO createComplaint(ComplaintDTO complaintDTO);
}