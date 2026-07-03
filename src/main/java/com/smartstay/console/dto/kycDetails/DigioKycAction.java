package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record DigioKycAction(String id,

                             @JsonProperty("action_ref")
                             String actionRef,

                             String type,

                             String status,

                             @JsonProperty("execution_request_id")
                             String executionRequestId,

                             DigioKycDetails details,

                             @JsonProperty("validation_result")
                             Map<String, Object> validationResult,

                             @JsonProperty("face_match_obj_type")
                             String faceMatchObjType,

                             @JsonProperty("face_match_status")
                             String faceMatchStatus,

                             @JsonProperty("obj_analysis_status")
                             String objAnalysisStatus,

                             @JsonProperty("processing_done")
                             Boolean processingDone,

                             @JsonProperty("retry_count")
                             Integer retryCount,

                             @JsonProperty("rules_data")
                             DigioKycRulesData rulesData) {
}
