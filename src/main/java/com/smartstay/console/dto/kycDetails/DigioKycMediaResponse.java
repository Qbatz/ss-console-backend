package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioKycMediaResponse(@JsonProperty("file_in_base64")
                                    String file,

                                    @JsonProperty("file_type")
                                    String fileType,

                                    @JsonProperty("file_name")
                                    String fileName,

                                    @JsonProperty("size_in_bytes")
                                    int fileSize) {
}
