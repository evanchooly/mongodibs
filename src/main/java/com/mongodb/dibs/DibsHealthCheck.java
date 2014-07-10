package com.mongodb.dibs;

import com.codahale.metrics.health.HealthCheck;

public class DibsHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
