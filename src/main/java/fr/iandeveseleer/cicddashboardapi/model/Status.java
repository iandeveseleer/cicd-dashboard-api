package fr.iandeveseleer.cicddashboardapi.model;

import org.apache.commons.lang3.StringUtils;

public enum Status {
    CREATED,
    SUCCESS,
    FAILED,
    WAITING,
    IN_PROGRESS,
    BYPASSED,
    CANCELED,
    UNKNOWN;

    public static Status fromString(String status) {
        if(StringUtils.isNotEmpty(status)) {
            return switch (status.toLowerCase()) {
                case "created" -> CREATED;
                case "success" -> SUCCESS;
                case "failed" -> FAILED;
                case "pending","waiting_for_resource","preparing" -> WAITING;
                case "running","canceling" -> IN_PROGRESS;
                case "bypassed","skipped" -> BYPASSED;
                case "canceled" -> CANCELED;
                default -> UNKNOWN;
            };
        } else {
            return UNKNOWN;
        }
    }
}
