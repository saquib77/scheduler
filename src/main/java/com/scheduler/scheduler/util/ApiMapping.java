package com.scheduler.scheduler.util;

/**
 * API route mappings for the scheduler service.
 */
public final class ApiMapping {

    private ApiMapping() {
    }

    public static final String BASE_PATH = "/api/v1/schedule";
    public static final String GENERIC = "";
    public static final String SLOTS = "/slots";
    public static final String SLOT = "/slot";
    public static final String JOB = "/job";
    public static final String DELETE_JOB = "/delete-job/{name}/{group}";
}
